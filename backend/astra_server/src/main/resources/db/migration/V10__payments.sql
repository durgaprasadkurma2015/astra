-- V10__payments.sql

CREATE TABLE payment_transactions (
    id BIGINT NOT NULL AUTO_INCREMENT,

    transaction_number VARCHAR(50) NOT NULL,

    order_id BIGINT NOT NULL,

    user_id BIGINT NOT NULL,

    status VARCHAR(30) NOT NULL,

    payment_method VARCHAR(30) NOT NULL,

    amount DECIMAL(19,2) NOT NULL,

    currency VARCHAR(10) NOT NULL DEFAULT 'INR',

    provider_payment_id VARCHAR(150),

    provider_order_id VARCHAR(500),

    failure_reason VARCHAR(1000),

    refund_reference VARCHAR(1000),

    created_at DATETIME(6) NOT NULL,

    updated_at DATETIME(6) NOT NULL,

    PRIMARY KEY (id),

    UNIQUE KEY uk_payment_transaction_number (
        transaction_number
    ),

    UNIQUE KEY uk_payment_provider_payment_id (
        provider_payment_id
    ),

    KEY idx_payment_order (
        order_id
    ),

    KEY idx_payment_user (
        user_id
    ),

    KEY idx_payment_status (
        status
    ),

    KEY idx_payment_provider_id (
        provider_payment_id
    ),

    CONSTRAINT fk_payment_order
        FOREIGN KEY (order_id)
        REFERENCES orders(id),

    CONSTRAINT fk_payment_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
) ENGINE=InnoDB;
