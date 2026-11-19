CREATE TABLE cart_items (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_email VARCHAR(255) NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),

    CONSTRAINT uq_cart_user_product
        UNIQUE (user_email, product_id),

    CONSTRAINT chk_cart_quantity
        CHECK (quantity > 0)
);

CREATE INDEX idx_cart_user_email
    ON cart_items(user_email);