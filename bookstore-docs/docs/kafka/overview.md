---
title: Kafka
---

# Kafka

## Topics found in the repository

| Topic | Producer | Consumer |
| --- | --- | --- |
| `order-created` | `order-service` | `notification-service`, `analytics-service` |
| `payment-success` | `payment-service` | `order-service` |
| `payment-failed` | `payment-service` | `analytics-service` |

## Event schemas

### `order-created`

- `orderId`
- `userId`
- `email`
- `totalAmount`
- `items[]`
- `firstName`
- `phoneNumber`
- `status`

### `payment-success`

- `paymentId`
- `checkoutId`
- `userId`
- `transactionId`
- `amount`
- `items[]`
- `email`
- `firstName`
- `phoneNumber`

### `payment-failed`

- `paymentId`
- `checkoutId`
- `userId`
- `transactionId`
- `amount`

## Checkout/event flow

```mermaid
flowchart LR
  P[payment-service webhook handler] --> S[publish payment-success]
  P --> F[publish payment-failed]
  S --> O[order-service consumes payment-success]
  O --> C[publish order-created]
  C --> N[notification-service consumes order-created]
  C --> A[analytics-service consumes order-created]
  F --> A2[analytics-service consumes payment-failed]
```

## Retry / delivery notes

- Producers use Spring Kafka `KafkaTemplate`
- `payment-service` waits on `.get()` for publish success/failure
- `order-service` publishes after transaction commit using `TransactionSynchronizationManager`
- `analytics-service` adds an application-level deduplication table via `processed_events`
