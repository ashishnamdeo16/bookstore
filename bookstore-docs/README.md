# Bookstore Docs

This directory contains a static documentation site for the Bookstore microservices repository.

## What is documented

- Project overview
- Local setup and environment variables
- Architecture and request flow
- One page for each backend service
- API and Kafka reference
- Database and security model
- Frontend structure and checkout flow
- Deployment status in the current repository

## Install

```bash
cd bookstore-docs
npm install
```

## Run locally

```bash
npm run start
```

Default local docs URL:

```text
http://localhost:3000/bookstore-docs/
```

## Build

```bash
npm run build
```

## Serve the built site

```bash
npm run serve
```

## Search



## Mermaid support



## Notes

- The docs intentionally reflect the repository as-is.
- Where implementation gaps exist, the docs call them out as TODOs or known issues instead of inventing missing behavior.
