package ru.yandex.practicum.commerce.payment.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.commerce.interaction_api.dto.PaymentDto;
import ru.yandex.practicum.commerce.interaction_api.enums.PaymentStatus;
import ru.yandex.practicum.commerce.payment.model.Payment;

import java.util.UUID;

@Component
public class PaymentMapper {
    public PaymentDto toDto(Payment payment) {
        return PaymentDto.builder()
                .paymentId(payment.getId())
                .totalPayment(payment.getTotalPayment())
                .deliveryTotal(payment.getDeliveryTotal())
                .feeTotal(payment.getFeeTotal())
                .build();
    }

    public Payment toEntity(PaymentDto dto, UUID orderId, PaymentStatus status) {
        return Payment.builder()
                .id(dto.getPaymentId())
                .orderId(orderId)
                .totalPayment(dto.getTotalPayment())
                .deliveryTotal(dto.getDeliveryTotal())
                .feeTotal(dto.getFeeTotal())
                .status(status)
                .build();
    }
}