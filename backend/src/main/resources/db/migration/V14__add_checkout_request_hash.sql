ALTER TABLE customer_orders
    ADD COLUMN checkout_request_hash CHAR(64) NULL;