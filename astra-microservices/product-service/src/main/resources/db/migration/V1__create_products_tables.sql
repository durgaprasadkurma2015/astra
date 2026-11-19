CREATE TABLE products (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(150) NOT NULL,
    sku VARCHAR(100) NULL,
    description VARCHAR(1000) NULL,
    category VARCHAR(100) NULL,
    subcategory VARCHAR(100) NULL,
    image_url VARCHAR(1000) NULL,
    price DECIMAL(12,2) NOT NULL,
    discount_percentage DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    stock_quantity INT NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),

    CONSTRAINT uk_products_sku UNIQUE (sku),
    CONSTRAINT chk_products_price CHECK (price >= 0),
    CONSTRAINT chk_products_discount
        CHECK (discount_percentage >= 0 AND discount_percentage <= 100),
    CONSTRAINT chk_products_stock CHECK (stock_quantity >= 0)
);

CREATE TABLE product_variants (
    product_id BIGINT NOT NULL,
    variant_sku VARCHAR(100),
    variant_size VARCHAR(50),
    variant_color VARCHAR(50),
    variant_stock_quantity INT NOT NULL DEFAULT 0,

    CONSTRAINT fk_product_variants_product
        FOREIGN KEY (product_id)
        REFERENCES products(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_variant_stock
        CHECK (variant_stock_quantity >= 0)
);

CREATE INDEX idx_products_category
    ON products(category);

CREATE INDEX idx_products_active_created
    ON products(active, created_at);