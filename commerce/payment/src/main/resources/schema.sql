CREATE SCHEMA IF NOT EXISTS payment;

DROP TABLE IF EXISTS payment.payments;

CREATE TABLE payment.payments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL,
    total_payment DOUBLE PRECISION NOT NULL,
    delivery_total DOUBLE PRECISION,
    fee_total DOUBLE PRECISION,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);