CREATE TABLE coupons (
    id BIGINT NOT NULL AUTO_INCREMENT,
    code VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    discount_type VARCHAR(20) NOT NULL,
    discount_value DECIMAL(12,2) NOT NULL,
    minimum_order_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    maximum_discount_amount DECIMAL(12,2),
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
    CONSTRAINT chk_coupon_dates
        CHECK (expires_at > starts_at)
);