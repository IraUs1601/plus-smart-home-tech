package ru.yandex.practicum.commerce.delivery.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import ru.yandex.practicum.commerce.interaction_api.dto.AddressDto;
import ru.yandex.practicum.commerce.interaction_api.enums.DeliveryState;

import java.util.UUID;

@Entity
@Table(name = "deliveries", schema = "delivery")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Delivery {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @Column(name = "order_id", nullable = false)
    UUID orderId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "from_address", columnDefinition = "jsonb")
    AddressDto fromAddress;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "to_address", columnDefinition = "jsonb")
    AddressDto toAddress;

    @Column(name = "delivery_volume")
    Double deliveryVolume;

    @Column(name = "delivery_weight")
    Double deliveryWeight;

    @Column
    Boolean fragile;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    DeliveryState state;
}