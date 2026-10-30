ALTER TABLE product_reviews
    ADD COLUMN published BOOLEAN NOT NULL DEFAULT TRUE;

CREATE INDEX idx_product_reviews_published_created
    ON product_reviews (published, created_at);
