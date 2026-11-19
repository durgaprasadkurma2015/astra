
CREATE TABLE api_request_logs (
    id BIGINT NOT NULL AUTO_INCREMENT,

    request_id VARCHAR(64) NULL,

    source VARCHAR(20) NOT NULL,

    request_timestamp DATETIME(3) NOT NULL,

    http_method VARCHAR(12) NOT NULL,

    endpoint VARCHAR(2048) NOT NULL,

    status_code INT NOT NULL DEFAULT 0,

    duration_ms BIGINT NOT NULL DEFAULT 0,

    success BOOLEAN NOT NULL DEFAULT FALSE,

    client_ip VARCHAR(64) NULL,

    user_agent VARCHAR(1000) NULL,

    request_headers LONGTEXT NULL,

    request_body LONGTEXT NULL,

    response_headers LONGTEXT NULL,

    response_body LONGTEXT NULL,

    error_message VARCHAR(2000) NULL,

    created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),

    PRIMARY KEY (id),

    INDEX idx_api_logs_source_timestamp (source, request_timestamp),

    INDEX idx_api_logs_status_timestamp (status_code, request_timestamp),

    INDEX idx_api_logs_method (http_method),

    INDEX idx_api_logs_request_id (request_id),

    INDEX idx_api_logs_created_at (created_at)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
