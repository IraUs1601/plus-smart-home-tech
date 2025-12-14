package ru.yandex.practicum.commerce.interaction_api.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.commerce.interaction_api.requests.ChangeProductQuantityRequest;
import ru.yandex.practicum.commerce.interaction_api.dto.ShoppingCartDto;

import java.util.List;
import java.util.Map;

@FeignClient(name = "shopping-cart")
public interface ShoppingCartClient {
    String CART_ENDPOINT = "/api/v1/shopping-cart";

    @PutMapping(CART_ENDPOINT)
    ShoppingCartDto addProductToShoppingCart(@RequestParam("username") String username,
                                             @RequestBody Map<String, Long> products);

    @GetMapping(CART_ENDPOINT)
    ShoppingCartDto getShoppingCart(@RequestParam("username") String username);

    @PostMapping(CART_ENDPOINT + "/change-quantity")
    ShoppingCartDto changeProductQuantity(@RequestParam("username") String username,
                                          @RequestBody ChangeProductQuantityRequest request);

    @PostMapping(CART_ENDPOINT + "/remove")
    ShoppingCartDto removeFromShoppingCart(@RequestParam("username") String username,
                                           @RequestBody List<String> productIds);

    @DeleteMapping(CART_ENDPOINT)
    void deactivateCurrentShoppingCart(@RequestParam("username") String username);
}