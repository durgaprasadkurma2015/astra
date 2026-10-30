-- V9__seed_products_with_images.sql
-- Insert missing products with image URLs.
-- Existing product names are skipped.

INSERT INTO products
    (name, description, category, image_url, price, stock_quantity, active)
SELECT
    seed.name,
    seed.description,
    seed.category,
    seed.image_url,
    seed.price,
    seed.stock_quantity,
    TRUE
FROM (
    SELECT
        'Wireless Headphones' AS name,
        'Wireless headphones for everyday listening' AS description,
        'Electronics' AS category,
        'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=600' AS image_url,
        1999.00 AS price,
        25 AS stock_quantity

    UNION ALL SELECT
        'Smart Watch',
        'Smart watch for everyday use',
        'Electronics',
        'https://images.unsplash.com/photo-1546868871-7041f2a55e12?w=600',
        2499.00, 20

    UNION ALL SELECT
        'Travel Backpack',
        'Spacious backpack for work and travel',
        'Accessories',
        'https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=600',
        1299.00, 30

    UNION ALL SELECT
        'Portable Speaker',
        'Compact speaker for music on the go',
        'Electronics',
        'https://images.unsplash.com/photo-1608043152269-423dbba4e7e1?w=600',
        1599.00, 15

    UNION ALL SELECT
        'Classic T-Shirt',
        'Comfortable everyday T-shirt',
        'Fashion',
        'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=600',
        599.00, 50

    UNION ALL SELECT
        'Water Bottle',
        'Reusable bottle for everyday hydration',
        'Accessories',
        'https://images.unsplash.com/photo-1602143407151-7111542de6e8?w=600',
        399.00, 40

    UNION ALL SELECT
        'Running Shoes',
        'Lightweight running shoes',
        'Footwear',
        'https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=600',
        2499.00, 30

    UNION ALL SELECT
        'Classic Watch',
        'Stylish everyday wrist watch',
        'Accessories',
        'https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=600',
        1499.00, 40

    UNION ALL SELECT
        'Digital Camera',
        'Compact digital camera',
        'Electronics',
        'https://images.unsplash.com/photo-1526170375885-4d8ecf77b99f?w=600',
        4999.00, 15

    UNION ALL SELECT
        'Modern Chair',
        'Minimal modern chair',
        'Home & Kitchen',
        'https://images.unsplash.com/photo-1503602642458-232111445657?w=600',
        3499.00, 10

    UNION ALL SELECT
        'Perfume',
        'Premium fragrance',
        'Beauty',
        'https://images.unsplash.com/photo-1541643600914-78b084683601?w=600',
        999.00, 45

    UNION ALL SELECT
        'Fashion Jacket',
        'Stylish casual jacket',
        'Fashion',
        'https://images.unsplash.com/photo-1523381210434-271e8be1f52b?w=600',
        1999.00, 25

    UNION ALL SELECT
        'Sunglasses',
        'Classic sunglasses',
        'Accessories',
        'https://images.unsplash.com/photo-1572635196237-14b3f281503f?w=600',
        799.00, 60

    UNION ALL SELECT
        'Headset',
        'Over-ear audio headset',
        'Electronics',
        'https://images.unsplash.com/photo-1583394838336-acd977736f90?w=600',
        1599.00, 25

    UNION ALL SELECT
        'Sports Shoes',
        'Comfortable sports shoes',
        'Footwear',
        'https://images.unsplash.com/photo-1491553895911-0055eca6402d?w=600',
        2199.00, 30

    UNION ALL SELECT
        'Laptop',
        'Laptop for everyday work',
        'Electronics',
        'https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=600',
        45999.00, 10

    UNION ALL SELECT
        'Smart Watch Pro',
        'Advanced smartwatch',
        'Electronics',
        'https://images.unsplash.com/photo-1546868871-7041f2a55e12?w=600',
        3999.00, 15

    UNION ALL SELECT
        'Camera Pro',
        'Digital camera for photography',
        'Electronics',
        'https://images.unsplash.com/photo-1526170375885-4d8ecf77b99f?w=600',
        8999.00, 8

    UNION ALL SELECT
        'Cotton T-Shirt',
        'Everyday cotton T-shirt',
        'Fashion',
        'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=600',
        499.00, 75

    UNION ALL SELECT
        'Travel Bag',
        'Spacious travel bag',
        'Accessories',
        'https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=600',
        1599.00, 20
) AS seed
WHERE NOT EXISTS (
    SELECT 1
    FROM products p
    WHERE p.name = seed.name
);