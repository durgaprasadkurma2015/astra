CREATE TABLE user_notifications (

    id BIGINT NOT NULL AUTO_INCREMENT,

    user_email VARCHAR(255) NOT NULL,

    title VARCHAR(120) NOT NULL,

    message VARCHAR(500) NOT NULL,

    type VARCHAR(40) NOT NULL,

    reference_id VARCHAR(100),

    target_url VARCHAR(500),

    read_at TIMESTAMP NULL,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),

    INDEX idx_notifications_user_created
        (user_email, created_at),

    INDEX idx_notifications_user_unread
        (user_email, read_at)

);