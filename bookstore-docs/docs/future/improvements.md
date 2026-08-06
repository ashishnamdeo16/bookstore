---
title: Future Improvements
---

# Future Improvements

This page intentionally lists gaps observed in the current codebase rather than invented roadmap items.

## High-value engineering improvements

1. Align analytics success topic:
   - `payment-service` publishes `payment-success`
   - `analytics-service` listens to `payment-completed`

2. Add analytics database bootstrap to Docker MySQL init script

3. Add CI/CD:
   - build backend services
   - build frontend
   - build docs
   - run automated tests

4. Add production deployment assets:
   - reverse proxy config
   - TLS termination
   - cloud infrastructure definitions

5. Expand observability:
   - metrics
   - tracing
   - dashboards
   - alerting

6. Add API-level integration tests across payment/order/notification flow

7. Revisit some service security policies:
   - confirm public vs authenticated reads in `book-service`
   - confirm `/api/orders/userId` should remain non-admin

8. Formalize API and event schemas:
   - OpenAPI generation
   - versioned Kafka contracts

9. Expand book image support:
   - controller exists, but endpoints are not implemented

10. Add `analytics-service` and `frontend` to `docker-compose.yml`

11. Update `frontend/README.md`:
    - current file still describes auth/profile-only scope

12. Remove or wire legacy frontend pages:
    - `BookListPage`, `BookDetailsPage`, `CreateBookPage`, `EditBookPage`, `AppShell`

13. Reconcile auth role model:
    - backend `Role` enum includes `MANAGER`
    - frontend only normalizes `USER` and `ADMIN`
