---
title: Deployment
---

# Deployment

## What exists in the repository

### Docker Compose

The root `docker-compose.yml` is the deployment artifact for local and demo environments.

It defines:

- MySQL
- Zookeeper
- Kafka
- auth-service
- user-service
- book-service
- order-service
- notification-service
- payment-service
- api-gateway

Analytics service code exists, but it is **not** included in the checked-in Compose file shown in this repository snapshot.

### Dockerfile

`docker/Dockerfile.service` is a generic two-stage build:

1. Maven build stage
2. Eclipse Temurin JRE runtime stage

## Production-related topics

### AWS / EC2

No AWS-specific infrastructure code is present in the repository.

### Nginx

No Nginx config is present in the repository.

### GitHub Actions

No workflow files are present under `.github/workflows`.

## Recommendation

Treat production deployment documentation as a current-state gap:

- TODO: add CI/CD pipeline
- TODO: add reverse proxy / TLS config
- TODO: add container registry and environment promotion strategy
- TODO: add production secret management
