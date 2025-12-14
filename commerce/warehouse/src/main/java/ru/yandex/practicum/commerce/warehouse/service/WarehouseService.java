package ru.yandex.practicum.commerce.warehouse.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.interaction_api.client.ProductClient;
import ru.yandex.practicum.commerce.interaction_api.requests.AddProductToWarehouseRequest;
import ru.yandex.practicum.commerce.interaction_api.dto.AddressDto;
import ru.yandex.practicum.commerce.interaction_api.requests.NewProductInWarehouseRequest;
import ru.yandex.practicum.commerce.interaction_api.dto.ProductDto;
import ru.yandex.practicum.commerce.interaction_api.dto.ShoppingCartDto;
import ru.yandex.practicum.commerce.interaction_api.exceptions.NoSpecifiedProductInWarehouseException;
import ru.yandex.practicum.commerce.interaction_api.exceptions.ProductInShoppingCartLowQuantityInWarehouse;
import ru.yandex.practicum.commerce.interaction_api.exceptions.SpecifiedProductAlreadyInWarehouseException;
import ru.yandex.practicum.commerce.warehouse.model.Product;
import ru.yandex.practicum.commerce.warehouse.repository.ProductRepository;

import java.security.SecureRandom;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WarehouseService {
    private static final String[] ADDRESSES = new String[]{"ADDRESS_1", "ADDRESS_2"};
    private static final String CURRENT_ADDRESS = ADDRESSES[new SecureRandom().nextInt(0, 2)];

    private final ProductRepository productRepository;
    private final ProductClient productClient;

    @Transactional
    public void newProductInWarehouse(NewProductInWarehouseRequest request) {
        Optional<Product> existingProduct = productRepository.findById(request.getProductId());
        if (existingProduct.isPresent()) {
            throw new SpecifiedProductAlreadyInWarehouseException(
                    "Product with ID " + request.getProductId() + " already exists in warehouse");
        }

        Product product = new Product();
        product.setProductId(request.getProductId());
        product.setWidth(request.getDimension().getWidth());
        product.setHeight(request.getDimension().getHeight());
        product.setDepth(request.getDimension().getDepth());
        product.setWeight(request.getWeight());
        product.setFragile(request.getFragile() != null && request.getFragile());
        product.setQuantity(0L);

        productRepository.save(product);
    }

    @Transactional
    public void addProductToWarehouse(AddProductToWarehouseRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new NoSpecifiedProductInWarehouseException(
                        "Product with ID " + request.getProductId() + " not found in warehouse"));

        product.setQuantity(product.getQuantity() + request.getQuantity());
        productRepository.save(product);
    }

    @Transactional(readOnly = true)
    public void checkProductQuantityEnoughForShoppingCart(ShoppingCartDto cart) {
        if (cart == null || cart.getProducts() == null || cart.getProducts().isEmpty()) {
            throw new IllegalArgumentException("Shopping cart products must not be empty");
        }

        for (Map.Entry<String, Long> entry : cart.getProducts().entrySet()) {
            UUID productId;
            try {
                productId = UUID.fromString(entry.getKey());
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid productId UUID: " + entry.getKey());
            }

            Long requestedQuantity = entry.getValue();
            if (requestedQuantity == null || requestedQuantity <= 0) {
                throw new IllegalArgumentException("Quantity must be positive for productId: " + productId);
            }

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new NoSpecifiedProductInWarehouseException(
                            "Product with ID " + productId + " not found in warehouse"));

            if (product.getQuantity() < requestedQuantity) {
                throw new ProductInShoppingCartLowQuantityInWarehouse(
                        "Insufficient quantity for product ID " + productId + ": requested " +
                                requestedQuantity + ", available " + product.getQuantity());
            }
        }
    }

    @Transactional(readOnly = true)
    public AddressDto getWarehouseAddress() {
        return new AddressDto(CURRENT_ADDRESS, CURRENT_ADDRESS, CURRENT_ADDRESS, CURRENT_ADDRESS, CURRENT_ADDRESS);
    }

    @Transactional(readOnly = true)
    public ProductDto getProduct(UUID productId) {
        productRepository.findById(productId)
                .orElseThrow(() -> new NoSpecifiedProductInWarehouseException(
                        "Product with ID " + productId + " not found in warehouse"));
        ProductDto storeProduct = productClient.getProduct(productId);
        return ProductDto.builder()
                .productId(productId)
                .productName(storeProduct.getProductName())
                .description(storeProduct.getDescription())
                .imageSrc(storeProduct.getImageSrc())
                .productCategory(storeProduct.getProductCategory())
                .quantityState(storeProduct.getQuantityState())
                .productState(storeProduct.getProductState())
                .price(storeProduct.getPrice())
                .build();
    }

    @Transactional
    public void assemblyProductForOrderFromShoppingCart(Map<UUID, Long> products, UUID orderId) {
        if (products == null || products.isEmpty()) {
            throw new IllegalArgumentException("Products map cannot be null or empty");
        }
        if (orderId == null) {
            throw new IllegalArgumentException("Order ID cannot be null");
        }
        for (Map.Entry<UUID, Long> entry : products.entrySet()) {
            Product product = productRepository.findById(entry.getKey())
                    .orElseThrow(() -> new NoSpecifiedProductInWarehouseException(
                            "Product with ID " + entry.getKey() + " not found in warehouse"));
            if (product.getQuantity() < entry.getValue()) {
                throw new ProductInShoppingCartLowQuantityInWarehouse(
                        "Insufficient quantity for product ID " + entry.getKey() + ": requested " +
                                entry.getValue() + ", available " + product.getQuantity());
            }
            product.setQuantity(product.getQuantity() - entry.getValue());
            productRepository.save(product);
        }
    }

    @Transactional
    public void returnProduct(Map<UUID, Long> products) {
        for (Map.Entry<UUID, Long> entry : products.entrySet()) {
            Product product = productRepository.findById(entry.getKey())
                    .orElseThrow(() -> new NoSpecifiedProductInWarehouseException(
                            "Product with ID " + entry.getKey() + " not found in warehouse"));
            product.setQuantity(product.getQuantity() + entry.getValue());
            productRepository.save(product);
        }
    }
}