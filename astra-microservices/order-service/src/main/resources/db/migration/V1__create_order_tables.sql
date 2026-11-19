
CREATE TABLE customer_orders (
    id BIGINT NOT NULL AUTO_INCREMENT,
    order_number VARCHAR(40) NOT NULL,
    checkout_idempotency_key VARCHAR(100) NOT NULL,
    checkout_request_hash VARCHAR(64) NOT NULL,
    user_email VARCHAR(255) NOT NULL,
    customer_name VARCHAR(150) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    shipping_address TEXT NOT NULL,
    subtotal DECIMAL(12,2) NOT NULL,
    coupon_code VARCHAR(50),
    discount_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    total_amount DECIMAL(12,2) NOT NULL,
    order_status VARCHAR(30) NOT NULL DEFAULT 'PENDING_STOCK',
    payment_method VARCHAR(30) NOT NULL DEFAULT 'COD',
    payment_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    CONSTRAINT uk_customer_orders_number UNIQUE (order_number),
    CONSTRAINT uk_order_user_idempotency
        UNIQUE (user_email, checkout_idempotency_key),
    INDEX idx_customer_orders_email (user_email),
    INDEX idx_customer_orders_status (order_status),
    CONSTRAINT chk_order_subtotal CHECK (subtotal >= 0),
    CONSTRAINT chk_order_discount CHECK (discount_amount >= 0),
    CONSTRAINT chk_order_total CHECK (total_amount >= 0)
);

CREATE TABLE customer_order_items (
    id BIGINT NOT NULL AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    unit_price DECIMAL(12,2) NOT NULL,
    quantity INT NOT NULL,
    line_total DECIMAL(12,2) NOT NULL,

    PRIMARY KEY (id),
    CONSTRAINT fk_order_items_order
        FOREIGN KEY (order_id) REFERENCES customer_orders(id),
    INDEX idx_order_items_product (product_id),
    CONSTRAINT chk_order_item_quantity CHECK (quantity > 0)
);

CREATE TABLE order_outbox_events (
    id BIGINT NOT NULL AUTO_INCREMENT,
    event_id VARCHAR(64) NOT NULL,
    aggregate_type VARCHAR(50) NOT NULL,
    aggregate_id VARCHAR(64) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    topic VARCHAR(150) NOT NULL,
    payload JSON NOT NULL,
    event_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    retry_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    published_at TIMESTAMP NULL,

    PRIMARY KEY (id),
    CONSTRAINT uk_order_outbox_event_id UNIQUE (event_id),
    INDEX idx_order_outbox_status_created (event_status, created_at)
);
