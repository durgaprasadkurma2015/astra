
CREATE TABLE products (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(150) NOT NULL,
    description VARCHAR(1000),
    category VARCHAR(100),
    image_url VARCHAR(1000),
    price DECIMAL(12, 2) NOT NULL,
    stock_quantity INT NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT chk_products_price CHECK (price >= 0),
    CONSTRAINT chk_products_stock CHECK (stock_quantity >= 0)
);

CREATE TABLE cart_items (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_email VARCHAR(255) NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_cart_items_product
        FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT uq_cart_user_product
        UNIQUE (user_email, product_id),
    CONSTRAINT chk_cart_quantity CHECK (quantity > 0)
);

INSERT INTO products
    (name, description, category, image_url, price, stock_quantity)
VALUES
    ('Wireless Headphones', 'Wireless headphones for everyday listening',
     'Electronics', NULL, 1999.00, 25),
    ('Smart Watch', 'Smart watch for everyday use',
     'Electronics', NULL, 2499.00, 20),
    ('Travel Backpack', 'Spacious backpack for work and travel',
     'Accessories', NULL, 1299.00, 30),
    ('Portable Speaker', 'Compact speaker for music on the go',
     'Electronics', NULL, 1599.00, 15),
    ('Classic T-Shirt', 'Comfortable everyday T-shirt',
     'Fashion', NULL, 599.00, 50),
    ('Water Bottle', 'Reusable bottle for everyday hydration',
     'Accessories', NULL, 399.00, 40);
