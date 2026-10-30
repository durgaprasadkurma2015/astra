CREATE TABLE refresh_tokens (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_email VARCHAR(255) NOT NULL,
    token_hash CHAR(64) NOT NULL,
    persistent BOOLEAN NOT NULL DEFAULT FALSE,
    expires_at DATETIME NOT NULL,
    revoked_at DATETIME,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_refresh_tokens_hash UNIQUE (token_hash),
    INDEX idx_refresh_tokens_owner_expiry (user_email, expires_at),
    INDEX idx_refresh_tokens_revoked_expiry (revoked_at, expires_at)
);
