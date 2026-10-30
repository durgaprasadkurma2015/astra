
ALTER TABLE app_users
ADD COLUMN email_verified BOOLEAN NOT NULL DEFAULT FALSE;

CREATE TABLE registration_otp_requests (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    otp_hash VARCHAR(64) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    attempts INT NOT NULL DEFAULT 0,
    verified_at TIMESTAMP NULL,
    used_at TIMESTAMP NULL,
    last_sent_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_registration_otp_requests PRIMARY KEY (id),
    CONSTRAINT fk_registration_otp_user
        FOREIGN KEY (user_id) REFERENCES app_users(id),
    INDEX idx_registration_otp_user (user_id),
    INDEX idx_registration_otp_expiry (expires_at)
);
