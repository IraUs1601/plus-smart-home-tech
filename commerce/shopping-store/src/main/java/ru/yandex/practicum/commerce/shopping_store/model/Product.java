package ru.yandex.practicum.commerce.shopping_store.model;

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
import ru.yandex.practicum.commerce.interaction_api.enums.Availability;
import ru.yandex.practicum.commerce.interaction_api.enums.Category;
import ru.yandex.practicum.commerce.interaction_api.enums.Status;

import java.util.UUID;

@Entity
@Table(name = "products", schema = "shopping_store")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @Column(name = "product_name", nullable = false)
    String productName;

    @Column
    String description;

    @Column(name = "image_src")
    String imageSrc;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_category", nullable = false)
    Category category;

    @Enumerated(EnumType.STRING)
    @Column(name = "quantity_state", nullable = false)
    Availability availability;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_state", nullable = false)
    Status status;

    @Column(nullable = false)
    Double price;
}