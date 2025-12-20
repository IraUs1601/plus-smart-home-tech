package ru.yandex.practicum.commerce.shopping_store.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.commerce.interaction_api.api.ProductApi;
import ru.yandex.practicum.commerce.interaction_api.dto.ProductDto;
import ru.yandex.practicum.commerce.interaction_api.enums.Availability;
import ru.yandex.practicum.commerce.interaction_api.requests.SetProductQuantityStateRequest;
import ru.yandex.practicum.commerce.shopping_store.service.ProductService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ProductController implements ProductApi {

    private final ProductService productService;

    @Override
    public Page<ProductDto> getProducts(String category, Pageable pageable) {
        return productService.getProducts(category, pageable);
    }

    @Override
    public ProductDto getProduct(UUID productId) {
        return productService.getProduct(productId);
    }

    @Override
    public ProductDto createProduct(ProductDto product) {
        return productService.createNewProduct(product);
    }

    @Override
    public ProductDto updateProduct(ProductDto product) {
        return productService.updateProduct(product);
    }

    @Override
    public boolean removeProductFromStore(UUID productId) {
        return productService.removeProductFromStore(productId);
    }

    @Override
    public boolean setProductQuantityState(@RequestParam UUID productId, @RequestParam Availability quantityState) {
        SetProductQuantityStateRequest request = new SetProductQuantityStateRequest();
        request.setProductId(productId);
        request.setQuantityState(quantityState);
        return productService.setProductQuantityState(request);
    }
}