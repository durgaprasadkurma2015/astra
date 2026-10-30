# Astra E-Commerce

A React/Vite storefront backed by a Spring Boot REST API and MySQL. Database schema changes are managed by Flyway. Docker is intentionally omitted from the local setup in line with the project request.

## Requirements

- Java 21
- Node.js LTS and npm
- MySQL 8.x
- Git; Postman is useful for API checks

Track requirement coverage and remaining work in [IMPLEMENTATION_STATUS.md](IMPLEMENTATION_STATUS.md).

## Configure the backend

Use [backend/.env.example](/D:/astra/backend/.env.example) as a reference and set the listed variables in your local shell or IDE run configuration. Spring does not load `.env` files automatically. Generate unique base64 secrets for `JWT_SECRET` and `OTP_HMAC_SECRET`; never commit real values. Configure the SMTP account if registration and password recovery emails should be sent.

Online payment is optional until `RAZORPAY_KEY_ID`, `RAZORPAY_KEY_SECRET`, and `RAZORPAY_WEBHOOK_SECRET` are configured. Keep these values server-side; the API verifies checkout signatures, confirms captured status with the provider, and accepts signed `payment.captured` webhooks for recovery.

Create the `astra_db` database or use the configured URL with `createDatabaseIfNotExist=true`. Flyway applies versioned migrations at startup, and Hibernate validates the resulting schema.

From `backend`, run:

```powershell
.\mvnw.cmd spring-boot:run
```

The API listens on `http://localhost:8080`. Health and readiness checks are available at `/actuator/health` and `/actuator/health/readiness`.

## Configure and run the frontend

From `frontend`, copy `.env.example` to `.env.local` if the API is not at its default URL, then install and start:

```powershell
npm ci
npm run dev
```

Vite serves the storefront at `http://localhost:5173`. Set `VITE_API_BASE_URL` to the versioned API root when using another backend host. The backend's `FRONTEND_URL` must match the browser origin for CORS.

## Verify changes

```powershell
# frontend
npm run lint
npm run build

# backend
.\mvnw.cmd verify
```

GitHub Actions runs frontend lint/build and backend verification against MySQL. The pipeline uses placeholder credentials only and does not deploy the application.
"# astra_d" 
