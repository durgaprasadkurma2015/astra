-- V16__seller.sql

CREATE TABLE sellers (
    id BIGINT NOT NULL AUTO_INCREMENT,

    user_id BIGINT NOT NULL,

    store_name VARCHAR(150) NOT NULL,

    store_slug VARCHAR(180) NOT NULL,

    description VARCHAR(2000),

    phone VARCHAR(30),

    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),

    CONSTRAINT uk_seller_user
        UNIQUE (user_id),

    CONSTRAINT uk_seller_store_slug
        UNIQUE (store_slug),

    CONSTRAINT fk_seller_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_seller_status
    ON sellers(status);

CREATE INDEX idx_seller_user
    ON sellers(user_id);
