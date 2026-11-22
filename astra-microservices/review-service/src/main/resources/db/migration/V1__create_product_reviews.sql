
CREATE TABLE product_reviews (
    id BIGINT NOT NULL AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    user_email VARCHAR(255) NOT NULL,
    customer_name VARCHAR(100) NOT NULL,
    rating INT NOT NULL,
    comment VARCHAR(2000) NOT NULL,
    verified_purchase BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_product_reviews
        PRIMARY KEY (id),

    CONSTRAINT chk_product_reviews_rating
        CHECK (rating BETWEEN 1 AND 5),

    CONSTRAINT uk_product_reviews_product_user
        UNIQUE (product_id, user_email),

    INDEX idx_product_reviews_product_created
        (product_id, created_at)
);
