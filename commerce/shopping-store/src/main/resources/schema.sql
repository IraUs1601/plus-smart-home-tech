CREATE SCHEMA IF NOT EXISTS shopping_store;

DROP TABLE IF EXISTS shopping_store.products;

CREATE TABLE IF NOT EXISTS shopping_store.products (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_name VARCHAR(255) NOT NULL,
    description TEXT,
    image_src VARCHAR(1000),
    product_category VARCHAR(50) NOT NULL,
    quantity_state VARCHAR(50) NOT NULL,
    product_state VARCHAR(50) NOT NULL,
    price DOUBLE PRECISION NOT NULL
);