CREATE TABLE coupons (
    id BIGINT NOT NULL AUTO_INCREMENT,
    code VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    discount_type VARCHAR(20) NOT NULL,
    discount_value DECIMAL(12, 2) NOT NULL,
    minimum_order_amount DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    maximum_discount_amount DECIMAL(12, 2),
    starts_at DATETIME NOT NULL,
    expires_at DATETIME NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    CONSTRAINT uk_coupons_code UNIQUE (code),
    CONSTRAINT chk_coupon_discount_type
        CHECK (discount_type IN ('PERCENTAGE', 'FIXED')),
    CONSTRAINT chk_coupon_discount_value
        CHECK (discount_value > 0),
    CONSTRAINT chk_coupon_minimum_amount
        CHECK (minimum_order_amount >= 0),
    CONSTRAINT chk_coupon_maximum_discount
        CHECK (
            maximum_discount_amount IS NULL
            OR maximum_discount_amount > 0
        ),
    CONSTRAINT chk_coupon_dates
        CHECK (expires_at > starts_at)
);

ALTER TABLE customer_orders
    ADD COLUMN coupon_code VARCHAR(50) NULL,
    ADD COLUMN discount_amount DECIMAL(12, 2)
        NOT NULL DEFAULT 0.00,
    ADD COLUMN total_amount DECIMAL(12, 2) NULL;

UPDATE customer_orders
SET total_amount = subtotal
WHERE total_amount IS NULL;

ALTER TABLE customer_orders
    MODIFY COLUMN total_amount DECIMAL(12, 2) NOT NULL;