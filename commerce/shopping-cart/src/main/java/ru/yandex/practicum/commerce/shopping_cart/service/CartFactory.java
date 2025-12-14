package ru.yandex.practicum.commerce.shopping_cart.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.interaction_api.enums.Status;
import ru.yandex.practicum.commerce.shopping_cart.model.ShoppingCart;
import ru.yandex.practicum.commerce.shopping_cart.repository.ShoppingCartRepository;

@Component
@RequiredArgsConstructor
public class CartFactory {
    private final ShoppingCartRepository cartRepository;

    @Transactional
    public ShoppingCart createNewCart(String username) {
        ShoppingCart cart = ShoppingCart.builder()
                .username(username)
                .status(Status.ACTIVE)
                .build();
        return cartRepository.save(cart);
    }
}