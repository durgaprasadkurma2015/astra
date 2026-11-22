CREATE TABLE notification_events (
    id BIGINT NOT NULL AUTO_INCREMENT,
    event_id VARCHAR(150) NOT NULL,
    event_type VARCHAR(80) NOT NULL,
    recipient_email VARCHAR(255),
    order_number VARCHAR(50),
    subject VARCHAR(255),
    message TEXT,
    delivery_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    failure_reason VARCHAR(1000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    CONSTRAINT uk_notification_event UNIQUE (event_id),
    INDEX idx_notification_recipient (recipient_email),
    INDEX idx_notification_order (order_number),
    INDEX idx_notification_status (delivery_status)
);