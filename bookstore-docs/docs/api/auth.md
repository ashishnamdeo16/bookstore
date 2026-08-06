---
title: Auth API
---

# Auth API

Base path: `/auth` &nbsp;•&nbsp; Service: `auth-service` (port `8081`) &nbsp;•&nbsp; Gateway: `http://localhost:8080`

All routes are `permitAll` except `POST /auth/logout-all`, which requires a valid access token. Successful authentication returns a JWT whose subject is the user's `userId` (UUID) and whose authority is `ROLE_<Role>`.

---

## POST /auth/register

Creates an auth record and, via an internal call to `user-service`, the matching user profile.

**Auth:** none

**Request body — `RegisterRequestDto`**

| Field | Type | Notes |
| --- | --- | --- |
| `email` | string | Login identity |
| `password` | string | Stored BCrypt-hashed |
| `firstName` | string | |
| `lastName` | string | |
| `phoneNumber` | string | |
| `dateOfBirth` | string (`yyyy-MM-dd`) | |
| `address` | string | |

```http
POST /auth/register
Content-Type: application/json
```

```json
{
  "email": "ada@example.com",
  "password": "S3cur3Pass!",
  "firstName": "Ada",
  "lastName": "Lovelace",
  "phoneNumber": "+15551234567",
  "dateOfBirth": "1990-12-10",
  "address": "12 Analytical Ave, London"
}
```

**Response `200 OK` — `RegisterResponseDto`**

| Field | Type |
| --- | --- |
| `userId` | string (UUID) |
| `email` | string |
| `message` | string |

```json
{
  "userId": "3f1c2d4e-5a6b-47c8-9d0e-1f2a3b4c5d6e",
  "email": "ada@example.com",
  "message": "User registered successfully"
}
```

**Errors:** duplicate email → `DuplicateResourceException` (409-style error payload).

---

## POST /auth/login

Authenticates credentials and issues an access token plus a refresh-token-backed session. The `User-Agent` header, if present, is stored as the session device name.

**Auth:** none

**Request body — `LoginRequestDto`**

| Field | Type | Notes |
| --- | --- | --- |
| `email` | string | |
| `password` | string | |
| `deviceId` | string | Stable per-device identifier |

```http
POST /auth/login
Content-Type: application/json
User-Agent: Mozilla/5.0 (Macintosh; ...)
```

```json
{
  "email": "ada@example.com",
  "password": "S3cur3Pass!",
  "deviceId": "web-6f2a1c9b"
}
```

**Response `200 OK` — `LoginResponseDto`**

| Field | Type | Notes |
| --- | --- | --- |
| `accessToken` | string (JWT) | Sent as `Authorization: Bearer` |
| `refreshToken` | string | Used to rotate access tokens |
| `expiresIn` | number (long) | Access-token lifetime in ms |

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "b7c9f0a1-2d3e-4f50-8a1b-2c3d4e5f6071",
  "expiresIn": 900000
}
```

:::note
The user's role is encoded as a JWT claim and is **not** returned in the response body.
:::

---

## POST /auth/refresh

Rotates the refresh token and issues a fresh access token.

**Auth:** none (the refresh token in the body is the credential)

**Request body — `RefreshTokenRequestDto`**

| Field | Type |
| --- | --- |
| `refreshToken` | string |

```json
{
  "refreshToken": "b7c9f0a1-2d3e-4f50-8a1b-2c3d4e5f6071"
}
```

**Response `200 OK` — `LoginResponseDto`** (same shape as login).

**Errors:** invalid, expired, or revoked token → refresh fails.

---

## POST /auth/logout

Revokes a single refresh-token session.

**Auth:** none (refresh token identifies the session)

**Request body — `RefreshTokenRequestDto`**

```json
{
  "refreshToken": "b7c9f0a1-2d3e-4f50-8a1b-2c3d4e5f6071"
}
```

**Response:** `204 No Content` (empty body).

---

## POST /auth/logout-all

Revokes every active session for the authenticated user.

**Auth:** required — `Authorization: Bearer <access token>`

```http
POST /auth/logout-all
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**Response:** `204 No Content` (empty body).

---

## Flow: register → profile creation

```mermaid
sequenceDiagram
  autonumber
  participant C as Client
  participant AUTH as auth-service
  participant DB as bookstore_auth_db
  participant USER as user-service

  C->>AUTH: POST /auth/register
  AUTH->>DB: insert auth_users (BCrypt password)
  AUTH->>USER: POST /api/user/create (X-Internal-Api-Key)
  USER-->>AUTH: UserResponseDto
  AUTH-->>C: 200 RegisterResponseDto
```
