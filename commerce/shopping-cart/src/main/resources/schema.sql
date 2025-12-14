CREATE SCHEMA IF NOT EXISTS shopping_cart;

DROP TABLE IF EXISTS shopping_cart.cart_items;
DROP TABLE IF EXISTS shopping_cart.shopping_cart;

CREATE TABLE IF NOT EXISTS shopping_cart.shopping_cart (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(255) NOT NULL,
    cart_status VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS shopping_cart.cart_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cart_id UUID NOT NULL REFERENCES shopping_cart.shopping_cart(id) ON DELETE CASCADE,
    product_id UUID NOT NULL,
    quantity BIGINT NOT NULL
);