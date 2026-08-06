---
title: API Gateway
---

# API Gateway

## Purpose

`api-gateway` is the single HTTP entry point for the SPA and forwards requests to backend services using Spring Cloud Gateway Server WebFlux.

## Responsibilities

- Route public and authenticated HTTP traffic
- Apply CORS for local frontend origins
- Keep service base URLs configurable through environment variables

## Runtime

- Port: `8080`
- Stack: Spring Boot + Spring Cloud Gateway + Actuator

## Routes

| Route prefix | Target service |
| --- | --- |
| `/auth/**` | auth-service |
| `/api/user/**` | user-service |
| `/api/books/**` | book-service |
| `/api/authors/**` | book-service |
| `/api/categories/**` | book-service |
| `/api/publishers/**` | book-service |
| `/api/orders/**` | order-service |
| `/api/payments/**` | payment-service |
| `/analytics/**` | analytics-service |

## Routing map

```mermaid
flowchart LR
  SPA([React SPA :5173]) -->|http| GW{{api-gateway :8080}}
  GW -->|/auth/**| AUTH[auth-service :8081]
  GW -->|/api/user/**| USER[user-service :8082]
  GW -->|/api/books,authors,categories,publishers/**| BOOK[book-service :8083]
  GW -->|/api/orders/**| ORDER[order-service :8084]
  GW -->|/api/payments/**| PAY[payment-service :8087]
  GW -->|/analytics/**| ANALYTICS[analytics-service :8088]
  NOTIF[notification-service :8085]:::async
  classDef async stroke-dasharray: 5 5,fill:#eee,color:#555;
```

`notification-service` is not reachable through the gateway — it only consumes Kafka events.

## CORS

Allowed origins in `application.yml`:

- `http://localhost:5173`
- `http://localhost:3000`

Allowed methods:

- `GET`
- `POST`
- `PUT`
- `DELETE`
- `OPTIONS`

## Dependencies

- Spring Cloud Gateway Server WebFlux
- Spring Boot Actuator

## Not routed by the gateway

The following paths are **not** proxied today:

- `/api/bookimage/**` — `BookImageController` exists but has no implemented endpoints
- notification-service — Kafka-only worker with no HTTP API

## Notes

The gateway is purely routing infrastructure. Authentication decisions are still enforced inside each downstream service.
