-- V6__cart.sql

CREATE TABLE carts (
    id BIGINT NOT NULL AUTO_INCREMENT,

    user_id BIGINT NOT NULL,

    subtotal DECIMAL(12,2) NOT NULL DEFAULT 0.00,

    discount DECIMAL(12,2) NOT NULL DEFAULT 0.00,

    total DECIMAL(12,2) NOT NULL DEFAULT 0.00,

    created_at DATETIME(6) NOT NULL,

    updated_at DATETIME(6) NOT NULL,

    PRIMARY KEY (id),

    UNIQUE KEY uk_cart_user (user_id),

    KEY idx_cart_user (user_id),

    CONSTRAINT fk_cart_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
) ENGINE=InnoDB;


CREATE TABLE cart_items (
    id BIGINT NOT NULL AUTO_INCREMENT,

    cart_id BIGINT NOT NULL,

    product_id BIGINT NOT NULL,

    quantity INT NOT NULL,

    unit_price DECIMAL(12,2) NOT NULL,

    created_at DATETIME(6),

    updated_at DATETIME(6),

    PRIMARY KEY (id),

    UNIQUE KEY uk_cart_item_product (cart_id, product_id),

    KEY idx_cart_item_cart (cart_id),

    KEY idx_cart_item_product (product_id),

    CONSTRAINT fk_cart_item_cart
        FOREIGN KEY (cart_id)
        REFERENCES carts(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_cart_item_product
        FOREIGN KEY (product_id)
        REFERENCES products(id)
) ENGINE=InnoDB;
