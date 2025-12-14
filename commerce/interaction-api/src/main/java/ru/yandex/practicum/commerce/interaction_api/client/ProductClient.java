package ru.yandex.practicum.commerce.interaction_api.client;

import org.springframework.cloud.openfeign.FeignClient;
import ru.yandex.practicum.commerce.interaction_api.api.ProductApi;

@FeignClient(name = "shopping-store")
public interface ProductClient extends ProductApi {
}