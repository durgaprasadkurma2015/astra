CREATE TABLE customers (
    id BIGINT NOT NULL AUTO_INCREMENT,

    auth_user_id BIGINT NULL,

    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,

    date_of_birth DATE NULL,
    gender VARCHAR(30) NULL,

    phone_number VARCHAR(20) NULL,
    alternate_phone_number VARCHAR(20) NULL,

    address_line1 VARCHAR(255) NULL,
    address_line2 VARCHAR(255) NULL,
    landmark VARCHAR(150) NULL,

    city VARCHAR(100) NULL,
    district VARCHAR(100) NULL,
    state VARCHAR(100) NULL,
    country VARCHAR(100) NULL,

    postal_code VARCHAR(20) NULL,
    preferred_language VARCHAR(50) NULL,

    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,

    CONSTRAINT pk_customers PRIMARY KEY (id),
    CONSTRAINT uk_customer_email UNIQUE (email),
    CONSTRAINT uk_customer_auth_user UNIQUE (auth_user_id)
);