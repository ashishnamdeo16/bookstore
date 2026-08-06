---
title: Getting Started
sidebar_position: 2
---

# Getting Started

This section documents the repository as it exists today.

## Prerequisites

### Required locally

- Node.js 20+
- npm 10+
- Java 17
- Maven Wrapper support (`./mvnw` is present in each backend service)
- Docker Desktop or compatible Docker engine

### Useful tools

- Stripe CLI for local webhook forwarding
- MySQL client
- Kafka UI or CLI tools

## Environment variables

Root `.env.example` contains the shared local variables:

```dotenv
MYSQL_ROOT_PASSWORD=root
DB_USERNAME=bookstore
DB_PASSWORD=bookstore
JWT_SECRET=change-me-to-a-long-random-secret
INTERNAL_API_KEY=change-me-internal-key
MAIL_HOST=sandbox.smtp.mailtrap.io
MAIL_PORT=2525
MAIL_USERNAME=
MAIL_PASSWORD=
TWILIO_ACCOUNT_SID=
TWILIO_AUTH_TOKEN=
TWILIO_PHONE_NUMBER=
STRIPE_SECRET_KEY=
STRIPE_WEBHOOK_SECRET=
VITE_STRIPE_PUBLISHABLE_KEY=
```

Frontend `.env.example` currently contains:

```dotenv
VITE_API_BASE_URL=http://localhost:8080
```

## Run with Docker Compose

From the repository root:

```bash
docker compose up --build -d
```

Services and ports in `docker-compose.yml`:

| Service | Port |
| --- | --- |
| API Gateway | `8080` |
| Auth Service | `8081` |
| User Service | `8082` |
| Book Service | `8083` |
| Order Service | `8084` |
| Notification Service | `8085` |
| Payment Service | `8087` |
| Analytics Service | `8088` |
| MySQL | `3306` |
| Zookeeper | `2181` |
| Kafka external listener | `9092` |

## Run without Docker

Typical order:

1. Start MySQL
2. Start Zookeeper and Kafka
3. Start backend services
4. Start frontend

Example backend startup:

```bash
cd auth-service && ./mvnw spring-boot:run
cd user-service && ./mvnw spring-boot:run
cd book-service && ./mvnw spring-boot:run
cd order-service && ./mvnw spring-boot:run
cd notification-service && ./mvnw spring-boot:run
cd payment-service && ./mvnw spring-boot:run
cd analytics-service && ./mvnw spring-boot:run
cd api-gateway && ./mvnw spring-boot:run
```

Frontend:

```bash
cd frontend
npm install
npm run dev
```

## Stripe local workflow

The frontend expects `VITE_STRIPE_PUBLISHABLE_KEY`.

The payment service expects:

- `STRIPE_SECRET_KEY`
- `STRIPE_WEBHOOK_SECRET`

To forward Stripe test webhooks locally:

```bash
stripe listen --forward-to localhost:8087/api/payments/webhook
```

## Notes on databases

`docker/mysql/init.sql` creates these databases:

- `bookstore_auth_db`
- `bookstore_user_db`
- `bookstore_books_db`
- `bookstore_order_db`
- `bookstore_notification_db`
- `bookstore_payment_db`

:::note
No analytics database is created in `docker/mysql/init.sql`, even though `analytics-service` defaults to `bookstore_analytics_db` in its local `application.yml`. If analytics is run locally without schema bootstrap changes, that database must exist first.
:::
