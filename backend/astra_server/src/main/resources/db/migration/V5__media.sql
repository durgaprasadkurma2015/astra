-- V5__media.sql

CREATE TABLE product_images (
    id BIGINT NOT NULL AUTO_INCREMENT,

    product_id BIGINT NOT NULL,

    image_url VARCHAR(1000) NOT NULL,

    original_file_name VARCHAR(255),

    stored_file_name VARCHAR(255),

    content_type VARCHAR(100),

    file_size BIGINT,

    primary_image BOOLEAN NOT NULL DEFAULT FALSE,

    display_order INT NOT NULL DEFAULT 0,

    PRIMARY KEY (id),

    KEY idx_product_image_product (product_id),

    KEY idx_product_image_primary (product_id, primary_image),

    KEY idx_product_image_order (product_id, display_order),

    CONSTRAINT fk_product_image_product
        FOREIGN KEY (product_id)
        REFERENCES products(id)
        ON DELETE CASCADE
) ENGINE=InnoDB;
