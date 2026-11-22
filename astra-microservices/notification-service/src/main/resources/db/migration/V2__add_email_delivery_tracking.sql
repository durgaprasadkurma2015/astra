
ALTER TABLE notification_events
    ADD COLUMN attempt_count INT NOT NULL DEFAULT 0,
    ADD COLUMN max_attempts INT NOT NULL DEFAULT 5,
    ADD COLUMN next_attempt_at DATETIME NULL,
    ADD COLUMN last_attempt_at DATETIME NULL,
    ADD COLUMN sent_at DATETIME NULL,
    ADD COLUMN locked_until DATETIME NULL;
    
CREATE INDEX idx_notification_delivery
    ON notification_events (
        delivery_status,
        next_attempt_at,
        locked_until
    );
