ALTER TABLE customer_orders
ADD COLUMN stock_status VARCHAR(30) NOT NULL DEFAULT 'PENDING';

CREATE TABLE processed_payment_events (
    id BIGINT NOT NULL AUTO_INCREMENT,
    event_id VARCHAR(100) NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    order_number VARCHAR(40) NOT NULL,
    processed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    CONSTRAINT uk_processed_payment_event UNIQUE (event_id),
    INDEX idx_processed_payment_order (order_number)
);