-- V12__shipping.sql

CREATE TABLE shipments (
    id BIGINT NOT NULL AUTO_INCREMENT,

    order_id BIGINT NOT NULL,

    user_id BIGINT NOT NULL,

    tracking_number VARCHAR(100) NOT NULL,

    carrier VARCHAR(100),

    status VARCHAR(40) NOT NULL,

    shipping_full_name VARCHAR(255) NOT NULL,

    shipping_phone VARCHAR(255) NOT NULL,

    shipping_address_line1 VARCHAR(255) NOT NULL,

    shipping_address_line2 VARCHAR(255),

    shipping_city VARCHAR(255) NOT NULL,

    shipping_state VARCHAR(255) NOT NULL,

    shipping_postal_code VARCHAR(255) NOT NULL,

    shipping_country VARCHAR(255) NOT NULL,

    estimated_delivery_date DATE,

    actual_delivery_date DATE,

    shipped_at DATETIME(6),

    delivered_at DATETIME(6),

    current_location VARCHAR(500),

    delivery_note VARCHAR(500),

    created_at DATETIME(6) NOT NULL,

    updated_at DATETIME(6) NOT NULL,

    PRIMARY KEY (id),

    UNIQUE KEY uk_shipment_order (order_id),

    UNIQUE KEY uk_shipment_tracking (tracking_number),

    KEY idx_shipment_user (user_id),

    KEY idx_shipment_status (status),

    KEY idx_shipment_tracking (tracking_number),

    CONSTRAINT fk_shipment_order
        FOREIGN KEY (order_id)
        REFERENCES orders(id),

    CONSTRAINT fk_shipment_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
) ENGINE=InnoDB;


CREATE TABLE shipment_tracking (
    id BIGINT NOT NULL AUTO_INCREMENT,

    shipment_id BIGINT NOT NULL,

    status VARCHAR(40) NOT NULL,

    location VARCHAR(500),

    description VARCHAR(1000),

    tracked_at DATETIME(6) NOT NULL,

    PRIMARY KEY (id),

    KEY idx_shipment_tracking_shipment (shipment_id),

    KEY idx_shipment_tracking_time (tracked_at),

    CONSTRAINT fk_shipment_tracking_shipment
        FOREIGN KEY (shipment_id)
        REFERENCES shipments(id)
        ON DELETE CASCADE
) ENGINE=InnoDB;
