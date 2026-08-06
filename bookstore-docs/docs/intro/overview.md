---
title: Project Overview
sidebar_position: 1
---

# Bookstore Microservices

This repository implements a bookstore platform as a set of Spring Boot services behind a Spring Cloud Gateway, plus a React + Vite frontend and a local Docker Compose environment.

## What is in the repository

| Area | Implementation |
| --- | --- |
| Frontend | React 19 + Vite 8 + React Router 7 + Stripe Elements |
| Gateway | `api-gateway` using Spring Cloud Gateway Server WebFlux |
| Authentication | `auth-service` with JWT access tokens and refresh-token-backed sessions |
| User profiles | `user-service` |
| Catalog | `book-service` for books, authors, categories, publishers |
| Orders | `order-service` |
| Payments | `payment-service` with Stripe |
| Notifications | `notification-service` with email + Twilio SMS integration |
| Analytics | `analytics-service` fed from Kafka events |
| Messaging | Kafka + Zookeeper in Docker Compose |
| Databases | MySQL with separate databases per service |

## Repository layout

```text
auth-service/
user-service/
book-service/
order-service/
payment-service/
notification-service/
analytics-service/
api-gateway/
frontend/
docker/
docker-compose.yml
```

## Key platform capabilities

- User registration and login
- JWT-protected customer and admin routes
- Catalog CRUD for books, authors, categories, and publishers
- Cart and checkout flow in the frontend
- Stripe payment intent creation and webhook handling
- Order creation from payment success events
- Notification fan-out from order events
- Admin analytics views backed by Kafka-fed aggregates
- Docker-based local full-stack environment

## Technology stack

### Backend

- Java 17
- Spring Boot 4.x
- Spring Security
- Spring Data JPA
- Spring Cloud OpenFeign
- Spring Cloud Gateway
- Spring Kafka
- MySQL
- JWT via `io.jsonwebtoken`

### Frontend

- React 19
- TypeScript 6
- Vite 8
- React Router DOM 7
- Recharts
- `@stripe/react-stripe-js`

## Features reflected in the current code

- Access tokens are short-lived JWTs and refresh tokens are persisted as device sessions in `auth-service`
- User profile creation is delegated from `auth-service` to `user-service` via an internal API key
- The payment flow creates a Stripe PaymentIntent first, then waits for the webhook to publish Kafka events
- `order-service` builds a confirmed order from Kafka topic `payment-success`
- `notification-service` consumes `order-created`
- `analytics-service` consumes `order-created` and `payment-failed`, and is wired to consume `payment-completed`

:::warning Known implementation gap
`payment-service` publishes `payment-success`, while `analytics-service` listens for `payment-completed`. As checked in the current codebase, successful payment analytics will not be ingested until the topic names are aligned.
:::
