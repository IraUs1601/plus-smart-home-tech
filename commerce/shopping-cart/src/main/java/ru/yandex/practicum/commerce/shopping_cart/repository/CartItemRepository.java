package ru.yandex.practicum.commerce.shopping_cart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.commerce.shopping_cart.model.CartItem;
import ru.yandex.practicum.commerce.shopping_cart.model.ShoppingCart;

import java.util.Optional;
import java.util.UUID;

public interface CartItemRepository extends JpaRepository<CartItem, UUID> {
    Optional<CartItem> findByCartAndProductId(ShoppingCart cart, UUID productId);
}