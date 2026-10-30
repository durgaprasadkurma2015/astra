CREATE TABLE password_reset_requests (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    otp_hash VARCHAR(64) NOT NULL,
    reset_token_hash VARCHAR(64) NULL,
    expires_at TIMESTAMP NOT NULL,
    attempts INT NOT NULL DEFAULT 0,
    verified_at TIMESTAMP NULL,
    used_at TIMESTAMP NULL,
    last_sent_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_password_reset_requests
        PRIMARY KEY (id),

    CONSTRAINT fk_password_reset_user
        FOREIGN KEY (user_id)
        REFERENCES app_users(id),

    INDEX idx_password_reset_user (user_id),
    INDEX idx_password_reset_expiry (expires_at)
);