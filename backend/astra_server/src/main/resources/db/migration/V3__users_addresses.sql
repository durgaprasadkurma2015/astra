-- V3__users_addresses.sql

CREATE TABLE addresses (
    id BIGINT NOT NULL AUTO_INCREMENT,

    user_id BIGINT NOT NULL,

    full_name VARCHAR(255) NOT NULL,

    phone VARCHAR(255) NOT NULL,

    address_line1 VARCHAR(500) NOT NULL,

    address_line2 VARCHAR(500),

    city VARCHAR(255) NOT NULL,

    state VARCHAR(255) NOT NULL,

    postal_code VARCHAR(255) NOT NULL,

    country VARCHAR(255) NOT NULL,

    default_address BOOLEAN NOT NULL DEFAULT FALSE,

    created_at DATETIME(6) NOT NULL,

    updated_at DATETIME(6),

    PRIMARY KEY (id),

    KEY idx_address_user (user_id),

    CONSTRAINT fk_address_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
) ENGINE=InnoDB;
