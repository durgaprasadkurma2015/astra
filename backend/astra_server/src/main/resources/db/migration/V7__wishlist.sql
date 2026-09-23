-- V7__wishlist.sql

CREATE TABLE wishlists (
    id BIGINT NOT NULL AUTO_INCREMENT,

    user_id BIGINT NOT NULL,

    created_at DATETIME(6) NOT NULL,

    updated_at DATETIME(6) NOT NULL,

    PRIMARY KEY (id),

    UNIQUE KEY uk_wishlist_user (user_id),

    KEY idx_wishlist_user (user_id),

    CONSTRAINT fk_wishlist_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
) ENGINE=InnoDB;


CREATE TABLE wishlist_items (
    id BIGINT NOT NULL AUTO_INCREMENT,

    wishlist_id BIGINT NOT NULL,

    product_id BIGINT NOT NULL,

    created_at DATETIME(6),

    PRIMARY KEY (id),

    UNIQUE KEY uk_wishlist_item_product (
        wishlist_id,
        product_id
    ),

    KEY idx_wishlist_item_wishlist (wishlist_id),

    KEY idx_wishlist_item_product (product_id),

    CONSTRAINT fk_wishlist_item_wishlist
        FOREIGN KEY (wishlist_id)
        REFERENCES wishlists(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_wishlist_item_product
        FOREIGN KEY (product_id)
        REFERENCES products(id)
        ON DELETE CASCADE
) ENGINE=InnoDB;
