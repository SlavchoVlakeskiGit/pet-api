# Pet API

A production-grade REST API for managing pets, built with Spring Boot. Demonstrates real-world backend engineering practices including security, observability, resilience, and event-driven architecture.

## Architecture

```
Client
  │
  ▼
Pet API (Spring Boot, port 8080)
  ├── MySQL       (persistence + Flyway migrations)
  ├── Redis       (response caching)
  ├── Kafka       (publishes pet lifecycle events)
  └── Zipkin      (distributed tracing)
        │
        ▼
  Notification Service (Kafka consumer, port 8081)
```

## Features

- **REST API** — full CRUD with pagination and filtering by species/owner
- **JWT Authentication** — stateless auth with short-lived access tokens (15 min) and refresh tokens (7 days)
- **Role-Based Access Control** — `USER` can create and update; `ADMIN` required to delete
- **Soft Delete** — pets are never hard-deleted; `deleted_at` timestamp is set instead
- **Redis Caching** — `GET /pets/{id}` responses cached, evicted automatically on update/delete
- **Kafka Event Publishing** — publishes `CREATED`, `UPDATED`, `DELETED` events to `pet-events` topic
- **Circuit Breaker** — Resilience4j wraps Kafka publishing; pet operations never fail due to Kafka being down
- **Flyway Migrations** — versioned database schema managed in SQL
- **Spring Actuator** — `/actuator/health`, `/actuator/metrics`, `/actuator/info`
- **Distributed Tracing** — Micrometer + Zipkin traces requests across services
- **Swagger UI** — interactive API docs at `/swagger-ui.html`
- **Testcontainers** — integration tests run against real MySQL and Redis Docker containers
- **GitHub Actions CI** — runs full test suite on every push and pull request

## Tech Stack

| Layer | Technology |
|---|---|
| Framework | Spring Boot 3.4 |
| Language | Java 17 |
| Database | MySQL 8 + Spring Data JPA + Hibernate |
| Migrations | Flyway |
| Caching | Redis |
| Messaging | Apache Kafka |
| Security | Spring Security + JWT (jjwt) |
| Mapping | MapStruct |
| Resilience | Resilience4j Circuit Breaker |
| Observability | Spring Actuator + Micrometer + Zipkin |
| API Docs | SpringDoc OpenAPI (Swagger UI) |
| Testing | JUnit 5 + Mockito + Testcontainers |
| CI | GitHub Actions |
| Containerisation | Docker + Docker Compose |

## Getting Started

### Prerequisites

- Java 17
- Docker Desktop

### Run with Docker Compose

```bash
docker-compose up --build
```

This starts MySQL, Redis, Kafka, Zipkin, the Pet API, and the Notification Service.

| Service | URL |
|---|---|
| Pet API | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| Actuator Health | http://localhost:8080/actuator/health |
| Zipkin UI | http://localhost:9411 |

### Run locally

Requires MySQL on port 3306 with a `petdb` database.

```bash
./mvnw spring-boot:run
```

A default admin user (`admin` / `admin123`) is created automatically on first startup.

## API Overview

### Authentication

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| POST | `/auth/register` | Register a new user | Public |
| POST | `/auth/login` | Login — returns access token + refresh token | Public |
| POST | `/auth/refresh` | Exchange refresh token for a new access token | Public |
| POST | `/auth/logout` | Invalidate refresh token | Required |

### Pets

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| GET | `/pets` | List pets (paginated, filterable) | Public |
| GET | `/pets/{id}` | Get pet by ID (cached) | Public |
| POST | `/pets` | Create a pet | USER or ADMIN |
| PATCH | `/pets/{id}` | Partially update a pet | USER or ADMIN |
| DELETE | `/pets/{id}` | Soft delete a pet | ADMIN only |

### Quick example

```bash
# Login as admin
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# Create a pet (use the token from the response above)
curl -X POST http://localhost:8080/pets \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"name":"Rex","species":"Dog","age":3,"ownerName":"John"}'
```

## Running Tests

Requires Docker running — Testcontainers automatically spins up MySQL and Redis.

```bash
./mvnw test
```

## Related

- [notification-service](https://github.com/SlavchoVlakeskiGit/notification-service) — Kafka consumer that handles pet lifecycle event notifications
