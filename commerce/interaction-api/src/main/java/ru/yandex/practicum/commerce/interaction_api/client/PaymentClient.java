package ru.yandex.practicum.commerce.interaction_api.client;

import org.springframework.cloud.openfeign.FeignClient;
import ru.yandex.practicum.commerce.interaction_api.api.PaymentApi;

@FeignClient(name = "payment")
public interface PaymentClient extends PaymentApi {
}