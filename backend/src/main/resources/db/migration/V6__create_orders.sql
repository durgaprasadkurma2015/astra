
CREATE TABLE customer_orders (
    id BIGINT NOT NULL AUTO_INCREMENT,
    order_number VARCHAR(40) NOT NULL,
    user_email VARCHAR(255) NOT NULL,
    customer_name VARCHAR(150) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    shipping_address TEXT NOT NULL,
    subtotal DECIMAL(12,2) NOT NULL,
    order_status VARCHAR(30) NOT NULL DEFAULT 'PLACED',
    payment_method VARCHAR(30) NOT NULL DEFAULT 'COD',
    payment_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_customer_orders_number (order_number),
    INDEX idx_customer_orders_email (user_email)
);

CREATE TABLE customer_order_items (
    id BIGINT NOT NULL AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    unit_price DECIMAL(12,2) NOT NULL,
    quantity INT NOT NULL,
    line_total DECIMAL(12,2) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_order_items_order
        FOREIGN KEY (order_id) REFERENCES customer_orders(id),
    CONSTRAINT fk_order_items_product
        FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT chk_order_item_quantity CHECK (quantity > 0)
);
