
CREATE TABLE payments (
    id BIGINT NOT NULL AUTO_INCREMENT,
    payment_reference VARCHAR(100) NOT NULL,
    order_number VARCHAR(100) NOT NULL,
    customer_email VARCHAR(255) NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'INR',
    payment_method VARCHAR(30) NOT NULL,
    payment_status VARCHAR(30) NOT NULL,
    idempotency_key VARCHAR(100) NOT NULL,
    gateway_transaction_id VARCHAR(150),
    failure_reason VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    CONSTRAINT uk_payment_reference UNIQUE (payment_reference),
    CONSTRAINT uk_payment_idempotency UNIQUE (customer_email, idempotency_key),
    INDEX idx_payment_order (order_number),
    INDEX idx_payment_customer (customer_email),

    CONSTRAINT chk_payment_amount CHECK (amount > 0)
);

CREATE TABLE payment_events (
    id BIGINT NOT NULL AUTO_INCREMENT,
    event_id VARCHAR(100) NOT NULL,
    payment_id BIGINT NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    topic VARCHAR(100) NOT NULL,
    payload JSON NOT NULL,
    event_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    retry_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    published_at TIMESTAMP NULL,

    PRIMARY KEY (id),
    CONSTRAINT uk_payment_event_id UNIQUE (event_id),
    INDEX idx_payment_event_status_created (event_status, created_at),

    CONSTRAINT fk_payment_event_payment
        FOREIGN KEY (payment_id) REFERENCES payments(id)
);
