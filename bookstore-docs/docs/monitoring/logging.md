---
title: Monitoring and Logging
---

# Monitoring and Logging

## Logging

Logging is configured in service `application*.yml` files rather than a centralized observability stack.

Examples in the repository:

- `org.springframework.cloud.gateway: INFO`
- `com.bookstore.analytics: INFO`
- `com.bookstore.notification: INFO` / `DEBUG` depending on profile
- `org.springframework.web: DEBUG` in payment-service dev config

## Health endpoints

The following services explicitly permit health/info routes:

- `book-service`
- `payment-service` (`/actuator/health`)
- `analytics-service`

## Current observability posture

Present:

- service logs
- actuator health endpoints in some services
- domain events captured in Kafka

Missing in repository:

- distributed tracing
- metrics backend
- dashboards
- log aggregation
- alerting rules

## TODOs

- Add consistent actuator exposure across all services
- Export Micrometer metrics
- Add centralized log collection
- Add traces across gateway, payment, and order flows
