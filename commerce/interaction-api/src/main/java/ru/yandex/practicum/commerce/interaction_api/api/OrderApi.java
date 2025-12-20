package ru.yandex.practicum.commerce.interaction_api.api;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.commerce.interaction_api.dto.OrderDto;
import ru.yandex.practicum.commerce.interaction_api.enums.OrderState;
import ru.yandex.practicum.commerce.interaction_api.requests.CreateNewOrderRequest;
import ru.yandex.practicum.commerce.interaction_api.requests.ProductReturnRequest;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface OrderApi {

    @GetMapping("/api/v1/order")
    List<OrderDto> getClientOrders(@RequestParam("username") String username);

    @PutMapping("/api/v1/order")
    OrderDto createNewOrder(@Valid @RequestBody CreateNewOrderRequest request);

    @PostMapping("/api/v1/order/return")
    OrderDto productReturn(@Valid @RequestBody ProductReturnRequest productReturnRequest);

    @PostMapping("/api/v1/order/payment")
    OrderDto payment(@RequestBody Map<String, UUID> request);

    @PostMapping("/api/v1/order/payment/failed")
    OrderDto paymentFailed(@RequestBody UUID orderId);

    @PostMapping("/api/v1/order/delivery")
    OrderDto delivery(@RequestBody Map<String, UUID> request);

    @PostMapping("/api/v1/order/delivery/failed")
    OrderDto deliveryFailed(@RequestBody UUID orderId);

    @PostMapping("/api/v1/order/completed")
    OrderDto complete(@RequestBody UUID orderId);

    @PostMapping("/api/v1/order/calculate/total")
    OrderDto calculateTotalCost(@RequestBody UUID orderId);

    @PostMapping("/api/v1/order/calculate/delivery")
    OrderDto calculateDeliveryCost(@RequestBody UUID orderId);

    @PostMapping("/api/v1/order/assembly")
    OrderDto assembly(@RequestBody Map<String, UUID> request);

    @PostMapping("/api/v1/order/assembly/failed")
    OrderDto assemblyFailed(@RequestBody UUID orderId);

    @PostMapping("/api/v1/order/updatePaymentStatus")
    OrderDto updatePaymentStatus(@RequestBody UUID orderId, @RequestParam("status") OrderState status);
}