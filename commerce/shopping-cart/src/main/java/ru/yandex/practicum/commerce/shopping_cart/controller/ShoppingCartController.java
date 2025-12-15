package ru.yandex.practicum.commerce.shopping_cart.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.commerce.interaction_api.client.ShoppingCartClient;
import ru.yandex.practicum.commerce.interaction_api.requests.ChangeProductQuantityRequest;
import ru.yandex.practicum.commerce.interaction_api.dto.ShoppingCartDto;
import ru.yandex.practicum.commerce.shopping_cart.service.ShoppingCartService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/shopping-cart")
@RequiredArgsConstructor
public class ShoppingCartController implements ShoppingCartClient {
    private final ShoppingCartService shoppingCartService;

    @Override
    @PutMapping
    public ShoppingCartDto addProductToShoppingCart(@RequestParam("username") String username,
                                                    @RequestBody Map<String, Long> products) {
        return shoppingCartService.addProductToShoppingCart(username, products);
    }

    @Override
    @GetMapping
    public ShoppingCartDto getShoppingCart(@RequestParam("username") String username) {
        return shoppingCartService.getShoppingCart(username);
    }

    @Override
    @PostMapping("/change-quantity")
    public ShoppingCartDto changeProductQuantity(@RequestParam("username") String username,
                                                 @Valid @RequestBody ChangeProductQuantityRequest request) {
        return shoppingCartService.changeProductQuantity(username, request);
    }

    @Override
    @PostMapping("/remove")
    public ShoppingCartDto removeFromShoppingCart(@RequestParam("username") String username,
                                                  @RequestBody List<String> productIds) {
        return shoppingCartService.removeFromShoppingCart(username, productIds);
    }

    @Override
    @DeleteMapping
    public void deactivateCurrentShoppingCart(@RequestParam("username") String username) {
        shoppingCartService.deactivateCurrentShoppingCart(username);
    }
}