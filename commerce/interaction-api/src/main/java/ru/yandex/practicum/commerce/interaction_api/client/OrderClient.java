package ru.yandex.practicum.commerce.interaction_api.client;

import org.springframework.cloud.openfeign.FeignClient;
import ru.yandex.practicum.commerce.interaction_api.api.OrderApi;

@FeignClient(name = "order")
public interface OrderClient extends OrderApi {
}