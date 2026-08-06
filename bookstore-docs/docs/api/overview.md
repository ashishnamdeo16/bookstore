---
title: API Reference
---

# API Reference

Endpoint-by-endpoint reference generated from the current controller classes in the repository. Field names and structures match the DTOs in the source; IDs and sample values are illustrative.

## Reference pages

| Area | Service | Base path |
| --- | --- | --- |
| [Auth API](./auth.md) | auth-service | `/auth` |
| [Users API](./users.md) | user-service | `/api/user` |
| [Catalog API](./catalog.md) | book-service | `/api/books`, `/api/authors`, `/api/categories`, `/api/publishers` |
| [Orders API](./orders.md) | order-service | `/api/orders` |
| [Payments API](./payments.md) | payment-service | `/api/payments` |
| [Analytics API](./analytics.md) | analytics-service | `/analytics` |

## Base URL

All frontend requests go through the gateway:

```text
http://localhost:8080
```

That value comes from `frontend/.env.example` as `VITE_API_BASE_URL`. The gateway proxies each path prefix to the matching service without stripping the prefix.

## Authentication

Most routes expect a bearer token:

```http
Authorization: Bearer <JWT access token>
```

The JWT subject is the caller's `userId` (UUID) and the authority is `ROLE_<Role>`, where `Role` is one of `USER`, `ADMIN`, `MANAGER`.

**Public / non-JWT routes:**

- `POST /auth/register`, `POST /auth/login`, `POST /auth/refresh`, `POST /auth/logout`
- `POST /api/user/create` — requires `X-Internal-Api-Key`
- `POST /api/payments/webhook` — requires `Stripe-Signature`

## Conventions

| Java type | JSON representation |
| --- | --- |
| `UUID` | string, e.g. `"3f1c2d4e-..."` |
| `BigDecimal` | number, e.g. `39.99` |
| `LocalDate` | string `"yyyy-MM-dd"` |
| `LocalDateTime` | ISO-8601 string, e.g. `"2026-08-04T18:00:00"` |
| enum | its name string, e.g. `"CONFIRMED"` |

Some endpoints return a **raw string** or **`204 No Content`** rather than a JSON object — these are flagged on each page.

## Error shape

Services use `@RestControllerAdvice` handlers. Typical failures:

- `400` — validation errors (bean validation on request DTOs)
- `401` — missing/invalid JWT
- `403` — authenticated but lacking role/ownership
- `404` — `ResourceNotFoundException`
- `409` — `DuplicateResourceException` (e.g. duplicate email)
