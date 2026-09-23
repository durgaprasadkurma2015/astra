-- V9__orders.sql

CREATE TABLE orders (
    id BIGINT NOT NULL AUTO_INCREMENT,

    user_id BIGINT NOT NULL,

    order_number VARCHAR(100) NOT NULL,

    status VARCHAR(40) NOT NULL,

    payment_status VARCHAR(40) NOT NULL,

    subtotal DECIMAL(19,2) NOT NULL,

    discount DECIMAL(19,2) NOT NULL DEFAULT 0.00,

    shipping_cost DECIMAL(19,2) NOT NULL DEFAULT 0.00,

    tax DECIMAL(19,2) NOT NULL DEFAULT 0.00,

    total DECIMAL(19,2) NOT NULL,

    created_at DATETIME(6) NOT NULL,

    updated_at DATETIME(6),

    PRIMARY KEY (id),

    UNIQUE KEY uk_order_number (order_number),

    KEY idx_order_user (user_id),

    KEY idx_order_status (status),

    KEY idx_order_payment_status (payment_status),

    CONSTRAINT fk_order_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
) ENGINE=InnoDB;


CREATE TABLE order_items (
    id BIGINT NOT NULL AUTO_INCREMENT,

    order_id BIGINT NOT NULL,

    product_id BIGINT NOT NULL,

    product_name VARCHAR(255) NOT NULL,

    product_slug VARCHAR(150) NOT NULL,

    thumbnail_url VARCHAR(500),

    quantity INT NOT NULL,

    unit_price DECIMAL(19,2) NOT NULL,

    line_total DECIMAL(19,2) NOT NULL,

    PRIMARY KEY (id),

    KEY idx_order_item_order (order_id),

    KEY idx_order_item_product (product_id),

    CONSTRAINT fk_order_item_order
        FOREIGN KEY (order_id)
        REFERENCES orders(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_order_item_product
        FOREIGN KEY (product_id)
        REFERENCES products(id)
) ENGINE=InnoDB;
