package ru.yandex.practicum.commerce.payment.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import ru.yandex.practicum.commerce.interaction_api.enums.PaymentStatus;

import java.util.UUID;

@Entity
@Table(name = "payments", schema = "payment")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @Column(name = "order_id", nullable = false)
    UUID orderId;

    @Column(name = "total_payment")
    Double totalPayment;

    @Column(name = "delivery_total")
    Double deliveryTotal;

    @Column(name = "fee_total")
    Double feeTotal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    PaymentStatus status;
}