package ru.yandex.practicum.commerce.interaction_api.api;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.commerce.interaction_api.dto.ProductDto;
import ru.yandex.practicum.commerce.interaction_api.enums.Availability;

import java.util.UUID;

public interface ProductApi {

    @GetMapping("/api/v1/shopping-store")
    Page<ProductDto> getProducts(@RequestParam String category, Pageable pageable);

    @GetMapping("/api/v1/shopping-store/{productId}")
    ProductDto getProduct(@PathVariable UUID productId);

    @PutMapping("/api/v1/shopping-store")
    ProductDto createProduct(@RequestBody ProductDto product);

    @PostMapping("/api/v1/shopping-store")
    ProductDto updateProduct(@RequestBody ProductDto product);

    @PostMapping("/api/v1/shopping-store/removeProductFromStore")
    boolean removeProductFromStore(@RequestBody UUID productId);

    @PostMapping("/api/v1/shopping-store/quantityState")
    boolean setProductQuantityState(@RequestParam UUID productId,
                                    @RequestParam Availability quantityState
    );
}