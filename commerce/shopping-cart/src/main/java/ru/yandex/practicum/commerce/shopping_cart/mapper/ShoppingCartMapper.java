package ru.yandex.practicum.commerce.shopping_cart.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.commerce.interaction_api.dto.ShoppingCartDto;
import ru.yandex.practicum.commerce.shopping_cart.model.ShoppingCart;

import java.util.HashMap;
import java.util.Map;

@Component
public class ShoppingCartMapper {
    public ShoppingCartDto toDto(ShoppingCart cart) {
        Map<String, Long> products = new HashMap<>();
        cart.getItems().forEach(item -> products.put(item.getProductId().toString(), item.getQuantity()));
        return ShoppingCartDto.builder()
                .shoppingCartId(cart.getId().toString())
                .products(products)
                .build();
    }
}