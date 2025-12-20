package ru.yandex.practicum.commerce.interaction_api.client;

import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.commerce.interaction_api.dto.BookedProductsDto;
import ru.yandex.practicum.commerce.interaction_api.requests.AddProductToWarehouseRequest;
import ru.yandex.practicum.commerce.interaction_api.dto.AddressDto;
import ru.yandex.practicum.commerce.interaction_api.requests.AssemblyProductsForOrderRequest;
import ru.yandex.practicum.commerce.interaction_api.requests.NewProductInWarehouseRequest;
import ru.yandex.practicum.commerce.interaction_api.dto.ProductDto;
import ru.yandex.practicum.commerce.interaction_api.dto.ShoppingCartDto;
import ru.yandex.practicum.commerce.interaction_api.requests.ShippedToDeliveryRequest;

import java.util.Map;
import java.util.UUID;

@FeignClient(name = "warehouse")
public interface WarehouseClient {
    String WAREHOUSE_ENDPOINT = "/api/v1/warehouse";

    @PutMapping(WAREHOUSE_ENDPOINT)
    void newProductInWarehouse(@Valid @RequestBody NewProductInWarehouseRequest request);

    @PostMapping(WAREHOUSE_ENDPOINT + "/add")
    void addProductToWarehouse(@Valid @RequestBody AddProductToWarehouseRequest request);

    @PostMapping(WAREHOUSE_ENDPOINT + "/check")
    BookedProductsDto checkProductQuantityEnoughForShoppingCart(@Valid @RequestBody ShoppingCartDto cart);

    @GetMapping(WAREHOUSE_ENDPOINT + "/address")
    AddressDto getWarehouseAddress();

    @GetMapping(WAREHOUSE_ENDPOINT + "/{productId}")
    ProductDto getProduct(@PathVariable UUID productId);

    @PostMapping(WAREHOUSE_ENDPOINT + "/assembly")
    void assemblyProductForOrderFromShoppingCart(@Valid @RequestBody AssemblyProductsForOrderRequest request);

    @PostMapping(WAREHOUSE_ENDPOINT + "/shipped")
    void shippedToDelivery(@Valid @RequestBody ShippedToDeliveryRequest request);

    @PostMapping(WAREHOUSE_ENDPOINT + "/return")
    void returnProduct(@RequestBody Map<UUID, Long> products);
}