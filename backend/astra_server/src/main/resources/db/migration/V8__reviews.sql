-- V8__reviews.sql

CREATE TABLE product_reviews (
    id BIGINT NOT NULL AUTO_INCREMENT,

    product_id BIGINT NOT NULL,

    user_id BIGINT NOT NULL,

    rating INT NOT NULL,

    title VARCHAR(200),

    comment VARCHAR(5000),

    verified_purchase BOOLEAN NOT NULL DEFAULT FALSE,

    active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at DATETIME(6) NOT NULL,

    updated_at DATETIME(6) NOT NULL,

    PRIMARY KEY (id),

    UNIQUE KEY uk_review_product_user (
        product_id,
        user_id
    ),

    KEY idx_review_product (
        product_id
    ),

    KEY idx_review_user (
        user_id
    ),

    KEY idx_review_active (
        active
    ),

    CONSTRAINT fk_review_product
        FOREIGN KEY (product_id)
        REFERENCES products(id),

    CONSTRAINT fk_review_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
) ENGINE=InnoDB;
