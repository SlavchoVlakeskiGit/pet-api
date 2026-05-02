# Pet API

[![CI](https://github.com/SlavchoVlakeskiGit/pet-api/actions/workflows/ci.yml/badge.svg)](https://github.com/SlavchoVlakeskiGit/pet-api/actions/workflows/ci.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=SlavchoVlakeskiGit_pet-api&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=SlavchoVlakeskiGit_pet-api)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=SlavchoVlakeskiGit_pet-api&metric=coverage)](https://sonarcloud.io/summary/new_code?id=SlavchoVlakeskiGit_pet-api)

A production-grade REST API for managing pets, built with Spring Boot. Demonstrates real-world backend engineering practices across security, resilience, observability, event-driven architecture, and infrastructure.

## Architecture

```
Client
  │
  ▼
Nginx (reverse proxy, port 80)
  │
  ▼
Pet API (Spring Boot, port 8080)
  ├── MySQL       (persistence + Flyway migrations)
  ├── Redis       (response caching, idempotency keys, rate limiting)
  ├── MongoDB     (audit log)
  ├── Kafka       (publishes pet lifecycle events)
  └── Zipkin      (distributed tracing)
        │
        ▼
Notification Service (Kafka consumer + DLQ)
```

## Features

### API
- **Full CRUD** with pagination and filtering by species and owner
- **Soft delete** — pets are never hard-deleted; `deleted_at` timestamp is set instead
- **Scheduled purge** — daily job at 02:00 permanently removes soft-deleted records older than 30 days
- **Optimistic locking** — `@Version` on the `Pet` entity; concurrent updates return `409 Conflict`
- **Idempotency keys** — `POST /pets` with an `Idempotency-Key` header stores the response in Redis for 24 hours; duplicate requests return the original response without re-creating the pet

### Security
- **JWT authentication** — stateless auth with short-lived access tokens (15 min) and refresh tokens (7 days stored in MySQL)
- **Role-Based Access Control** — `USER` can create and update; `ADMIN` required to delete or view audit logs
- **Rate limiting** — 100 requests per minute per user, enforced via Redis with `X-RateLimit-Remaining` headers and `429` responses

### Resilience
- **Circuit breaker** — Resilience4j wraps Kafka publishing; pet operations never fail because Kafka is down
- **Bulkhead** — caps concurrent Kafka publish calls to prevent thread exhaustion
- **Retry** — `getPetById` retries up to 3× on transient database failures with exponential backoff

### Observability
- **Distributed tracing** — Micrometer + Zipkin traces every request across services
- **Prometheus metrics** — `/actuator/prometheus` endpoint; scrape-ready for Grafana
- **Spring Actuator** — `/actuator/health`, `/actuator/metrics`, `/actuator/info`
- **Correlation IDs** — every request carries an `X-Request-ID` (passed in or auto-generated) injected into MDC so every log line is traceable to its originating request
- **Audit log** — every create/update/delete writes an async `PetAuditLog` document to MongoDB; `GET /pets/{id}/audit` (ADMIN only) returns the full history

### Event-Driven
- **Kafka producer** — publishes `CREATED`, `UPDATED`, `DELETED` events to the `pet-events` topic
- **Dead Letter Queue** — the notification service retries failed events 3× with exponential backoff via `@RetryableTopic`; events that exhaust retries land in `pet-events-dlt` and are handled by `@DltHandler`

### Data
- **MySQL** — primary relational store, managed by Flyway (5 versioned migrations)
- **MongoDB** — audit log collection; second data store demonstrating multi-database architecture
- **Redis** — shared cache for responses, idempotency keys, and rate limit counters
- **Flyway** — versioned, repeatable database migrations applied on startup

### Infrastructure
- **Nginx** — reverse proxy in front of the app; forwards `X-Forwarded-For` and `X-Real-IP`
- **Docker Compose** — single command starts the full stack: MySQL, Redis, MongoDB, Kafka, Zipkin, Nginx, Pet API, Notification Service
- **Kubernetes** — full `k8s/` manifest set: namespace, configmap, secret, persistent volumes for MySQL and MongoDB, deployments for all services, pet-api with 2 replicas, resource requests/limits, readiness/liveness probes, HPA (scales 2–5 pods on CPU/memory), and Nginx Ingress
- **Multi-stage Dockerfile** — Maven build stage + JRE-only runtime image; build tools are not in the production image

### Testing & CI
- **Integration tests** — Testcontainers spins up real MySQL, Redis, and MongoDB containers; 16 tests cover full CRUD, auth checks, filters, soft delete, idempotency deduplication, audit log, RBAC enforcement, ETag 304 responses, and rate limit 429 responses
- **Unit tests** — Mockito-based service tests covering all business logic paths
- **ArchUnit** — architecture rules enforced at test time (layered dependencies, no cross-layer shortcuts)
- **GitHub Actions** — runs the full test suite, generates JaCoCo coverage, runs SonarCloud analysis, and pushes a Docker image to GHCR on every merge to main
- **OWASP Dependency Check** — weekly workflow scans for known CVEs in third-party libraries; fails the build on CVSS ≥ 7

## Tech Stack

| Layer | Technology |
|---|---|
| Framework | Spring Boot 3.4 |
| Language | Java 17 |
| Primary DB | MySQL 8 + Spring Data JPA + Hibernate |
| Audit Store | MongoDB 7 + Spring Data MongoDB |
| Cache / Rate Limit | Redis 7 + Spring Data Redis |
| Migrations | Flyway |
| Messaging | Apache Kafka (KRaft) |
| Security | Spring Security + JWT (jjwt 0.12) |
| Mapping | MapStruct |
| Resilience | Resilience4j (Circuit Breaker, Bulkhead, Retry) |
| Observability | Spring Actuator + Micrometer + Zipkin + Prometheus |
| API Docs | SpringDoc OpenAPI (Swagger UI) |
| Testing | JUnit 5 + Mockito + Testcontainers |
| CI | GitHub Actions |
| Containerisation | Docker + Docker Compose + multi-stage Dockerfile |
| Orchestration | Kubernetes (manifests in `k8s/`) |
| Reverse Proxy | Nginx |

## Getting Started

### Prerequisites

- Java 17
- Docker Desktop

### Run with Docker Compose

```bash
docker-compose up --build
```

This starts the full stack automatically.

| Service | URL |
|---|---|
| Pet API (via Nginx) | http://localhost |
| Swagger UI | http://localhost/swagger-ui.html |
| Actuator Health | http://localhost/actuator/health |
| Prometheus Metrics | http://localhost/actuator/prometheus |
| Zipkin UI | http://localhost:9411 |
| Grafana Dashboards | http://localhost:3000 (admin / admin) |
| Prometheus UI | http://localhost:9090 |

A default admin user (`admin` / `admin123`) is created automatically on first startup.

### Run locally

Requires MySQL on port 3306, Redis on 6379, MongoDB on 27017, Kafka on 9093.

```bash
./mvnw spring-boot:run
```

### Deploy to Kubernetes

```bash
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/secret.yaml
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/mysql.yaml
kubectl apply -f k8s/redis.yaml
kubectl apply -f k8s/mongo.yaml
kubectl apply -f k8s/kafka.yaml
kubectl apply -f k8s/zipkin.yaml
kubectl apply -f k8s/app.yaml
kubectl apply -f k8s/notification.yaml
kubectl apply -f k8s/ingress.yaml
kubectl apply -f k8s/hpa.yaml
```

## API Overview

### Authentication

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| POST | `/v1/auth/register` | Register a new user | Public |
| POST | `/v1/auth/login` | Login — returns access token + refresh token | Public |
| POST | `/v1/auth/refresh` | Exchange refresh token for a new access token | Public |
| POST | `/v1/auth/logout` | Invalidate refresh token | Required |

### Pets

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| GET | `/v1/pets` | List pets (paginated, filterable by species/ownerName) | Public |
| GET | `/v1/pets/{id}` | Get pet by ID (Redis cached, ETag supported) | Public |
| POST | `/v1/pets` | Create a pet (supports `Idempotency-Key` header) | USER or ADMIN |
| PATCH | `/v1/pets/{id}` | Partially update a pet | USER or ADMIN |
| DELETE | `/v1/pets/{id}` | Soft delete a pet | ADMIN only |
| GET | `/v1/pets/{id}/audit` | Full audit history for a pet | ADMIN only |

### Rate Limiting

All endpoints are rate-limited to **100 requests per minute** per user. Responses include:

```
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 94
```

Exceeding the limit returns `429 Too Many Requests` with a `Retry-After: 60` header.

### Idempotency

To safely retry `POST /pets` without creating duplicates:

```bash
curl -X POST http://localhost/v1/pets \
  -H "Authorization: Bearer <token>" \
  -H "Idempotency-Key: my-unique-key-123" \
  -H "Content-Type: application/json" \
  -d '{"name":"Rex","species":"Dog","age":3,"ownerName":"John"}'
```

Sending the same request again with the same `Idempotency-Key` returns the original response.

### Quick example

```bash
# Login
curl -X POST http://localhost/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# Create a pet
curl -X POST http://localhost/v1/pets \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"name":"Rex","species":"Dog","age":3,"ownerName":"John"}'

# Get audit log (admin only)
curl http://localhost/v1/pets/1/audit \
  -H "Authorization: Bearer <token>"
```

## Running Tests

Requires Docker (for Testcontainers).

```bash
./mvnw test
```

## Related

- [notification-service](https://github.com/SlavchoVlakeskiGit/notification-service) — Kafka consumer with DLQ that handles pet lifecycle event notifications
