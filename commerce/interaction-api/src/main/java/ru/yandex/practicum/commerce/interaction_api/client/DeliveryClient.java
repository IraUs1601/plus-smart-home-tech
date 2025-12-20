package ru.yandex.practicum.commerce.interaction_api.client;

import org.springframework.cloud.openfeign.FeignClient;
import ru.yandex.practicum.commerce.interaction_api.api.DeliveryApi;

@FeignClient(name = "delivery")
public interface DeliveryClient extends DeliveryApi {
}