package ru.yandex.practicum.commerce.warehouse.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.commerce.interaction_api.client.WarehouseClient;
import ru.yandex.practicum.commerce.interaction_api.dto.BookedProductsDto;
import ru.yandex.practicum.commerce.interaction_api.requests.AddProductToWarehouseRequest;
import ru.yandex.practicum.commerce.interaction_api.dto.AddressDto;
import ru.yandex.practicum.commerce.interaction_api.requests.AssemblyProductsForOrderRequest;
import ru.yandex.practicum.commerce.interaction_api.requests.NewProductInWarehouseRequest;
import ru.yandex.practicum.commerce.interaction_api.dto.ProductDto;
import ru.yandex.practicum.commerce.interaction_api.dto.ShoppingCartDto;
import ru.yandex.practicum.commerce.interaction_api.requests.ShippedToDeliveryRequest;
import ru.yandex.practicum.commerce.warehouse.service.WarehouseService;

import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class WarehouseController implements WarehouseClient {
    private final WarehouseService warehouseService;

    @Override
    public void newProductInWarehouse(@Valid @RequestBody NewProductInWarehouseRequest request) {
        warehouseService.newProductInWarehouse(request);
    }

    @Override
    public void addProductToWarehouse(@Valid @RequestBody AddProductToWarehouseRequest request) {
        warehouseService.addProductToWarehouse(request);
    }

    @Override
    public BookedProductsDto checkProductQuantityEnoughForShoppingCart(@Valid @RequestBody ShoppingCartDto cart) {
        return warehouseService.checkProductQuantityEnoughForShoppingCart(cart);
    }

    @Override
    public AddressDto getWarehouseAddress() {
        return warehouseService.getWarehouseAddress();
    }

    @Override
    public ProductDto getProduct(UUID productId) {
        return warehouseService.getProduct(productId);
    }

    @Override
    public void assemblyProductForOrderFromShoppingCart(@Valid @RequestBody AssemblyProductsForOrderRequest request) {
        warehouseService.assemblyProductForOrderFromShoppingCart(request.getProducts(), request.getOrderId());
    }

    @Override
    public void shippedToDelivery(@Valid @RequestBody ShippedToDeliveryRequest request) {
        warehouseService.shippedToDelivery(request.getOrderId(), request.getDeliveryId());
    }

    @Override
    public void returnProduct(@RequestBody Map<UUID, Long> products) {
        warehouseService.returnProduct(products);
    }
}