-- Add product catalogue fields.
-- Existing products and data will be preserved.

ALTER TABLE products
    ADD COLUMN sku VARCHAR(100) NULL,
    ADD COLUMN subcategory VARCHAR(100) NULL,
    ADD COLUMN discount_percentage DECIMAL(5,2)
        NOT NULL DEFAULT 0.00;

-- Create a unique SKU index.
-- Multiple NULL values are allowed until existing products
-- have been assigned SKUs.
CREATE UNIQUE INDEX uk_products_sku
    ON products (sku);

-- Store product size/color variants separately.
CREATE TABLE product_variants (
    product_id BIGINT NOT NULL,
    variant_sku VARCHAR(100),
    variant_size VARCHAR(50),
    variant_color VARCHAR(50),
    variant_stock_quantity INT NOT NULL DEFAULT 0,

    CONSTRAINT fk_product_variants_product
        FOREIGN KEY (product_id)
        REFERENCES products(id)
        ON DELETE CASCADE
);