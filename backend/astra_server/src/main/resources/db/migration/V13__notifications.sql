-- V13__notifications.sql

CREATE TABLE notifications (
    id BIGINT NOT NULL AUTO_INCREMENT,

    user_id BIGINT NOT NULL,

    type VARCHAR(30) NOT NULL,

    channel VARCHAR(20) NOT NULL,

    status VARCHAR(20) NOT NULL,

    subject VARCHAR(255) NOT NULL,

    message TEXT NOT NULL,

    recipient VARCHAR(255),

    reference_type VARCHAR(50),

    reference_id BIGINT,

    retry_count INT NOT NULL DEFAULT 0,

    failure_reason VARCHAR(1000),

    sent_at DATETIME(6),

    read_at DATETIME(6),

    created_at DATETIME(6) NOT NULL,

    updated_at DATETIME(6) NOT NULL,

    PRIMARY KEY (id),

    KEY idx_notification_user (
        user_id
    ),

    KEY idx_notification_status (
        status
    ),

    KEY idx_notification_created (
        created_at
    ),

    KEY idx_notification_user_read (
        user_id,
        status
    ),

    CONSTRAINT fk_notification_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
) ENGINE=InnoDB;
