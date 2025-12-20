package ru.yandex.practicum.commerce.payment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.commerce.interaction_api.api.PaymentApi;
import ru.yandex.practicum.commerce.interaction_api.dto.OrderDto;
import ru.yandex.practicum.commerce.interaction_api.dto.PaymentDto;
import ru.yandex.practicum.commerce.payment.service.PaymentService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class PaymentController implements PaymentApi {

    private final PaymentService paymentService;

    @Override
    public PaymentDto payment(@Valid OrderDto order) {
        return paymentService.payment(order);
    }

    @Override
    public Double getTotalCost(@Valid OrderDto order) {
        return paymentService.getTotalCost(order);
    }

    @Override
    public void paymentSuccess(UUID paymentId) {
        paymentService.paymentSuccess(paymentId);
    }

    @Override
    public Double productCost(@Valid OrderDto order) {
        return paymentService.productCost(order);
    }

    @Override
    public void paymentFailed(UUID paymentId) {
        paymentService.paymentFailed(paymentId);
    }
}