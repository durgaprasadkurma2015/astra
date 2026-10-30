
ALTER TABLE app_users
    ADD COLUMN date_of_birth DATE NULL,
    ADD COLUMN gender VARCHAR(30) NULL,
    ADD COLUMN phone_number VARCHAR(20) NULL,
    ADD COLUMN alternate_phone_number VARCHAR(20) NULL,
    ADD COLUMN address_line1 VARCHAR(255) NULL,
    ADD COLUMN address_line2 VARCHAR(255) NULL,
    ADD COLUMN landmark VARCHAR(150) NULL,
    ADD COLUMN city VARCHAR(100) NULL,
    ADD COLUMN district VARCHAR(100) NULL,
    ADD COLUMN state VARCHAR(100) NULL,
    ADD COLUMN country VARCHAR(100) NULL,
    ADD COLUMN postal_code VARCHAR(20) NULL,
    ADD COLUMN preferred_language VARCHAR(50) NULL;
