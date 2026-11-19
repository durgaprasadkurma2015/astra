
-- 1. Warehouses
CREATE TABLE warehouses (
    id BIGINT NOT NULL AUTO_INCREMENT,
    warehouse_code VARCHAR(40) NOT NULL,
    warehouse_name VARCHAR(150) NOT NULL,
    address VARCHAR(500),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_warehouse_code (warehouse_code)
);

-- 2. Warehouse stock
CREATE TABLE warehouse_stock (
    id BIGINT NOT NULL AUTO_INCREMENT,
    warehouse_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    on_hand_quantity INT NOT NULL DEFAULT 0,
    reserved_quantity INT NOT NULL DEFAULT 0,
    reorder_level INT NOT NULL DEFAULT 5,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_warehouse_product (warehouse_id, product_id),
    CONSTRAINT fk_ws_warehouse
        FOREIGN KEY (warehouse_id) REFERENCES warehouses(id),
    CONSTRAINT fk_ws_product
        FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT chk_ws_on_hand CHECK (on_hand_quantity >= 0),
    CONSTRAINT chk_ws_reserved CHECK (
        reserved_quantity >= 0
        AND reserved_quantity <= on_hand_quantity
    ),
    INDEX idx_ws_product (product_id)
);

-- 3. Reservations
CREATE TABLE inventory_reservations (
    id VARCHAR(36) NOT NULL,
    reference_type VARCHAR(30) NOT NULL,
    reference_id VARCHAR(100) NOT NULL,
    customer_email VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_reservation_reference (
        reference_type, reference_id
    ),
    INDEX idx_reservation_expiry (status, expires_at),
    CONSTRAINT chk_reservation_status CHECK (
        status IN ('ACTIVE', 'COMMITTED', 'RELEASED', 'EXPIRED')
    )
);

-- 4. Reservation items
CREATE TABLE inventory_reservation_items (
    id BIGINT NOT NULL AUTO_INCREMENT,
    reservation_id VARCHAR(36) NOT NULL,
    warehouse_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_ri_reservation
        FOREIGN KEY (reservation_id)
        REFERENCES inventory_reservations(id),
    CONSTRAINT fk_ri_warehouse
        FOREIGN KEY (warehouse_id) REFERENCES warehouses(id),
    CONSTRAINT fk_ri_product
        FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT chk_ri_quantity CHECK (quantity > 0),
    UNIQUE KEY uk_reservation_product (
        reservation_id, product_id
    )
);

-- 5. Warehouse transfers
CREATE TABLE warehouse_transfers (
    id BIGINT NOT NULL AUTO_INCREMENT,
    transfer_number VARCHAR(50) NOT NULL,
    product_id BIGINT NOT NULL,
    source_warehouse_id BIGINT NOT NULL,
    destination_warehouse_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED',
    actor_email VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_transfer_number (transfer_number),
    CONSTRAINT fk_wt_product
        FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT fk_wt_source
        FOREIGN KEY (source_warehouse_id) REFERENCES warehouses(id),
    CONSTRAINT fk_wt_destination
        FOREIGN KEY (destination_warehouse_id) REFERENCES warehouses(id),
    CONSTRAINT chk_wt_quantity CHECK (quantity > 0),
    CONSTRAINT chk_wt_different_warehouses CHECK (
        source_warehouse_id <> destination_warehouse_id
    )
);

-- 6. Create default warehouse
INSERT INTO warehouses (
    warehouse_code,
    warehouse_name,
    address
)
VALUES (
    'ASTRA-MAIN',
    'Astra Main Warehouse',
    'Main Distribution Center'
);

-- 7. Migrate existing product stock into default warehouse.
INSERT INTO warehouse_stock (
    warehouse_id,
    product_id,
    on_hand_quantity,
    reserved_quantity,
    reorder_level
)
SELECT
    1,
    id,
    stock_quantity,
    0,
    5
FROM products;
