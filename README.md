# Bookstore

A microservices bookstore platform with a React frontend, Spring Boot services, Kafka eventing, Stripe payments, and Docker Compose for local development.

## Documentation

Official engineering docs live in [`bookstore-docs/`](./bookstore-docs/README.md).

```bash
cd bookstore-docs
npm install
npm run start
```

## Quick start

### Docker Compose

```bash
docker compose up --build -d
```

Gateway: `http://localhost:8080`

### Frontend

```bash
cd frontend
cp .env.example .env
npm install
npm run dev
```

Frontend: `http://localhost:5173`

## Services

| Service | Port |
| --- | --- |
| API Gateway | 8080 |
| Auth Service | 8081 |
| User Service | 8082 |
| Book Service | 8083 |
| Order Service | 8084 |
| Notification Service | 8085 |
| Payment Service | 8087 |
| Analytics Service | 8088 |

## Environment

Copy `.env.example` to `.env` at the repository root and fill in secrets before running payment or notification features.
