# Bookstore Account Frontend

Premium authentication and profile management UI for the Bookstore microservices stack.

## Scope

- Login / registration
- Authenticated application shell
- View and edit user profile

Does **not** include bookstore browsing, cart, orders, or payments.

## Stack

- React 19 + TypeScript
- Vite
- React Router
- Talks to API Gateway at `http://localhost:8080`

## Run

```bash
# From repo root — start gateway, auth-service, user-service first
cd frontend
npm install
npm run dev
```

App: `http://localhost:5173`

## Architecture

```
src/
├── api/           # HTTP client + auth/user services
├── auth/          # AuthProvider, JWT helpers, protected routes
├── features/      # Login, Register, Profile feature modules
├── components/ui/ # Shared form/UI primitives
├── layouts/       # Auth layout + app shell
├── pages/         # Route-level pages
└── types/         # Shared TypeScript contracts
```

Token refresh is centralized in `src/api/client.ts`. Authenticated requests that receive `401` call `POST /auth/refresh`, retry once, and clear the session if refresh fails.

## API mapping

| UI action | Gateway route |
|-----------|---------------|
| Register | `POST /auth/register` |
| Login | `POST /auth/login` |
| Refresh | `POST /auth/refresh` |
| Logout | `POST /auth/logout` |
| Get profile | `GET /api/user/{userId}` |
| Update profile | `PUT /api/user/update/{userId}` |
