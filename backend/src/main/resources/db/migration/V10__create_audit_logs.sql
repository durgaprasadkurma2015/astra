
CREATE TABLE audit_logs (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_email VARCHAR(255),
    action VARCHAR(50) NOT NULL,
    outcome VARCHAR(20) NOT NULL,
    details VARCHAR(500),
    ip_address VARCHAR(45),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_audit_logs_email (user_email),
    INDEX idx_audit_logs_action (action),
    INDEX idx_audit_logs_created_at (created_at)
);
