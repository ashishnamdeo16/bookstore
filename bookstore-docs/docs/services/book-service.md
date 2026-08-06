---
title: Book Service
---

# Book Service

## Purpose

`book-service` is the catalog system for books and supporting reference data.

## Responsibilities

- CRUD for books
- CRUD for authors
- CRUD for categories
- CRUD for publishers
- Batch fetch books by IDs for checkout/order flows

## Dependencies

- Port: `8083`
- Database: `bookstore_books_db`

## REST APIs

### Books

| Method | Path | Auth | Description |
| --- | --- | --- | --- |
| `POST` | `/api/books/create` | Admin | Create book |
| `PUT` | `/api/books/update/{id}` | Admin | Update book |
| `DELETE` | `/api/books/{id}` | Admin | Delete book |
| `GET` | `/api/books/{id}` | Yes | Get one book |
| `POST` | `/api/books/batch` | Yes | Fetch many books by UUID list |
| `GET` | `/api/books/` | Yes | List all books |

### Authors

- `POST /api/authors/create` (Admin)
- `PUT /api/authors/update/{id}` (Admin)
- `GET /api/authors/{id}`
- `DELETE /api/authors/{id}` (Admin)
- `GET /api/authors/`

### Categories

- `POST /api/categories/create` (Admin)
- `PUT /api/categories/update/{id}` (Admin)
- `GET /api/categories/{id}`
- `DELETE /api/categories/{id}` (Admin)
- `GET /api/categories/`

### Publishers

- `POST /api/publishers/create` (Admin)
- `PUT /api/publishers/update/{id}` (Admin)
- `GET /api/publishers/{id}`
- `DELETE /api/publishers/{id}` (Admin)
- `GET /api/publishers/`

### Book images

`BookImageController` exists with base path `/api/bookimage`, but there are no implemented endpoints in the current code.

## Database tables

- `books`
- `authors`
- `categories`
- `publishers`
- `book_images`
- join table `book_authors`

## Entity relationships

```mermaid
erDiagram
  CATEGORIES ||--o{ BOOKS : "category_id"
  PUBLISHERS ||--o{ BOOKS : "publisher_id"
  BOOKS }o--o{ AUTHORS : "book_authors"
  BOOKS ||--o{ BOOK_IMAGES : "book_id"
  BOOKS {
    uuid id PK
    string isbn
    string title
    string description
    decimal price
    string language
    date published_date
    uuid category_id FK
    uuid publisher_id FK
  }
  AUTHORS {
    uuid id PK
    string first_name
    string last_name
    string biography
    string country
  }
  CATEGORIES {
    uuid id PK
    string name
    string description
  }
  PUBLISHERS {
    uuid id PK
    string name
    string address
  }
  BOOK_IMAGES {
    uuid id PK
    uuid book_id FK
    string image_url
    string image_type
    boolean is_primary
  }
```

## Admin write flow

```mermaid
sequenceDiagram
  autonumber
  participant A as Admin (JWT ROLE_ADMIN)
  participant GW as api-gateway
  participant BOOK as book-service
  participant DB as bookstore_books_db

  A->>GW: POST /api/books/create
  GW->>BOOK: forward with Authorization
  BOOK->>BOOK: @PreAuthorize hasRole('ADMIN')
  BOOK->>DB: persist book + book_authors
  BOOK-->>A: 201 BookResponse
```

## Batch fetch (used by order & payment)

```mermaid
flowchart LR
  PAY[payment-service] -->|POST /api/books/batch List UUID| BOOK[book-service]
  ORD[order-service] -->|POST /api/books/batch| BOOK
  BOOK -->|List BookResponse| PAY
  BOOK -->|List BookResponse| ORD
```

## Security

Only `/actuator/health` and `/actuator/info` are public in the service security config. All catalog routes require authentication, and mutations additionally require admin privileges at the controller layer.
