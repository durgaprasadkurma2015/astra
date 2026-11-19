
CREATE INDEX idx_customer_orders_created_at
    ON customer_orders (created_at);

CREATE INDEX idx_customer_orders_created_status
    ON customer_orders (created_at, order_status);

CREATE INDEX idx_customer_orders_created_payment
    ON customer_orders (created_at, payment_status);

CREATE INDEX idx_order_items_order_product
    ON customer_order_items (order_id, product_id);

CREATE INDEX idx_app_users_role_created
    ON app_users (role, created_at);

CREATE INDEX idx_products_active_stock
    ON products (active, stock_quantity);
