package ru.yandex.practicum.commerce.delivery.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.commerce.delivery.model.Delivery;
import ru.yandex.practicum.commerce.interaction_api.dto.AddressDto;
import ru.yandex.practicum.commerce.interaction_api.dto.DeliveryDto;
import ru.yandex.practicum.commerce.interaction_api.dto.OrderDto;
import ru.yandex.practicum.commerce.interaction_api.enums.DeliveryState;

import java.util.UUID;

@Component
public class DeliveryMapper {

    public DeliveryDto toDto(Delivery delivery) {
        return DeliveryDto.builder()
                .deliveryId(delivery.getId())
                .fromAddress(delivery.getFromAddress())
                .toAddress(delivery.getToAddress())
                .orderId(delivery.getOrderId())
                .deliveryState(delivery.getState())
                .build();
    }

    public Delivery toEntity(DeliveryDto dto, OrderDto order, AddressDto warehouseAddress) {
        UUID deliveryId = dto.getDeliveryId() != null ? dto.getDeliveryId() : UUID.randomUUID();

        return Delivery.builder()
                .id(deliveryId)
                .orderId(dto.getOrderId())
                .fromAddress(warehouseAddress)
                .toAddress(order.getDeliveryAddress())
                .deliveryVolume(order.getDeliveryVolume() != null ? order.getDeliveryVolume() : 0.0)
                .deliveryWeight(order.getDeliveryWeight() != null ? order.getDeliveryWeight() : 0.0)
                .fragile(Boolean.TRUE.equals(order.getFragile()))
                .state(dto.getDeliveryState() != null ? dto.getDeliveryState() : DeliveryState.CREATED)
                .build();
    }
}