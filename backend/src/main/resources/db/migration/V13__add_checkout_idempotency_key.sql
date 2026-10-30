ALTER TABLE customer_orders
    ADD COLUMN checkout_idempotency_key VARCHAR(100) NULL;

CREATE UNIQUE INDEX uk_customer_orders_user_checkout_key
    ON customer_orders (user_email, checkout_idempotency_key);