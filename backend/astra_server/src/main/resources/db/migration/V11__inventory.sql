-- V11__inventory.sql

CREATE TABLE product_inventory (
    id BIGINT NOT NULL AUTO_INCREMENT,

    product_id BIGINT NOT NULL,

    available_quantity INT NOT NULL DEFAULT 0,

    reserved_quantity INT NOT NULL DEFAULT 0,

    version BIGINT,

    created_at DATETIME(6) NOT NULL,

    updated_at DATETIME(6) NOT NULL,

    PRIMARY KEY (id),

    UNIQUE KEY uk_product_inventory_product (product_id),

    CONSTRAINT fk_product_inventory_product
        FOREIGN KEY (product_id)
        REFERENCES products(id)
        ON DELETE CASCADE
) ENGINE=InnoDB;


CREATE TABLE inventory_movements (
    id BIGINT NOT NULL AUTO_INCREMENT,

    product_id BIGINT NOT NULL,

    order_id BIGINT,

    movement_type VARCHAR(30) NOT NULL,

    quantity INT NOT NULL,

    quantity_before INT NOT NULL,

    quantity_after INT NOT NULL,

    reason VARCHAR(500),

    created_at DATETIME(6) NOT NULL,

    PRIMARY KEY (id),

    KEY idx_inventory_movement_product (product_id),

    KEY idx_inventory_movement_order (order_id),

    KEY idx_inventory_movement_type (movement_type),

    KEY idx_inventory_movement_created (created_at),

    CONSTRAINT fk_inventory_movement_product
        FOREIGN KEY (product_id)
        REFERENCES products(id),

    CONSTRAINT fk_inventory_movement_order
        FOREIGN KEY (order_id)
        REFERENCES orders(id)
) ENGINE=InnoDB;
