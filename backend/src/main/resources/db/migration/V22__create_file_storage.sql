CREATE TABLE stored_files (
    id BIGINT NOT NULL AUTO_INCREMENT,
    original_filename VARCHAR(255) NOT NULL,
    storage_filename VARCHAR(100) NOT NULL,
    content_type VARCHAR(150) NOT NULL,
    file_size BIGINT NOT NULL,
    purpose VARCHAR(30) NOT NULL,
    owner_username VARCHAR(190),
    entity_id BIGINT,
    storage_path VARCHAR(1000) NOT NULL,
    file_content LONGBLOB NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    INDEX idx_stored_files_owner (owner_username),
    INDEX idx_stored_files_purpose_entity (purpose, entity_id),
    CONSTRAINT chk_stored_files_size
        CHECK (file_size > 0)
);