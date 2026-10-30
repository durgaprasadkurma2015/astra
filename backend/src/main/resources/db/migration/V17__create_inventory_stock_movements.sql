CREATE TABLE inventory_stock_movements (
    id BIGINT NOT NULL AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    actor_email VARCHAR(255) NOT NULL,
    quantity_delta INT NOT NULL,
    stock_after INT NOT NULL,
    reason VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_inventory_movements_product
        FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT chk_inventory_movement_stock_after CHECK (stock_after >= 0),
    INDEX idx_inventory_movements_product_created (product_id, created_at),
    INDEX idx_inventory_movements_actor_created (actor_email, created_at)
);
