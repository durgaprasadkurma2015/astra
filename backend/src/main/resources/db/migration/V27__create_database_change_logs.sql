
CREATE TABLE IF NOT EXISTS database_change_logs (
    id BIGINT NOT NULL AUTO_INCREMENT,

    table_schema VARCHAR(128) NOT NULL,
    table_name VARCHAR(128) NOT NULL,
    operation VARCHAR(10) NOT NULL,

    record_id JSON NOT NULL,
    old_data JSON NULL,
    new_data JSON NULL,
    changed_columns JSON NULL,

    database_user VARCHAR(255) NULL,
    connection_id BIGINT UNSIGNED NULL,

    changed_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    PRIMARY KEY (id),

    INDEX idx_db_change_table (table_name),
    INDEX idx_db_change_operation (operation),
    INDEX idx_db_change_timestamp (changed_at),
    INDEX idx_db_change_table_timestamp (table_name, changed_at),
    INDEX idx_db_change_operation_timestamp (operation, changed_at)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;
