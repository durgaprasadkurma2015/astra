
CREATE TABLE wishlist_items (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_email VARCHAR(255) NOT NULL,
    product_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),

    CONSTRAINT uk_wishlist_user_product
        UNIQUE (user_email, product_id),

    INDEX idx_wishlist_user_email (user_email),
    INDEX idx_wishlist_product_id (product_id)
);
