CREATE TABLE revoked_tokens (
    id BIGINT NOT NULL AUTO_INCREMENT,
    token_id VARCHAR(36) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    revoked_at DATETIME(6) NOT NULL,

    PRIMARY KEY (id),

    CONSTRAINT uk_revoked_tokens_token_id
        UNIQUE (token_id),

    INDEX idx_revoked_token_expiry (expires_at)
);