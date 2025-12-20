package ru.yandex.practicum.commerce.order.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.commerce.interaction_api.dto.AddressDto;
import ru.yandex.practicum.commerce.interaction_api.dto.OrderDto;
import ru.yandex.practicum.commerce.order.model.Order;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class OrderMapper {
    public OrderDto toDto(Order order) {
        Map<String, Long> products = new HashMap<>();
        order.getProducts().forEach((key, value) -> products.put(key.toString(), value));
        return OrderDto.builder()
                .orderId(order.getId())
                .shoppingCartId(order.getShoppingCartId())
                .products(products)
                .paymentId(order.getPaymentId())
                .deliveryId(order.getDeliveryId())
                .state(order.getState())
                .deliveryWeight(order.getDeliveryWeight())
                .deliveryVolume(order.getDeliveryVolume())
                .fragile(order.getFragile())
                .totalPrice(order.getTotalPrice())
                .deliveryPrice(order.getDeliveryPrice())
                .productPrice(order.getProductPrice())
                .deliveryAddress(order.getDeliveryAddress())
                .build();
    }

    public Order toEntity(OrderDto dto, AddressDto addressDto) {
        Map<UUID, Long> products = new HashMap<>();
        dto.getProducts().forEach((key, value) -> products.put(UUID.fromString(key), value));
        return Order.builder()
                .id(dto.getOrderId())
                .shoppingCartId(dto.getShoppingCartId())
                .products(products)
                .paymentId(dto.getPaymentId())
                .deliveryId(dto.getDeliveryId())
                .state(dto.getState())
                .deliveryWeight(dto.getDeliveryWeight())
                .deliveryVolume(dto.getDeliveryVolume())
                .fragile(dto.getFragile())
                .totalPrice(dto.getTotalPrice())
                .deliveryPrice(dto.getDeliveryPrice())
                .productPrice(dto.getProductPrice())
                .deliveryAddress(dto.getDeliveryAddress() != null ? dto.getDeliveryAddress() : addressDto)
                .build();
    }
}