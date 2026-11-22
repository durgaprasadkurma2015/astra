
CREATE TABLE password_reset_otp_requests (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    otp_hash VARCHAR(64) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    attempts INT NOT NULL DEFAULT 0,
    used_at DATETIME(6) NULL,
    last_sent_at DATETIME(6) NOT NULL,
    created_at DATETIME(6) NOT NULL,

    PRIMARY KEY (id),

    INDEX idx_password_reset_user (user_id),
    INDEX idx_password_reset_created (created_at),

    CONSTRAINT fk_password_reset_user
        FOREIGN KEY (user_id)
        REFERENCES app_users(id)
);
