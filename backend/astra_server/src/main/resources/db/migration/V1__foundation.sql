-- V1__foundation.sql

CREATE TABLE roles (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(30) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_roles_name (name)
) ENGINE=InnoDB;

INSERT INTO roles (name) VALUES
    ('CUSTOMER'),
    ('SELLER'),
    ('ADMIN');
