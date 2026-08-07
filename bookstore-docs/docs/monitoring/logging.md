---
title: Monitoring and Logging
---

# Monitoring and Logging

## Logging

Logging is configured in service `application*.yml` files rather than a centralized log aggregation stack.

Examples in the repository:

- `org.springframework.cloud.gateway: INFO`
- `com.bookstore.analytics: INFO`
- `com.bookstore.notification: INFO` / `DEBUG` depending on profile
- `org.springframework.web: DEBUG` in payment-service dev config

In production (EKS), logs are available via `kubectl logs` on individual pods. There is no centralized log collector (ELK, Loki, CloudWatch agent) configured in the current infrastructure.

## Metrics (Prometheus)

Most backend services expose **Micrometer metrics** through Spring Actuator:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,prometheus
```

### Services with Prometheus exposure

| Service | Health | Prometheus | Custom metrics |
| --- | --- | --- | --- |
| auth-service | Yes | Yes | `BusinessMetrics` counters |
| user-service | Yes | Yes | `BusinessMetrics` counters |
| book-service | Yes | Yes | `BusinessMetrics` counters |
| order-service | Yes | Yes | `BusinessMetrics` counters |
| payment-service | Yes | Yes | `BusinessMetrics` counters |
| notification-service | Yes | Yes | `BusinessMetrics` counters |
| analytics-service | Yes | Yes | `BusinessMetrics` counters |
| api-gateway | Partial | Not configured in yml | — |

### Production Prometheus stack

The [**bookstore-infra**](https://github.com/ashishnamdeo16/bookstore-infra) repository deploys a full monitoring stack in the **`monitoring`** namespace, managed by separate Argo CD Applications:

| Component | Manifest path | Purpose |
| --- | --- | --- |
| Prometheus | `k8s/monitoring/prometheus/` | Scrapes annotated services in `bookstore` namespace |
| Grafana | `k8s/monitoring/grafana/` | Dashboards |
| Alertmanager | `k8s/monitoring/alertmanager/` | Alert routing |

Prometheus uses **Kubernetes service discovery** to find endpoints in the `bookstore` namespace. Services must carry these annotations to be scraped:

```yaml
prometheus.io/scrape: "true"
prometheus.io/path: /actuator/prometheus
prometheus.io/port: "8083"
```

Prometheus stores time-series data on a **10 Gi EBS-backed PVC** (`gp2` storage class).

Prometheus is configured to forward alerts to Alertmanager at `alertmanager.monitoring.svc.cluster.local:9093`.

## Health endpoints

Actuator health endpoints are available at `/actuator/health` on services that expose Actuator. These are used for basic liveness checks but are not wired to Kubernetes liveness/readiness probes in all manifests.

## Event observability (Kafka)

Domain events provide an async audit trail:

| Event | Producer | Consumers |
| --- | --- | --- |
| `payment-success` | payment-service | order-service |
| `payment-failed` | payment-service | analytics-service |
| `order-created` | order-service | notification-service, analytics-service |

The analytics-service deduplicates consumed events in a `processed_events` table.

## Current observability posture

| Capability | Status |
| --- | --- |
| Service logs (stdout) | Present |
| Actuator health | Present on most services |
| Prometheus metrics (application) | Present on most services |
| Prometheus server (production) | Deployed via bookstore-infra |
| Grafana dashboards | Deployed via bookstore-infra |
| Alertmanager | Deployed via bookstore-infra |
| Distributed tracing | Not implemented |
| Centralized log aggregation | Not implemented |

## Related documentation

- [Kubernetes Deployment](../deployment/kubernetes.md)
- [AWS Architecture Overview](../aws/overview.md)
