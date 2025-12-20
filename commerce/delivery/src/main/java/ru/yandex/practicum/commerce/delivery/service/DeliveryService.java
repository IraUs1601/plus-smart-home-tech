package ru.yandex.practicum.commerce.delivery.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.delivery.mapper.DeliveryMapper;
import ru.yandex.practicum.commerce.delivery.model.Delivery;
import ru.yandex.practicum.commerce.delivery.repository.DeliveryRepository;
import ru.yandex.practicum.commerce.interaction_api.client.OrderClient;
import ru.yandex.practicum.commerce.interaction_api.client.WarehouseClient;
import ru.yandex.practicum.commerce.interaction_api.dto.AddressDto;
import ru.yandex.practicum.commerce.interaction_api.dto.DeliveryDto;
import ru.yandex.practicum.commerce.interaction_api.dto.OrderDto;
import ru.yandex.practicum.commerce.interaction_api.enums.DeliveryState;
import ru.yandex.practicum.commerce.interaction_api.exceptions.NoDeliveryFoundException;
import ru.yandex.practicum.commerce.interaction_api.requests.ShippedToDeliveryRequest;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeliveryService {
    private static final double BASE_COST = 5.0;
    static final String DELIVERY_NOT_FOUND = "Delivery not found for order: ";

    private final DeliveryRepository deliveryRepository;
    private final DeliveryMapper deliveryMapper;
    private final OrderClient orderClient;
    private final WarehouseClient warehouseClient;

    @Transactional
    @CircuitBreaker(name = "delivery", fallbackMethod = "planDeliveryFallback")
    public DeliveryDto planDelivery(DeliveryDto delivery) {
        if (delivery == null || delivery.getOrderId() == null) {
            throw new NoDeliveryFoundException("Order ID is required");
        }

        OrderDto order = orderClient.calculateTotalCost(delivery.getOrderId());
        if (order == null) {
            throw new NoDeliveryFoundException("Order not found: " + delivery.getOrderId());
        }
        if (order.getDeliveryAddress() == null) {
            throw new NoDeliveryFoundException("Order has no deliveryAddress: " + delivery.getOrderId());
        }

        AddressDto warehouseAddress = warehouseClient.getWarehouseAddress();
        if (warehouseAddress == null) {
            throw new NoDeliveryFoundException("Warehouse address is not available");
        }

        Delivery entity = deliveryMapper.toEntity(delivery, order, warehouseAddress);
        entity.setState(DeliveryState.CREATED);

        Delivery saved = deliveryRepository.save(entity);

        orderClient.assembly(Map.of("orderId", delivery.getOrderId(), "deliveryId", saved.getId()));

        return deliveryMapper.toDto(saved);
    }

    @Transactional
    public void deliverySuccessful(UUID orderId) {
        Delivery delivery = deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new NoDeliveryFoundException(DELIVERY_NOT_FOUND + orderId));
        delivery.setState(DeliveryState.DELIVERED);
        deliveryRepository.save(delivery);
        orderClient.delivery(Map.of("orderId", orderId, "deliveryId", delivery.getId()));
    }

    @Transactional
    public void deliveryPicked(UUID orderId) {
        Delivery delivery = deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new NoDeliveryFoundException(DELIVERY_NOT_FOUND + orderId));

        delivery.setState(DeliveryState.IN_PROGRESS);
        deliveryRepository.save(delivery);

        ShippedToDeliveryRequest request = ShippedToDeliveryRequest.builder()
                .orderId(orderId)
                .deliveryId(delivery.getId())
                .build();
        warehouseClient.shippedToDelivery(request);

        orderClient.assembly(Map.of(
                "orderId", orderId,
                "deliveryId", delivery.getId()
        ));
    }

    @Transactional
    public void deliveryFailed(UUID orderId) {
        Delivery delivery = deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new NoDeliveryFoundException(DELIVERY_NOT_FOUND + orderId));
        delivery.setState(DeliveryState.FAILED);
        deliveryRepository.save(delivery);
        orderClient.deliveryFailed(orderId);
    }

    @Transactional(readOnly = true)
    public Double deliveryCost(OrderDto order) {
        if (order == null || order.getOrderId() == null) {
            throw new NoDeliveryFoundException("Order is missing required information");
        }

        AddressDto warehouseAddress = warehouseClient.getWarehouseAddress();
        if (warehouseAddress == null || warehouseAddress.getStreet() == null) {
            throw new NoDeliveryFoundException("Warehouse address is required to calculate delivery cost");
        }

        AddressDto toAddress = order.getDeliveryAddress();
        if (toAddress == null || toAddress.getStreet() == null) {
            throw new NoDeliveryFoundException("Delivery address is required to calculate delivery cost");
        }

        double cost = BASE_COST;

        String whStreet = warehouseAddress.getStreet();
        double factor;
        if (whStreet.contains("ADDRESS_1")) {
            factor = 1.0;
        } else if (whStreet.contains("ADDRESS_2")) {
            factor = 2.0;
        } else {
            throw new NoDeliveryFoundException(
                    "Warehouse address must contain ADDRESS_1 or ADDRESS_2"
            );
        }

        cost = cost * factor + BASE_COST;

        if (Boolean.TRUE.equals(order.getFragile())) {
            cost += cost * 0.2;
        }

        if (order.getDeliveryWeight() != null) {
            cost += order.getDeliveryWeight() * 0.3;
        }

        if (order.getDeliveryVolume() != null) {
            cost += order.getDeliveryVolume() * 0.2;
        }

        String toStreet = toAddress.getStreet();
        if (!toStreet.equals(whStreet)) {
            cost += cost * 0.2;
        }

        return cost;
    }

    @SuppressWarnings("unused")
    public DeliveryDto planDeliveryFallback(DeliveryDto delivery, Throwable t) {
        throw new NoDeliveryFoundException("Delivery planning failed: " + t.getMessage());
    }
}