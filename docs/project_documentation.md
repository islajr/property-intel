# Property Intelligence Platform - Integration Reference Guide

*Document Version: 2.1 — June 2026*  
*Target Release: API Core v4.0.5*

This document serves as the official, public-facing Integration Reference Guide for the Property Intelligence Platform. It provides a rigorous technical specification of the platform's API endpoints, authentication flows, rate-limiting mechanics, database constraints, caching rules, and local development configurations.

---

## 1. Platform Identity & System Architecture

The Property Intelligence Platform structures, normalizes, and tracks Nigerian residential property listings from multiple portals. It serves as an analytics, valuation, and alerting layer, and **does not** facilitate real estate transactions or listing uploads.

```mermaid
graph TD
    Scraper[Python Scraper PS-0] -->|Scrapes & Deduplicates| DB[(PostgreSQL + PostGIS)]
    Client[Web Browser / API Client] -->|HTTPS Requests| Controller[Spring Boot Controllers]
    Controller -->|Call Domain logic| Service[Spring Boot Service Layer]
    Service -->|Query / Persist| DB
    Service -->|Read / Write Cache| Redis[(Redis Cache & Rate Limiting)]
    Filter[JwtFilter & RateLimitFilter] -->|Intercept Requests| Controller
    GoSearch[Go Search Service] <-->|Stage 2: Concurrent Query| Service
```

### Core Coding & Integration Constraints
- **Asymmetric Encryption (RS256):** Authentication tokens are signed using a private RSA key and verified with a public key.
- **Kobo Currency Representation:** All monetary values in requests and responses are integers representing **kobo** ($1\text{ Naira} = 100\text{ kobo}$). Floating-point values (floats/doubles) are strictly prohibited for prices to prevent rounding discrepancies.
- **snake_case Convention:** All JSON keys in API payloads are serialized in `snake_case`.
- **Constructor Injection:** The core API enforces strict separation of concerns with final dependencies resolved via constructor injection.

---

## 2. Security & Authentication Flow

The platform uses a stateless JWT-based authentication system with rotating refresh tokens.

```
Client                              API Gateway / Filter Chain
  |                                            |
  |--- 1. POST /auth/login (Credentials) ---->| (Authenticates User)
  |<-- 2. HTTP 200 (Access Token + Cookie) ----| (Sets HttpOnly Refresh Cookie)
  |                                            |
  |--- 3. GET /protected (Bearer Token) ------>| (Verifies RS256 Signature)
  |<-- 4. HTTP 200 OK (Protected Data) --------|
  |                                            |
  |--- 5. POST /auth/refresh (Expired JWT) --->| (Reads HttpOnly Refresh Cookie)
  |<-- 6. HTTP 200 OK (New Token + Cookie) ----| (Rotates Cookie Token)
```

### Access Tokens (JWT)
- **Algorithm:** RS256 (RSA Signature with SHA-256)
- **Token Location:** Passed in the `Authorization` request header as a Bearer token:
  ```http
  Authorization: Bearer eyJhbGciOiJSUzI1Ni...
  ```
- **Lifespan:** 15 minutes (`900` seconds).

### Refresh Tokens
- **Rotation:** Every call to the `/auth/refresh` endpoint invalidates the old refresh token and issues a new one.
- **Location:** Sent from the server in a secure cookie payload and must be returned automatically by the client.
- **Cookie Directives:**
  - `HttpOnly`: Access from JavaScript is blocked.
  - `Secure`: Transmitted only over HTTPS.
  - `SameSite=Strict`: Protected against Cross-Site Request Forgery (CSRF).
  - `Path=/api/v1/auth` (or `/auth`): Scope-restricted.
- **Lifespan:** 7 days (`604800` seconds).

---

## 3. Rate Limiting & Interceptors

Every request processed by the API traverses a rate-limiting filter integrated with Bucket4j. Limits are bucketed by caller tier and tracked via client IP addresses or JWT principal IDs.

### Refill Tiers & Limits

| Tier | Endpoint Scope | Bucket Capacity | Refill Speed | Refill Interval |
| :--- | :--- | :--- | :--- | :--- |
| `AUTH` | `/api/v1/auth/login`, `/register` | 10 tokens | 10 tokens | 1 minute |
| `PUBLIC` | All unauthenticated endpoints | 60 tokens | 60 tokens | 1 minute |
| `AUTHENTICATED` | All authenticated endpoints | 100 tokens | 100 tokens | 1 minute |

### Rate Limit HTTP Headers
All rate-limited responses return the following tracking headers:
- `X-Rate-Limit-Remaining`: The number of tokens left in the current bucket.
- `X-Rate-Limit-Retry-After-Seconds`: If the bucket is exhausted, this specifies the remaining seconds until a refill occurs.

### Idempotency Protection
Write operations (such as creating search alerts) use a hashing interceptor. Duplicate requests sent within a 5-second window containing identical payloads and headers will be blocked with an `HTTP 409 Conflict` to prevent duplicate persistence.

---

## 4. API Endpoints Specification

### 4.1. Authentication Interface

#### 1. Authenticate Credentials (Login)
- **Route:** `POST /api/v1/auth/login` (supports non-prefixed `POST /auth/login`)
- **Headers:** `Content-Type: application/json`
- **Request Payload:**
  ```json
  {
    "email": "user@example.com",
    "password": "SecretPassword123!"
  }
  ```
- **Response Contracts:**
  - **Success (`200 OK`):**
    - *Cookies Set:* `refreshToken=<token_string>; Path=/api/v1/auth; HttpOnly; Secure; SameSite=Strict`
    - *Body Payload:*
      ```json
      {
        "access_token": "eyJhbGciOiJSUzI1Ni...",
        "expires_in": 900
      }
      ```
  - **Validation Failure (`400 Bad Request`):**
    ```json
    {
      "error": "BAD_REQUEST",
      "message": "Validation failed for fields: {email=must be a well-formed email address}",
      "path": "/api/v1/auth/login",
      "timestamp": "2026-06-19T17:30:00Z"
    }
    ```
  - **Auth Failure (`401 Unauthorized`):**
    ```json
    {
      "error": "UNAUTHORIZED",
      "message": "Incorrect username or password",
      "path": "/api/v1/auth/login",
      "timestamp": "2026-06-19T17:30:05Z"
    }
    ```

#### 2. Register New User
- **Route:** `POST /api/v1/auth/register` (supports non-prefixed `POST /auth/register`)
- **Headers:** `Content-Type: application/json`
- **Request Payload:**
  ```json
  {
    "email": "newuser@example.com",
    "password": "SecretPassword123!"
  }
  ```
- **Response Contracts:**
  - **Success (`200 OK`):** Identical structure to [Login Success](#1-authenticate-credentials-login).
  - **Conflict (`400 Bad Request`):** Triggered if the email is already registered:
    ```json
    {
      "error": "BAD_REQUEST",
      "message": "Email is already registered",
      "path": "/api/v1/auth/register",
      "timestamp": "2026-06-19T17:31:00Z"
    }
    ```

#### 3. Refresh Access Token
- **Route:** `POST /api/v1/auth/refresh` (supports non-prefixed `POST /auth/refresh`)
- **Cookies Required:** `refreshToken=<token_string>`
- **Response Contracts:**
  - **Success (`200 OK`):** Rotates the refresh token cookie and returns a new access token payload.
  - **Expired/Revoked Token (`401 Unauthorized`):** If the token is invalid or expired:
    ```json
    {
      "error": "UNAUTHORIZED",
      "message": "Refresh token has expired or is invalid",
      "path": "/api/v1/auth/refresh",
      "timestamp": "2026-06-19T17:32:00Z"
    }
    ```

---

### 4.2. Listings Interface

#### 1. Retrieve Paginated Listings
- **Route:** `GET /api/v1/listings` (supports non-prefixed `GET /listings`)
- **Query Parameters:**
  - `neighbourhood` (string, optional): Filter by neighborhood (e.g. `Lekki`).
  - `type` (string, optional): Property type (e.g. `HOUSE`, `FLAT`, `DUPLEX`).
  - `min_price` (long, optional): Minimum price in kobo.
  - `max_price` (long, optional): Maximum price in kobo.
  - `min_beds` (integer, optional): Minimum bedrooms.
  - `max_beds` (integer, optional): Maximum bedrooms.
  - `price_reduced` (boolean, optional): Filter for listings with historical price reductions.
  - `limit` (integer, optional, default=20, max=50): Page size.
  - `cursor` (string, optional): Base64 pagination cursor.
- **Response Contract (`200 OK`):**
  ```json
  {
    "data": [
      {
        "id": 101,
        "external_id": "ext-101",
        "source": "jiji",
        "url": "http://jiji.ng/101",
        "title": "3 Bedroom Flat in Yaba",
        "price_kobo": 120000000,
        "price_formatted": "₦1,200,000.00",
        "property_type": "FLAT",
        "bedrooms": 3,
        "bathrooms": 3,
        "neighbourhood": "Yaba",
        "city": "Lagos",
        "lat": 6.5244,
        "lng": 3.3792,
        "listing_status": "ACTIVE"
      }
    ],
    "meta": {
      "count": 1,
      "next_cursor": "eyJpZCI6MTAxfQ==",
      "has_more": false
    }
  }
  ```

#### 2. Get Listing Detail Card
- **Route:** `GET /api/v1/listings/{id}` (supports non-prefixed `GET /listings/{id}`)
- **Path Variable:** `id` (long, required)
- **Response Contract (`200 OK`):**
  ```json
  {
    "id": 101,
    "source": "jiji",
    "url": "http://jiji.ng/101",
    "title": "3 Bedroom Flat in Yaba",
    "price_kobo": 120000000,
    "price_formatted": "₦1,200,000.00",
    "property_type": "FLAT",
    "bedrooms": 3,
    "bathrooms": 3,
    "neighbourhood": "Yaba",
    "city": "Lagos",
    "listing_status": "ACTIVE",
    "listing_history": [
      {
        "old_value": null,
        "new_value": 120000000,
        "event_type": "LISTED",
        "event_date": "2026-06-01T12:00:00Z"
      }
    ]
  }
  ```
- **Error Contract (`404 Not Found`):**
  ```json
  {
    "error": "NOT_FOUND",
    "message": "Listing with id 999 not found",
    "path": "/api/v1/listings/999",
    "timestamp": "2026-06-19T17:35:00Z"
  }
  ```

#### 3. Post Geofenced Search (Nearby Listings)
Performs a radial lookup centered on a specific coordinate.
- **Route:** `POST /api/v1/listings/nearby` (supports non-prefixed `POST /listings/nearby`)
- **Headers:** `Content-Type: application/json`
- **Request Payload:**
  ```json
  {
    "lat": 6.4281,
    "lng": 3.4219,
    "radius_metres": 2500.0,
    "limit": 10
  }
  ```
- **Response Contract (`200 OK`):** Returns an array of listings located within the specified radius (calculated using the Haversine formula):
  ```json
  [
    {
      "id": 102,
      "title": "Lekki Apartment",
      "price_kobo": 500000000,
      "neighbourhood": "Lekki",
      "city": "Lagos",
      "lat": 6.4281,
      "lng": 3.4219
    }
  ]
  ```

---

### 4.3. Market Analytics Interface

#### 1. Retrieve All Neighbourhood Trends
- **Route:** `GET /api/v1/market/neighbourhoods` (supports non-prefixed `GET /market/neighbourhoods`)
- **Query Parameters:**
  - `sort_by` (string, optional, default="neighbourhood"): Sorting method. Options: `neighbourhood | new_listings | price_reduced | median_price | active_listings`.
  - `limit` (integer, optional, default=20, max=50): Items per page.
  - `cursor` (string, optional): Pagination cursor.
- **Response Contract (`200 OK`):**
  ```json
  {
    "data": [
      {
        "neighbourhood": "Ajah",
        "city": "Lagos",
        "active_listing_count": 142,
        "median_price_kobo": 45000000,
        "formatted_median_price": "₦450,000.00"
      }
    ],
    "meta": {
      "count": 1,
      "next_cursor": "eyJuYW1lIjoiQWphaCJ9",
      "has_more": false
    }
  }
  ```

#### 2. Get Specific Neighbourhood Stats
- **Route:** `GET /api/v1/market/{neighbourhood}/stats` (supports non-prefixed `GET /market/{neighbourhood}/stats` or `/market/stats` with a query param)
- **Response Contract (`200 OK`):**
  ```json
  {
    "neighbourhood": "Ajah",
    "city": "Lagos",
    "median_price_kobo": 45000000,
    "formatted_median_price": "₦450,000.00",
    "active_listing_count": 142,
    "avg_days_on_market": 18.4,
    "new_listings_count": 12,
    "price_reduced_count": 8,
    "price_percentiles": {
      "p25": 30000000,
      "p50": 45000000,
      "p75": 70000000,
      "p90": 110000000
    }
  }
  ```

---

### 4.4. Search Interface

#### 1. Search Query
- **Route:** `GET /api/v1/search` (supports non-prefixed `GET /search`)
- **Query Parameters:** Same filters as `/api/v1/listings`.
- **Response Contract (`200 OK`):** Returns an array of search results matching the query criteria:
  ```json
  {
    "data": [
      {
        "id": 101,
        "title": "3 Bedroom Flat in Yaba",
        "price_kobo": 120000000,
        "neighbourhood": "Yaba",
        "city": "Lagos"
      }
    ],
    "meta": {
      "count": 1,
      "next_cursor": null,
      "has_more": false
    }
  }
  ```

---

### 4.5. Alerts Interface

#### 1. Create Search Alert
Registers a search filter profile for a user. Real-time listing updates matching this profile will trigger email notifications.
- **Route:** `POST /alerts`
- **Headers:** 
  - `Content-Type: application/json`
  - `Authorization: Bearer <jwt_token>` (Required)
- **Request Payload:**
  ```json
  {
    "neighbourhood": "Lekki",
    "max_price_kobo": 200000000,
    "min_bedrooms": 3,
    "property_type": "HOUSE"
  }
  ```
- **Response Contract (`210 Created`):**
  ```json
  {
    "id": "fc72421a-d605-41c5-bfca-7f04feedad84",
    "user_id": "89fb475c-d38a-4bf2-be75-298ad8cfbf90",
    "neighbourhood": "Lekki",
    "max_price_kobo": 200000000,
    "min_bedrooms": 3,
    "property_type": "HOUSE"
  }
  ```
- **Error Contracts:**
  - **Missing Token (`401 Unauthorized`):**
    ```json
    {
      "error": "UNAUTHORIZED",
      "message": "Full authentication is required to access this resource",
      "path": "/alerts",
      "timestamp": "2026-06-19T17:40:00Z"
    }
    ```

#### 2. Delete Search Alert
Cancels and removes a search alert.
- **Route:** `DELETE /alerts/{id}`
- **Headers:** 
  - `Authorization: Bearer <jwt_token>` (Required)
- **Path Variable:** `id` (UUID format, required)
- **Response Contracts:**
  - **Success (`200 OK`):** Alert successfully deleted.
  - **Forbidden (`403 Forbidden`):** Triggered if an authenticated user attempts to delete an alert owned by another user:
    ```json
    {
      "error": "FORBIDDEN",
      "message": "You do not own this alert",
      "path": "/alerts/fc72421a-d605-41c5-bfca-7f04feedad84",
      "timestamp": "2026-06-19T17:41:00Z"
    }
    ```

---

## 5. Database Schema & Flyway Structure

Database updates are managed through versioned Flyway migrations located in `src/main/resources/db/migration/`.

### Migration Versions

| File | Migration Description | Core Tables / Indexes Created |
| :--- | :--- | :--- |
| `V1__init_auths.sql` | Initialize authentication schemas | `auths.users`, `auths.refresh_tokens` |
| `V2__scraped_listings.sql` | Primary data catalog tables | `raw_data.scraped_listings`, `raw_data.listing_history` |
| `V3__market_snapshots.sql` | Historical aggregate snapshots | `market.neighbourhood_snapshots` |
| `V4__add_fts_indexes.sql` | Full-text search engine indexes | Gin Index on `scraped_listings.search_vector` |
| `V7__create_alerts.sql` | Asynchronous alert configurations | `auths.alerts` with FK reference to `auths.users` |

### Database Constraint Logic

```
   [ auths.users ]
         |  1
         |
         |  0..*
   [ auths.alerts ] 
         | (Target neighbourhood, max price, bedrooms, property type)
```

- **Referential Integrity:** If a user account is deleted, all associated `auths.alerts` and `auths.refresh_tokens` records are deleted automatically (`ON DELETE CASCADE`).
- **Uniqueness Constraints:** Email addresses in the `auths.users` table are unique.
- **Temporal Indexes:** All query filters on price and creation dates use indexes to keep search response times within the target 200ms range.
