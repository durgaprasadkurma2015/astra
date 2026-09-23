-- V4__product_catalog.sql

CREATE TABLE categories (
    id BIGINT NOT NULL AUTO_INCREMENT,

    name VARCHAR(150) NOT NULL,

    slug VARCHAR(180) NOT NULL,

    description VARCHAR(500),

    image_url VARCHAR(500),

    active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at DATETIME(6) NOT NULL,

    updated_at DATETIME(6),

    PRIMARY KEY (id),

    UNIQUE KEY uk_category_name (name),

    UNIQUE KEY uk_category_slug (slug),

    KEY idx_category_slug (slug),

    KEY idx_category_active (active)
) ENGINE=InnoDB;


CREATE TABLE products (
    id BIGINT NOT NULL AUTO_INCREMENT,

    name VARCHAR(200) NOT NULL,

    slug VARCHAR(220) NOT NULL,

    sku VARCHAR(100),

    short_description VARCHAR(1000),

    description TEXT,

    price DECIMAL(12,2) NOT NULL,

    discount_price DECIMAL(12,2),

    stock_quantity INT NOT NULL DEFAULT 0,

    active BOOLEAN NOT NULL DEFAULT TRUE,

    featured BOOLEAN NOT NULL DEFAULT FALSE,

    rating DOUBLE NOT NULL DEFAULT 0,

    review_count BIGINT NOT NULL DEFAULT 0,

    sales_count BIGINT NOT NULL DEFAULT 0,

    category_id BIGINT NOT NULL,

    thumbnail_url VARCHAR(500),

    created_at DATETIME(6) NOT NULL,

    updated_at DATETIME(6),

    PRIMARY KEY (id),

    UNIQUE KEY uk_product_slug (slug),

    UNIQUE KEY uk_product_sku (sku),

    KEY idx_product_slug (slug),

    KEY idx_product_category (category_id),

    KEY idx_product_active (active),

    KEY idx_product_featured (featured),

    CONSTRAINT fk_product_category
        FOREIGN KEY (category_id)
        REFERENCES categories(id)
) ENGINE=InnoDB;
