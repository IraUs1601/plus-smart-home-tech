package ru.yandex.practicum.commerce.payment.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.interaction_api.client.OrderClient;
import ru.yandex.practicum.commerce.interaction_api.client.ProductClient;
import ru.yandex.practicum.commerce.interaction_api.dto.OrderDto;
import ru.yandex.practicum.commerce.interaction_api.dto.PaymentDto;
import ru.yandex.practicum.commerce.interaction_api.dto.ProductDto;
import ru.yandex.practicum.commerce.interaction_api.enums.OrderState;
import ru.yandex.practicum.commerce.interaction_api.enums.PaymentStatus;
import ru.yandex.practicum.commerce.interaction_api.exceptions.NoOrderFoundException;
import ru.yandex.practicum.commerce.interaction_api.exceptions.NotEnoughInfoInOrderToCalculateException;
import ru.yandex.practicum.commerce.payment.mapper.PaymentMapper;
import ru.yandex.practicum.commerce.payment.model.Payment;
import ru.yandex.practicum.commerce.payment.repository.PaymentRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private static final double VAT_RATE = 0.10;
    private static final double DEFAULT_DELIVERY_COST = 50.0;

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final ProductClient productClient;
    private final OrderClient orderClient;

    @Transactional
    @CircuitBreaker(name = "payment", fallbackMethod = "paymentFallback")
    public PaymentDto payment(OrderDto order) {
        validateOrder(order);
        double productCost = calculateProductCost(order);
        double feeTotal = productCost * VAT_RATE;
        double deliveryTotal = order.getDeliveryPrice() != null ? order.getDeliveryPrice() : DEFAULT_DELIVERY_COST;
        double totalPayment = productCost + feeTotal + deliveryTotal;

        Payment payment = Payment.builder()
                .orderId(order.getOrderId())
                .totalPayment(totalPayment)
                .deliveryTotal(deliveryTotal)
                .feeTotal(feeTotal)
                .status(PaymentStatus.PENDING)
                .build();
        Payment saved = paymentRepository.save(payment);
        Map<String, UUID> assemblyRequest = new HashMap<>();
        assemblyRequest.put("orderId", payment.getOrderId());
        assemblyRequest.put("paymentId", saved.getId());
        if (order.getDeliveryId() != null) {
            assemblyRequest.put("deliveryId", order.getDeliveryId());
        }
        orderClient.assembly(assemblyRequest);
        return paymentMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public Double getTotalCost(OrderDto order) {
        validateOrder(order);
        double productCost = calculateProductCost(order);
        double feeTotal = productCost * VAT_RATE;
        double deliveryTotal = order.getDeliveryPrice() != null ? order.getDeliveryPrice() : DEFAULT_DELIVERY_COST;
        return productCost + feeTotal + deliveryTotal;
    }

    @Transactional
    public void paymentSuccess(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NoOrderFoundException("Payment not found: " + paymentId));
        payment.setStatus(PaymentStatus.SUCCESS);
        paymentRepository.save(payment);
        orderClient.payment(Map.of("orderId", payment.getOrderId(), "paymentId", paymentId));
    }

    @Transactional(readOnly = true)
    public Double productCost(OrderDto order) {
        validateOrder(order);
        return calculateProductCost(order);
    }

    @Transactional
    public void paymentFailed(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NoOrderFoundException("Payment not found: " + paymentId));
        payment.setStatus(PaymentStatus.FAILED);
        paymentRepository.save(payment);
        orderClient.updatePaymentStatus(payment.getOrderId(), OrderState.PAYMENT_FAILED);
    }

    private void validateOrder(OrderDto order) {
        if (order == null || order.getOrderId() == null || order.getProducts() == null || order.getProducts().isEmpty()) {
            throw new NotEnoughInfoInOrderToCalculateException("Order is missing required information");
        }
    }

    private double calculateProductCost(OrderDto order) {
        double total = 0.0;
        for (var entry : order.getProducts().entrySet()) {
            UUID productId = UUID.fromString(entry.getKey());
            Long quantity = entry.getValue();
            ProductDto product = productClient.getProduct(productId);
            if (product.getPrice() == null) {
                throw new NotEnoughInfoInOrderToCalculateException("Price not available for product: " + productId);
            }
            total += product.getPrice() * quantity;
        }
        return total;
    }

    @SuppressWarnings("unused")
    public PaymentDto paymentFallback(OrderDto order, Throwable t) {
        throw new NotEnoughInfoInOrderToCalculateException("Payment calculation failed: " + t.getMessage());
    }
}