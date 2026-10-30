-- V8__seed_product_images.sql

UPDATE products
SET image_url = CASE id
    WHEN 1 THEN 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=600'
    WHEN 2 THEN 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=600'
    WHEN 3 THEN 'https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=600'
    WHEN 4 THEN 'https://images.unsplash.com/photo-1608043152269-423dbba4e7e1?w=600'
    WHEN 5 THEN 'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=600'
    WHEN 6 THEN 'https://images.unsplash.com/photo-1602143407151-7111542de6e8?w=600'
    WHEN 7 THEN 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=600'
    WHEN 8 THEN 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=600'
    WHEN 9 THEN 'https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=600'
    WHEN 10 THEN 'https://images.unsplash.com/photo-1608043152269-423dbba4e7e1?w=600'
    WHEN 11 THEN 'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=600'
    WHEN 12 THEN 'https://images.unsplash.com/photo-1602143407151-7111542de6e8?w=600'
    WHEN 13 THEN 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=600'
    WHEN 14 THEN 'https://images.unsplash.com/photo-1546868871-7041f2a55e12?w=600'
    WHEN 15 THEN 'https://images.unsplash.com/photo-1526170375885-4d8ecf77b99f?w=600'
    WHEN 16 THEN 'https://images.unsplash.com/photo-1503602642458-232111445657?w=600'
    WHEN 17 THEN 'https://images.unsplash.com/photo-1541643600914-78b084683601?w=600'
    WHEN 18 THEN 'https://images.unsplash.com/photo-1523381210434-271e8be1f52b?w=600'
    WHEN 19 THEN 'https://images.unsplash.com/photo-1572635196237-14b3f281503f?w=600'
    WHEN 20 THEN 'https://images.unsplash.com/photo-1583394838336-acd977736f90?w=600'
    ELSE image_url
END
WHERE id BETWEEN 1 AND 20;