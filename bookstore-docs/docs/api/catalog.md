---
title: Catalog API
---

# Catalog API

Service: `book-service` (port `8083`). Covers four resource groups routed by the gateway: `/api/books`, `/api/authors`, `/api/categories`, `/api/publishers`.

**Security:** all catalog reads require `Authorization: Bearer <token>`; all writes additionally require `hasRole('ADMIN')`. Only `/actuator/health` and `/actuator/info` are public.

---

## Books — `/api/books`

| Method | Path | Auth | Body | Response |
| --- | --- | --- | --- | --- |
| POST | `/api/books/create` | ADMIN | `BookCreateRequest` | `BookResponse` (201) |
| PUT | `/api/books/update/{id}` | ADMIN | `BookCreateRequest` | `BookResponse` |
| DELETE | `/api/books/{id}` | ADMIN | — | raw string |
| GET | `/api/books/{id}` | authenticated | — | `BookResponse` |
| POST | `/api/books/batch` | authenticated | `List<UUID>` | `List<BookResponse>` |
| GET | `/api/books/` | authenticated | — | `List<BookResponse>` |

### `BookCreateRequest`

| Field | Type |
| --- | --- |
| `isbn` | string |
| `title` | string |
| `description` | string |
| `price` | number (BigDecimal) |
| `language` | string |
| `publishedDate` | string (`yyyy-MM-dd`) |
| `publisherId` | UUID |
| `categoryId` | UUID |
| `authorIds` | array&lt;UUID&gt; |

### `BookResponse`

| Field | Type |
| --- | --- |
| `id` | UUID |
| `isbn` | string |
| `title` | string |
| `description` | string |
| `price` | number |
| `language` | string |
| `publishedDate` | string (`yyyy-MM-dd`) |
| `publisherId` | UUID |
| `categoryId` | UUID |
| `authorIds` | array&lt;UUID&gt; |

### POST /api/books/create

```http
POST /api/books/create
Authorization: Bearer <admin token>
Content-Type: application/json
```

```json
{
  "isbn": "978-0135957059",
  "title": "The Pragmatic Programmer",
  "description": "Your journey to mastery.",
  "price": 39.99,
  "language": "en",
  "publishedDate": "2019-09-13",
  "publisherId": "9a1b2c3d-4e5f-4061-8273-8495a6b7c8d9",
  "categoryId": "1c2d3e4f-5061-4728-9384-95a6b7c8d9e0",
  "authorIds": ["2d3e4f50-6172-4839-9405-a6b7c8d9e0f1"]
}
```

**Response `201 Created`**

```json
{
  "id": "abcabc01-2345-4678-89ab-cdef01234567",
  "isbn": "978-0135957059",
  "title": "The Pragmatic Programmer",
  "description": "Your journey to mastery.",
  "price": 39.99,
  "language": "en",
  "publishedDate": "2019-09-13",
  "publisherId": "9a1b2c3d-4e5f-4061-8273-8495a6b7c8d9",
  "categoryId": "1c2d3e4f-5061-4728-9384-95a6b7c8d9e0",
  "authorIds": ["2d3e4f50-6172-4839-9405-a6b7c8d9e0f1"]
}
```

### POST /api/books/batch

Fetch many books at once — used by `order-service` and `payment-service`. The body is a **raw JSON array of UUIDs**, not a wrapped object.

```json
["abcabc01-2345-4678-89ab-cdef01234567", "bcdbcd12-3456-4789-9abc-def012345678"]
```

**Response `200 OK` — `List<BookResponse>`.**

### DELETE /api/books/&#123;id&#125;

**Response `200 OK` — raw string** `"Deleted SuccessFully"`.

---

## Authors — `/api/authors`

| Method | Path | Auth | Response |
| --- | --- | --- | --- |
| POST | `/api/authors/create` | ADMIN | `AuthorResponse` (201) |
| PUT | `/api/authors/update/{id}` | ADMIN | `AuthorResponse` |
| GET | `/api/authors/{id}` | authenticated | `AuthorResponse` |
| DELETE | `/api/authors/{id}` | ADMIN | raw string `"Success"` |
| GET | `/api/authors/` | authenticated | `List<AuthorResponse>` |

**`AuthorRequest`:** `firstName` (string), `lastName` (string), `biography` (string), `country` (string).
**`AuthorResponse`:** `id` (UUID) plus the same fields.

```json
{
  "firstName": "David",
  "lastName": "Thomas",
  "biography": "Co-author of The Pragmatic Programmer.",
  "country": "US"
}
```

---

## Categories — `/api/categories`

| Method | Path | Auth | Response |
| --- | --- | --- | --- |
| POST | `/api/categories/create` | ADMIN | `CategoryResponse` (201) |
| PUT | `/api/categories/update/{id}` | ADMIN | `CategoryResponse` |
| GET | `/api/categories/{id}` | authenticated | `CategoryResponse` |
| DELETE | `/api/categories/{id}` | ADMIN | raw string `"Success"` |
| GET | `/api/categories/` | authenticated | `List<CategoryResponse>` |

**`CategoryRequest`:** `name` (string), `description` (string).
**`CategoryResponse`:** `id` (UUID), `name`, `description`.

```json
{ "name": "Software Engineering", "description": "Books on building software." }
```

---

## Publishers — `/api/publishers`

| Method | Path | Auth | Response |
| --- | --- | --- | --- |
| POST | `/api/publishers/create` | ADMIN | `PublisherResponse` (201) |
| PUT | `/api/publishers/update/{id}` | ADMIN | `PublisherResponse` |
| GET | `/api/publishers/{id}` | authenticated | `PublisherResponse` |
| DELETE | `/api/publishers/{id}` | ADMIN | raw string `"Success"` |
| GET | `/api/publishers/` | authenticated | `List<PublisherResponse>` |

**`PublisherRequest`:** `name` (string), `address` (string).
**`PublisherResponse`:** `id` (UUID), `name`, `address`.

```json
{ "name": "Addison-Wesley", "address": "Boston, MA" }
```