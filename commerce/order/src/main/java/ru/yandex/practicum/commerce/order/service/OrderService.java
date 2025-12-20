package ru.yandex.practicum.commerce.order.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.interaction_api.client.WarehouseClient;
import ru.yandex.practicum.commerce.interaction_api.dto.BookedProductsDto;
import ru.yandex.practicum.commerce.interaction_api.dto.OrderDto;
import ru.yandex.practicum.commerce.interaction_api.dto.ProductDto;
import ru.yandex.practicum.commerce.interaction_api.dto.ShoppingCartDto;
import ru.yandex.practicum.commerce.interaction_api.enums.OrderState;
import ru.yandex.practicum.commerce.interaction_api.exceptions.NoOrderFoundException;
import ru.yandex.practicum.commerce.interaction_api.exceptions.NoSpecifiedProductInWarehouseException;
import ru.yandex.practicum.commerce.interaction_api.exceptions.NotAuthorizedUserException;
import ru.yandex.practicum.commerce.interaction_api.requests.AssemblyProductsForOrderRequest;
import ru.yandex.practicum.commerce.interaction_api.requests.CreateNewOrderRequest;
import ru.yandex.practicum.commerce.interaction_api.requests.ProductReturnRequest;
import ru.yandex.practicum.commerce.order.mapper.OrderMapper;
import ru.yandex.practicum.commerce.order.model.Order;
import ru.yandex.practicum.commerce.order.repository.OrderRepository;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final WarehouseClient warehouseClient;

    static final String ORDER_NOT_FOUND = "Order not found: ";

    @Transactional(readOnly = true)
    public List<OrderDto> getClientOrders(String username) {
        validateUsername(username);
        return orderRepository.findByUsername(username)
                .stream()
                .map(orderMapper::toDto)
                .toList();
    }

    @Transactional
    @CircuitBreaker(name = "warehouse", fallbackMethod = "createOrderFallback")
    public OrderDto createNewOrder(CreateNewOrderRequest request) {
        ShoppingCartDto cart = request.getShoppingCart();
        if (cart == null || cart.getProducts() == null || cart.getProducts().isEmpty()) {
            throw new IllegalArgumentException("Shopping cart or products cannot be null or empty");
        }
        try {
            BookedProductsDto bookedProducts = warehouseClient.checkProductQuantityEnoughForShoppingCart(cart);
            double productPrice = calculateProductPrice(cart);
            String username = extractUsernameFromCart(cart);
            log.info("Creating order with deliveryAddress: {}", request.getDeliveryAddress());
            Map<UUID, Long> productsMap = cart.getProducts().entrySet().stream()
                    .collect(Collectors.toMap(
                            e -> {
                                try {
                                    return UUID.fromString(e.getKey());
                                } catch (IllegalArgumentException ex) {
                                    throw new IllegalArgumentException("Invalid product ID: " + e.getKey(), ex);
                                }
                            },
                            Map.Entry::getValue));
            log.info("Products map: {}", productsMap);
            if (productsMap == null || productsMap.isEmpty()) {
                throw new IllegalStateException("Products map is null or empty after conversion");
            }
            Order order = Order.builder()
                    .username(username)
                    .shoppingCartId(UUID.fromString(cart.getShoppingCartId()))
                    .products(productsMap)
                    .state(OrderState.NEW)
                    .deliveryWeight(bookedProducts.getDeliveryWeight())
                    .deliveryVolume(bookedProducts.getDeliveryVolume())
                    .fragile(bookedProducts.getFragile())
                    .productPrice(productPrice)
                    .deliveryAddress(request.getDeliveryAddress())
                    .build();
            log.info("Saving order: {}", order);
            Order saved = orderRepository.save(order);
            AssemblyProductsForOrderRequest assemblyRequest = new AssemblyProductsForOrderRequest();
            assemblyRequest.setOrderId(saved.getId());
            assemblyRequest.setProducts(productsMap);
            ObjectMapper mapper = new ObjectMapper();
            log.info("Assembly request JSON: {}", mapper.writeValueAsString(assemblyRequest));
            warehouseClient.assemblyProductForOrderFromShoppingCart(assemblyRequest);
            return orderMapper.toDto(saved);
        } catch (Exception e) {
            log.error("Failed to create order: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create order: " + e.getMessage(), e);
        }
    }

    @Transactional
    public OrderDto productReturn(ProductReturnRequest request) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new NoOrderFoundException(ORDER_NOT_FOUND + request.getOrderId()));
        order.setState(OrderState.PRODUCT_RETURNED);
        orderRepository.save(order);
        return orderMapper.toDto(order);
    }

    @Transactional
    public OrderDto payment(Map<String, UUID> request) {
        UUID orderId = request.get("orderId");
        UUID paymentId = request.get("paymentId");
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoOrderFoundException(ORDER_NOT_FOUND + orderId));
        order.setState(OrderState.PAID);
        order.setPaymentId(paymentId);
        orderRepository.save(order);
        return orderMapper.toDto(order);
    }

    @Transactional
    public OrderDto paymentFailed(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoOrderFoundException(ORDER_NOT_FOUND + orderId));
        order.setState(OrderState.PAYMENT_FAILED);
        orderRepository.save(order);
        return orderMapper.toDto(order);
    }

    @Transactional
    public OrderDto delivery(Map<String, UUID> request) {
        UUID orderId = request.get("orderId");
        UUID deliveryId = request.get("deliveryId");
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoOrderFoundException(ORDER_NOT_FOUND + orderId));
        order.setState(OrderState.DELIVERED);
        order.setDeliveryId(deliveryId);
        orderRepository.save(order);
        return orderMapper.toDto(order);
    }

    @Transactional
    public OrderDto deliveryFailed(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoOrderFoundException(ORDER_NOT_FOUND + orderId));
        order.setState(OrderState.DELIVERY_FAILED);
        orderRepository.save(order);
        return orderMapper.toDto(order);
    }

    @Transactional
    public OrderDto complete(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoOrderFoundException(ORDER_NOT_FOUND + orderId));
        order.setState(OrderState.COMPLETED);
        orderRepository.save(order);
        return orderMapper.toDto(order);
    }

    @Transactional
    public OrderDto calculateTotalCost(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoOrderFoundException(ORDER_NOT_FOUND + orderId));

        double totalPrice =
                (order.getProductPrice() != null ? order.getProductPrice() : 0.0) +
                        (order.getDeliveryPrice() != null ? order.getDeliveryPrice() : 0.0);

        order.setTotalPrice(totalPrice);
        orderRepository.save(order);

        return orderMapper.toDto(order);
    }

    @Transactional
    public OrderDto calculateDeliveryCost(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoOrderFoundException(ORDER_NOT_FOUND + orderId));
        double deliveryPrice = calculateDeliveryPrice(order);
        order.setDeliveryPrice(deliveryPrice);
        orderRepository.save(order);
        return orderMapper.toDto(order);
    }

    @Transactional
    public OrderDto assembly(Map<String, UUID> request) {
        UUID orderId = request.get("orderId");
        UUID deliveryId = request.get("deliveryId");
        UUID paymentId = request.get("paymentId");
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoOrderFoundException(ORDER_NOT_FOUND + orderId));
        order.setState(OrderState.ASSEMBLED);
        if (deliveryId != null) {
            order.setDeliveryId(deliveryId);
        }
        if (paymentId != null) {
            order.setPaymentId(paymentId);
        }
        orderRepository.save(order);
        return orderMapper.toDto(order);
    }

    @Transactional
    public OrderDto assemblyFailed(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoOrderFoundException(ORDER_NOT_FOUND + orderId));
        order.setState(OrderState.ASSEMBLY_FAILED);
        orderRepository.save(order);
        return orderMapper.toDto(order);
    }

    @Transactional
    public OrderDto updatePaymentStatus(UUID orderId, OrderState status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoOrderFoundException(ORDER_NOT_FOUND + orderId));
        order.setState(status);
        orderRepository.save(order);
        return orderMapper.toDto(order);
    }

    private double calculateProductPrice(ShoppingCartDto cart) {
        double total = 0.0;
        for (Map.Entry<String, Long> entry : cart.getProducts().entrySet()) {
            UUID productId = UUID.fromString(entry.getKey());
            Long quantity = entry.getValue();
            ProductDto product = warehouseClient.getProduct(productId);
            total += product.getPrice() * quantity;
        }
        return total;
    }

    private double calculateDeliveryPrice(Order order) {
        double baseCost = 10.0;
        double weightCost = order.getDeliveryWeight() != null ? order.getDeliveryWeight() : 0.0;
        double fragileCost = order.getFragile() != null && order.getFragile() ? 5.0 : 0.0;
        return baseCost + weightCost + fragileCost;
    }

    private void validateUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new NotAuthorizedUserException("Username cannot be empty");
        }
    }

    private String extractUsernameFromCart(ShoppingCartDto cart) {
        return "user";
    }

    @SuppressWarnings("unused")
    public OrderDto createOrderFallback(CreateNewOrderRequest request, Throwable t) {
        log.error("Fallback triggered for createOrderFallback: {}", t.getMessage());
        throw new NoSpecifiedProductInWarehouseException("Warehouse service unavailable: " + t.getMessage());
    }
}