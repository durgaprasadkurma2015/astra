ALTER TABLE customer_orders
    ADD COLUMN razorpay_order_id VARCHAR(80) NULL,
    ADD COLUMN razorpay_payment_id VARCHAR(80) NULL;

CREATE UNIQUE INDEX uk_customer_orders_razorpay_order
    ON customer_orders (razorpay_order_id);
