-- V2__authentication.sql

CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT,

    name VARCHAR(120) NOT NULL,

    email VARCHAR(190) NOT NULL,

    phone VARCHAR(30),

    password VARCHAR(100),

    role VARCHAR(30) NOT NULL DEFAULT 'CUSTOMER',

    enabled BOOLEAN NOT NULL DEFAULT TRUE,

    email_verified BOOLEAN NOT NULL DEFAULT FALSE,

    phone_verified BOOLEAN NOT NULL DEFAULT FALSE,

    google_subject VARCHAR(255),

    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),

    PRIMARY KEY (id),

    UNIQUE KEY uk_users_email (email),

    UNIQUE KEY uk_users_phone (phone),

    UNIQUE KEY uk_users_google_subject (google_subject),

    KEY idx_users_email (email),

    KEY idx_users_phone (phone)
) ENGINE=InnoDB;


CREATE TABLE refresh_tokens (
    id BIGINT NOT NULL AUTO_INCREMENT,

    user_id BIGINT NOT NULL,

    token VARCHAR(500) NOT NULL,

    expires_at DATETIME(6) NOT NULL,

    revoked BOOLEAN NOT NULL DEFAULT FALSE,

    created_at DATETIME(6) NOT NULL,

    PRIMARY KEY (id),

    UNIQUE KEY uk_refresh_token (token),

    KEY idx_refresh_user (user_id),

    CONSTRAINT fk_refresh_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
) ENGINE=InnoDB;


CREATE TABLE otp_verifications (
    id BIGINT NOT NULL AUTO_INCREMENT,

    user_id BIGINT NULL,

    identifier VARCHAR(190) NOT NULL,

    otp VARCHAR(20) NOT NULL,

    purpose VARCHAR(40) NOT NULL,

    expires_at DATETIME(6) NOT NULL,

    verified BOOLEAN NOT NULL DEFAULT FALSE,

    created_at DATETIME(6) NOT NULL,

    PRIMARY KEY (id),

    KEY idx_otp_identifier (identifier),

    KEY idx_otp_user (user_id),

    KEY idx_otp_purpose (purpose),

    CONSTRAINT fk_otp_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
) ENGINE=InnoDB;


CREATE TABLE password_reset_tokens (
    id BIGINT NOT NULL AUTO_INCREMENT,

    user_id BIGINT NOT NULL,

    token VARCHAR(500) NOT NULL,

    expires_at DATETIME(6) NOT NULL,

    used BOOLEAN NOT NULL DEFAULT FALSE,

    created_at DATETIME(6) NOT NULL,

    PRIMARY KEY (id),

    UNIQUE KEY uk_password_reset_token (token),

    KEY idx_password_reset_user (user_id),

    CONSTRAINT fk_password_reset_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
) ENGINE=InnoDB;
