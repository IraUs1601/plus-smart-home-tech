package ru.yandex.practicum.commerce.warehouse.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Entity
@Table(name = "products", schema = "warehouse")
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class Product {
    @Id
    @Column(name = "product_id")
    UUID productId;

    @Column(nullable = false)
    Double width;

    @Column(nullable = false)
    Double height;

    @Column(nullable = false)
    Double depth;

    @Column(nullable = false)
    Double weight;

    @Column(nullable = false)
    Boolean fragile;

    @Column(nullable = false)
    Long quantity;
}