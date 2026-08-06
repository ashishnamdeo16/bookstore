---
title: Auth Service
---

# Auth Service

## Purpose

`auth-service` owns authentication data, password hashing, JWT issuance, refresh-token-backed sessions, and logout flows.

## Responsibilities

- Register users
- Authenticate credentials
- Issue JWT access tokens
- Create and rotate refresh-token sessions
- Revoke a single session or all sessions
- Call `user-service` to create the profile record after registration

## Dependencies

- Port: `8081`
- Database: `bookstore_auth_db`
- Downstream HTTP: `user-service`
- Security: Spring Security + JWT

## REST APIs

| Method | Path | Auth | Description |
| --- | --- | --- | --- |
| `POST` | `/auth/register` | No | Create auth user and user profile |
| `POST` | `/auth/login` | No | Authenticate and issue tokens |
| `POST` | `/auth/logout` | No body auth, refresh token required | Revoke one refresh token |
| `POST` | `/auth/logout-all` | Yes | Revoke all sessions for current user |
| `POST` | `/auth/refresh` | No | Rotate refresh token and issue a new access token |

## Key DTOs

- `RegisterRequestDto`: email, password, firstName, lastName, phoneNumber, dateOfBirth, address
- `RegisterResponseDto`: userId, email, message
- `LoginRequestDto`: email, password, deviceId
- `LoginResponseDto`: accessToken, refreshToken, expiresIn
- `RefreshTokenRequestDto`: refreshToken

## Database tables

### `auth_users`

- `user_id`
- `email`
- `role`
- `active`
- `password`
- `created_at`
- `updated_at`

### `refresh_tokens`

- `session_id`
- `user_id`
- `token` (stored hash)
- `device_id`
- `device_name`
- `created_at`
- `last_used_at`
- `expires_at`
- `revoked_at`

Indexes exist on:

- `user_id`
- `device_id`
- `token` (unique)

## Entity relationships

```mermaid
erDiagram
  AUTH_USERS ||--o{ REFRESH_TOKENS : "has sessions"
  AUTH_USERS {
    uuid user_id PK
    string email UK
    string role
    boolean active
    string password
    datetime created_at
    datetime updated_at
  }
  REFRESH_TOKENS {
    uuid session_id PK
    uuid user_id FK
    string token UK "stored hash"
    string device_id
    string device_name
    datetime created_at
    datetime last_used_at
    datetime expires_at
    datetime revoked_at
  }
```

## Security

Public routes in `SecurityConfig`:

- `/auth/login`
- `/auth/register`
- `/auth/refresh`
- `/auth/logout`

Authenticated route:

- `/auth/logout-all`

Passwords are encoded with `BCryptPasswordEncoder`.

## Internal flow

```mermaid
sequenceDiagram
  participant C as Client
  participant AUTH as auth-service
  participant DB as bookstore_auth_db
  participant USER as user-service

  C->>AUTH: POST /auth/register
  AUTH->>DB: save auth_users row
  AUTH->>USER: POST /api/user/create
  AUTH-->>C: RegisterResponseDto
```

## Error handling

- Duplicate email -> `DuplicateResourceException`
- Unknown user during login/refresh -> `ResourceNotFoundException`
- Invalid or revoked refresh token -> refresh flow fails

## Sequence diagram: login

```mermaid
sequenceDiagram
  participant C as Client
  participant AUTH as auth-service
  participant DB as bookstore_auth_db

  C->>AUTH: POST /auth/login
  AUTH->>AUTH: authenticate credentials
  AUTH->>AUTH: generate JWT
  AUTH->>DB: create refresh_tokens row
  AUTH-->>C: accessToken + refreshToken + expiresIn
```

## Sequence diagram: refresh rotation

```mermaid
sequenceDiagram
  autonumber
  participant C as Client
  participant AUTH as auth-service
  participant DB as bookstore_auth_db

  C->>AUTH: POST /auth/refresh (refreshToken)
  AUTH->>DB: look up token hash
  alt valid & not revoked & not expired
    AUTH->>DB: revoke old token, insert rotated token
    AUTH->>AUTH: issue new JWT
    AUTH-->>C: 200 LoginResponseDto
  else invalid/expired/revoked
    AUTH-->>C: error
  end
```
