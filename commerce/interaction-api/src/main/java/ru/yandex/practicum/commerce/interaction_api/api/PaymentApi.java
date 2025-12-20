package ru.yandex.practicum.commerce.interaction_api.api;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.commerce.interaction_api.dto.OrderDto;
import ru.yandex.practicum.commerce.interaction_api.dto.PaymentDto;

import java.util.UUID;

public interface PaymentApi {

    @PostMapping("/api/v1/payment")
    PaymentDto payment(@Valid @RequestBody OrderDto order);

    @PostMapping("/api/v1/payment/totalCost")
    Double getTotalCost(@Valid @RequestBody OrderDto order);

    @PostMapping("/api/v1/payment/refund")
    void paymentSuccess(@RequestBody UUID paymentId);

    @PostMapping("/api/v1/payment/productCost")
    Double productCost(@Valid @RequestBody OrderDto order);

    @PostMapping("/api/v1/payment/failed")
    void paymentFailed(@RequestBody UUID paymentId);
}