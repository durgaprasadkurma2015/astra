-- V14__recommendations.sql

CREATE TABLE recommendation_events (
    id BIGINT NOT NULL AUTO_INCREMENT,

    user_id BIGINT NULL,

    product_id BIGINT NOT NULL,

    event_type VARCHAR(40) NOT NULL,

    quantity INT NOT NULL DEFAULT 1,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),

    CONSTRAINT fk_recommendation_event_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE SET NULL,

    CONSTRAINT fk_recommendation_event_product
        FOREIGN KEY (product_id)
        REFERENCES products(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_rec_event_user
    ON recommendation_events(user_id);

CREATE INDEX idx_rec_event_product
    ON recommendation_events(product_id);

CREATE INDEX idx_rec_event_type
    ON recommendation_events(event_type);

CREATE INDEX idx_rec_event_created
    ON recommendation_events(created_at);

CREATE INDEX idx_rec_event_user_created
    ON recommendation_events(user_id, created_at);
