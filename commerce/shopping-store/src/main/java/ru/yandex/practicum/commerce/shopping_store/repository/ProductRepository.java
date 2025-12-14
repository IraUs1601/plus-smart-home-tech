package ru.yandex.practicum.commerce.shopping_store.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.commerce.interaction_api.enums.Category;
import ru.yandex.practicum.commerce.shopping_store.model.Product;

import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {
    Page<Product> findByCategory(Category category, Pageable pageable);
}