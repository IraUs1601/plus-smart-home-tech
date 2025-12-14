CREATE SCHEMA IF NOT EXISTS warehouse;

DROP TABLE IF EXISTS warehouse.products;

CREATE TABLE IF NOT EXISTS warehouse.products (
    product_id UUID PRIMARY KEY,
    width DOUBLE PRECISION NOT NULL CHECK (width > 0),
    height DOUBLE PRECISION NOT NULL CHECK (height > 0),
    depth DOUBLE PRECISION NOT NULL CHECK (depth > 0),
    weight DOUBLE PRECISION NOT NULL CHECK (weight > 0),
    fragile BOOLEAN NOT NULL,
    quantity BIGINT NOT NULL DEFAULT 0 CHECK (quantity >= 0)
);