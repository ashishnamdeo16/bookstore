---
title: Frontend
---

# Frontend

## Stack

- React `19.2.7`
- TypeScript `6.0.2`
- Vite `8.1.1`
- React Router DOM `7.18.1`
- Recharts `3.10.1`
- Stripe React SDK

## Application structure

Key folders:

- `src/api` - typed API wrappers over `apiClient`
- `src/auth` - auth provider, route guards, JWT/token helpers
- `src/components` - reusable UI and layout pieces
- `src/features` - cart, analytics, orders, profile
- `src/layouts` - auth/admin/customer shells
- `src/pages` - route-level pages
- `src/theme` - light/dark/system theme state

## Routes

### Customer routes

- `/dashboard`
- `/books`
- `/books/:id`
- `/cart`
- `/checkout`
- `/checkout/payment/:paymentId`
- `/payment-success`
- `/checkout/payment/:paymentId/failed`
- `/orders`
- `/orders/:orderId`
- `/profile`
- `/profile/edit`

### Admin routes

- `/admin/dashboard`
- `/admin/analytics`
- `/admin/books`
- `/admin/books/new`
- `/admin/books/:id/edit`
- `/admin/authors`
- `/admin/publishers`
- `/admin/categories`
- `/admin/users`

## API integration

`apiClient` uses `VITE_API_BASE_URL` and defaults to `http://localhost:8080`.

It:

- adds the bearer token when available
- retries once on `401` by calling `/auth/refresh`
- clears the session if refresh fails

## Checkout flow

1. Cart is stored in localStorage under `bookstore.cart`
2. Quantity is clamped to `1..2000`
3. Checkout page calls `POST /api/payments/checkout`
4. Payment page loads Stripe Elements with `VITE_STRIPE_PUBLISHABLE_KEY`
5. Payment success page polls `GET /api/orders/payment/{paymentId}` until the order appears

## Environment variables

`frontend/.env.example`:

```dotenv
VITE_API_BASE_URL=http://localhost:8080
VITE_STRIPE_PUBLISHABLE_KEY=pk_test_...
```

`VITE_STRIPE_PUBLISHABLE_KEY` is required for the payment page. Without it, Stripe Elements will not mount.

## Auth flow

1. Login stores access and refresh tokens in `localStorage`
2. A stable `deviceId` is generated and sent on login
3. On app load, `AuthProvider` restores the session from JWT or refresh token
4. `apiClient` retries once on `401` via `POST /auth/refresh`
5. Register completes with `POST /auth/register` and redirects to login without auto-login

## UI-specific behaviors currently in code

- Account dropdown with profile/order links and theme preference
- Customer-side labels were simplified to avoid “Customer Portal” branding
- Admin user management blocks self-delete in the UI
- Cart quantity limit warns inline and via toast when over `2000`
