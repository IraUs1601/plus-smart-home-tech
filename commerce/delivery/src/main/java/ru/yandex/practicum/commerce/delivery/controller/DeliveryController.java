package ru.yandex.practicum.commerce.delivery.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.commerce.delivery.service.DeliveryService;
import ru.yandex.practicum.commerce.interaction_api.api.DeliveryApi;
import ru.yandex.practicum.commerce.interaction_api.dto.DeliveryDto;
import ru.yandex.practicum.commerce.interaction_api.dto.OrderDto;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class DeliveryController implements DeliveryApi {

    private final DeliveryService deliveryService;

    @Override
    public DeliveryDto planDelivery(@Valid DeliveryDto delivery) {
        return deliveryService.planDelivery(delivery);
    }

    @Override
    public void deliverySuccessful(UUID orderId) {
        deliveryService.deliverySuccessful(orderId);
    }

    @Override
    public void deliveryPicked(UUID orderId) {
        deliveryService.deliveryPicked(orderId);
    }

    @Override
    public void deliveryFailed(UUID orderId) {
        deliveryService.deliveryFailed(orderId);
    }

    @Override
    public Double deliveryCost(@Valid OrderDto order) {
        return deliveryService.deliveryCost(order);
    }
}