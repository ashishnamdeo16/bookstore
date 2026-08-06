---
title: Architecture Overview
sidebar_position: 3
---

# Architecture Overview

## System diagram

```mermaid
flowchart LR
  Browser[React Frontend<br/>Vite SPA] --> Gateway[API Gateway :8080]
  Gateway --> Auth[auth-service :8081]
  Gateway --> User[user-service :8082]
  Gateway --> Book[book-service :8083]
  Gateway --> Order[order-service :8084]
  Gateway --> Payment[payment-service :8087]
  Gateway --> Analytics[analytics-service :8088]

  Auth --> AuthDB[(bookstore_auth_db)]
  User --> UserDB[(bookstore_user_db)]
  Book --> BookDB[(bookstore_books_db)]
  Order --> OrderDB[(bookstore_order_db)]
  Payment --> PaymentDB[(bookstore_payment_db)]
  Notification --> NotificationDB[(bookstore_notification_db)]
  Analytics --> AnalyticsDB[(bookstore_analytics_db)]

  Order --> Kafka[(Kafka)]
  Payment --> Kafka
  Kafka --> Notification[notification-service :8085]
  Kafka --> Analytics
  Kafka --> Order

  Payment --> Stripe[Stripe]
  Notification --> Mail[Mailtrap SMTP]
  Notification --> Twilio[Twilio SMS]
```

## Request model

### Synchronous HTTP

- Frontend calls the gateway at `VITE_API_BASE_URL` or `http://localhost:8080`
- Gateway routes `/auth/**`, `/api/user/**`, `/api/books/**`, `/api/orders/**`, `/api/payments/**`, and `/analytics/**`
- Services validate JWTs independently

### Asynchronous event flow

- `order-service` publishes `order-created`
- `payment-service` publishes `payment-success` and `payment-failed`
- `notification-service` consumes `order-created`
- `order-service` consumes `payment-success`
- `analytics-service` consumes `order-created`, `payment-failed`, and currently expects `payment-completed`

## Database strategy

The project follows a database-per-service pattern:

| Service | Database |
| --- | --- |
| auth-service | `bookstore_auth_db` |
| user-service | `bookstore_user_db` |
| book-service | `bookstore_books_db` |
| order-service | `bookstore_order_db` |
| notification-service | `bookstore_notification_db` |
| payment-service | `bookstore_payment_db` |
| analytics-service | `bookstore_analytics_db` |

## Security model

- JWT bearer tokens are issued by `auth-service`
- Refresh tokens are stored in `auth-service` as device sessions
- Most services require authentication for all routes, then narrow access with `@PreAuthorize`
- Admin-only paths are enforced in controllers and security config

## Checkout flow

```mermaid
sequenceDiagram
  participant U as User
  participant FE as Frontend
  participant GW as API Gateway
  participant PAY as payment-service
  participant BOOK as book-service
  participant USER as user-service
  participant STRIPE as Stripe
  participant K as Kafka
  participant ORD as order-service

  U->>FE: Review cart and continue
  FE->>GW: POST /api/payments/checkout
  GW->>PAY: Forward request
  PAY->>BOOK: POST /api/books/batch
  PAY->>USER: GET /api/user/{id}
  PAY->>STRIPE: Create PaymentIntent
  PAY-->>FE: paymentId + clientSecret
  FE->>STRIPE: confirmCardPayment
  STRIPE->>PAY: webhook /api/payments/webhook
  PAY->>K: publish payment-success
  K->>ORD: consume payment-success
  ORD->>K: publish order-created
  FE->>GW: GET /api/orders/payment/{paymentId}
  GW->>ORD: fetch confirmed order
```

## Deployment architecture in the repository

The repository contains:

- Local Docker Compose
- A generic multi-service JVM Dockerfile at `docker/Dockerfile.service`

The repository does **not** currently contain:

- GitHub Actions workflows
- Nginx configuration
- Terraform / CloudFormation
- Kubernetes manifests

These should be treated as future work, not current implementation.
