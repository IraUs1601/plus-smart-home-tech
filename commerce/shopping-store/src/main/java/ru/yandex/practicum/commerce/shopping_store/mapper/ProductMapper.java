package ru.yandex.practicum.commerce.shopping_store.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.commerce.interaction_api.dto.ProductDto;
import ru.yandex.practicum.commerce.shopping_store.model.Product;

@Component
public class ProductMapper {
    public ProductDto toDto(Product product) {
        return ProductDto.builder()
                .productId(product.getId())
                .productName(product.getProductName())
                .description(product.getDescription())
                .imageSrc(product.getImageSrc())
                .productCategory(product.getCategory())
                .quantityState(product.getAvailability())
                .productState(product.getStatus())
                .price(product.getPrice())
                .build();
    }

    public Product toEntity(ProductDto dto) {
        return Product.builder()
                .productName(dto.getProductName())
                .description(dto.getDescription())
                .imageSrc(dto.getImageSrc())
                .category(dto.getProductCategory())
                .availability(dto.getQuantityState())
                .status(dto.getProductState())
                .price(dto.getPrice())
                .build();
    }
}