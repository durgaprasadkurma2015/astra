# Astra backend

Spring Boot API for the Astra storefront. The backend requires Java 21, MySQL, and the included Maven wrapper.

## Configuration

Set these environment variables before starting the application. Keep actual credentials in your local environment or a secret manager; do not commit them.

| Variable | Purpose |
| --- | --- |
| `DB_URL` | Optional JDBC URL; defaults to `jdbc:mysql://localhost:3306/astra_db?createDatabaseIfNotExist=true` |
| `DB_USERNAME` | MySQL username |
| `DB_PASSWORD` | MySQL password |
| `JWT_SECRET` | Secret used to sign access tokens |
| `OTP_HMAC_SECRET` | Secret used to protect password reset OTPs |
| `MAIL_USERNAME` | SMTP account username |
| `MAIL_PASSWORD` | SMTP account password or app password |
| `MAIL_HOST` | Optional SMTP host; defaults to `smtp.gmail.com` |
| `MAIL_PORT` | Optional SMTP port; defaults to `587` |
| `FRONTEND_URL` | Optional storefront URL; defaults to `http://localhost:5173` |

Use unique, randomly generated secrets for `JWT_SECRET` and `OTP_HMAC_SECRET`. They must remain stable while the service is running so existing tokens and OTP requests can be validated.

## Run locally

1. Create the `astra_db` MySQL database, or allow the configured JDBC URL to create it.
2. Set all required environment variables in your shell.
3. From this directory, run `./mvnw spring-boot:run` (Windows: `mvnw.cmd spring-boot:run`).
4. The API listens on port `8080` by default. The frontend uses `http://localhost:8080/api/v1` unless `VITE_API_BASE_URL` is set.

Flyway applies schema migrations on startup.
