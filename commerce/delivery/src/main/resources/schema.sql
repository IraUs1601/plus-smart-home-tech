CREATE SCHEMA IF NOT EXISTS delivery;

DROP TABLE IF EXISTS delivery.deliveries;

CREATE TABLE IF NOT EXISTS delivery.deliveries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL,
    from_address JSONB,
    to_address JSONB,
    delivery_volume DOUBLE PRECISION,
    delivery_weight DOUBLE PRECISION,
    fragile BOOLEAN,
    state VARCHAR(50) NOT NULL
    );