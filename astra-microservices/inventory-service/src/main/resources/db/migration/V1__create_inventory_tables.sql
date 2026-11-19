
CREATE TABLE inventory_stock (
    id BIGINT NOT NULL AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    available_quantity INT NOT NULL DEFAULT 0,
    reserved_quantity INT NOT NULL DEFAULT 0,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    CONSTRAINT uk_inventory_product UNIQUE (product_id),
    CONSTRAINT chk_available_quantity
        CHECK (available_quantity >= 0),
    CONSTRAINT chk_reserved_quantity
        CHECK (reserved_quantity >= 0)
);

CREATE TABLE stock_reservations (
    id BIGINT NOT NULL AUTO_INCREMENT,
    order_number VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL,
    expires_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    CONSTRAINT uk_reservation_order UNIQUE (order_number)
);

CREATE TABLE stock_reservation_items (
    id BIGINT NOT NULL AUTO_INCREMENT,
    reservation_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,

    PRIMARY KEY (id),
    CONSTRAINT fk_reservation_items_reservation
        FOREIGN KEY (reservation_id)
        REFERENCES stock_reservations(id)
        ON DELETE CASCADE,
    CONSTRAINT chk_reservation_item_quantity
        CHECK (quantity > 0),
    CONSTRAINT uk_reservation_product
        UNIQUE (reservation_id, product_id)
);

CREATE INDEX idx_reservation_status
    ON stock_reservations(status);

CREATE INDEX idx_reservation_expiry
    ON stock_reservations(status, expires_at);
