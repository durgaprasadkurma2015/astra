# ASTRA — Spring Boot Concepts Implementation

ASTRA is implemented as a **modular monolith**: business modules are separated by package boundaries and can later be extracted into microservices. This avoids adding service-discovery infrastructure before there are independently deployable services.

## Implemented concepts

1. Spring Fundamentals — component scanning, dependency injection, constructor injection, configuration beans.
2. Spring Boot — executable application, starters, externalized configuration, profiles.
3. REST APIs — controllers, DTOs, validation, pagination, HTTP status handling.
4. JPA/Hibernate — entities, repositories, relationships, transactions, lazy loading with Open-Session-In-View disabled.
5. Entity relationships — one-to-one, one-to-many, many-to-one and join entities across users, products, carts, orders, payments, shipping, reviews and sellers.
6. Exception handling — centralized `GlobalExceptionHandler` and typed API exceptions.
7. Validation — Jakarta Bean Validation on request DTOs.
8. Security — Spring Security, JWT access/refresh tokens, role-based method security, Google ID-token verification.
9. Logging — Logback configuration and correlation IDs.
10. Scheduling — refresh-token cleanup and metrics heartbeat.
11. Profiles/configuration — local, dev, test, prod and SSL profiles plus environment variables.
12. AOP — controller audit logging and custom circuit-breaker aspect.
13. Transactions — service-level transaction boundaries and read-only transactions.
14. Caching — Spring Cache with Redis support for production.
15. Microservice readiness — module boundaries, `RestClient` integration, externalized service endpoints and event-driven boundaries. Actual service discovery/gateway is intentionally not forced into a monolith.
16. Messaging — Kafka product events and RabbitMQ product-event publishing.
17. HTTPS/SSL — dedicated SSL profile using an external PKCS12 keystore.
18. Testing — Spring Boot tests, Spring Security test support, Mockito/Testcontainers dependencies and H2 test profile.
19. Observability — Actuator, Micrometer metrics and Prometheus endpoint.
20. OpenAPI/Swagger — `/swagger-ui.html` and `/v3/api-docs`.
21. File handling — multipart upload, validation, safe path normalization, and an `ObjectStorageService` abstraction with a local adapter that can be replaced by S3/cloud storage.
22. Email/notifications — SMTP email, async execution, retry, in-app/SMS provider abstractions, WebSocket and SSE notification delivery.
23. Performance — HikariCP tuning, pagination, cache, read-only transactions, async executor.
24. Async processing — `@Async` task executor.
25. Real-time communication — STOMP WebSocket plus Server-Sent Events.
26. Deployment — Docker, Docker Compose, Kubernetes manifests and GitHub Actions CI.

## External infrastructure

- MySQL: transactional database.
- Redis: production cache.
- Kafka: durable event streaming.
- RabbitMQ: queue/topic messaging.
- Elasticsearch: advanced search module.
- SMTP: email delivery.

Secrets are intentionally environment-driven. Never commit real passwords, JWT secrets, SMTP credentials, cloud keys or production keystores.
