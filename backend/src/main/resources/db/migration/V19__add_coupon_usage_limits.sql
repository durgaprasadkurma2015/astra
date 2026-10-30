ALTER TABLE coupons
    ADD COLUMN usage_limit INT NULL,
    ADD COLUMN per_customer_usage_limit INT NULL,
    ADD COLUMN usage_count INT NOT NULL DEFAULT 0;

CREATE TABLE coupon_redemptions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    coupon_id BIGINT NOT NULL,
    order_id BIGINT NOT NULL,
    user_email VARCHAR(255) NOT NULL,
    discount_amount DECIMAL(12,2) NOT NULL,
    redeemed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_coupon_redemptions_coupon
        FOREIGN KEY (coupon_id) REFERENCES coupons(id),
    CONSTRAINT fk_coupon_redemptions_order
        FOREIGN KEY (order_id) REFERENCES customer_orders(id),
    CONSTRAINT uk_coupon_redemptions_order UNIQUE (order_id),
    INDEX idx_coupon_redemptions_owner (coupon_id, user_email),
    INDEX idx_coupon_redemptions_coupon_date (coupon_id, redeemed_at)
);

INSERT INTO coupon_redemptions
    (coupon_id, order_id, user_email, discount_amount)
SELECT c.id, o.id, o.user_email, o.discount_amount
FROM customer_orders o
JOIN coupons c ON UPPER(c.code) = UPPER(o.coupon_code)
WHERE o.coupon_code IS NOT NULL
  AND TRIM(o.coupon_code) <> '';

UPDATE coupons c
SET usage_count = (
    SELECT COUNT(*)
    FROM coupon_redemptions r
    WHERE r.coupon_id = c.id
);
