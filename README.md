# Property Intelligence Platform

[![Build Status](https://github.com/islajr/property-intel/actions/workflows/ci.yml/badge.svg)](https://github.com/islajr/property-intel/actions)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

A real-time data intelligence and market analytics engine for the residential real-estate market in Nigeria. The platform structures, normalizes, and tracks property listings from multiple portals over time, providing APIs for listings, neighbourhood-level statistics, percentiles, and market trends.

> [!NOTE]
> This is a **data intelligence platform**, not a marketplace. It does not facilitate property transactions, host user listing uploads, or collect photographs. Instead, it serves as the analytics and valuation layer for Nigerian residential properties.

---

## 🏗️ High-Level System Architecture

The platform uses a decoupled microservice architecture:

```mermaid
graph TD
    Scraper[Python Scraper PS-0] -->|Scrapes & Normalizes| DB[(PostgreSQL + PostGIS)]
    Client[Web Browser / API Client] -->|HTTPS Requests| Controller[Spring Boot REST Controllers]
    Controller -->|Call Domain Logic| Service[Spring Boot Service Layer]
    Service -->|Query & Persist| DB
    Service -->|Read / Write Cache| Redis[(Redis Cache & Rate Limiting)]
    Filter[JwtFilter & RateLimitFilter] -->|Intercept Requests| Controller
    GoSearch[Go Search Service] <-->|Stage 2: Concurrent Query| Service
```

### Core Components
1. **Python Data Pipeline (PS-0)**: Runs scheduled scraper processes against major Nigerian listing portals, geocodes addresses, and streams normalized properties into the database.
2. **Spring Boot Core API**: Primary REST API engine built on **Java 21** and **Spring Boot 4.0.5** conforming to strict production boundaries (constructor injection, records for DTOs, and representing all prices as `BIGINT` in **kobo**).
3. **Redis Cache & Rate Limiting**: Utilizes Redis for high-performance query caching (composite SpEL key normalization with 6-hour TTL) and Bucket4j rate-limiting.
4. **PostgreSQL + PostGIS**: Datastore spatially indexed with PostGIS for geo-distance lookups (`ST_DWithin` / Haversine) and full-text index search engines (`tsvector`).

---

## ⚡ Key Technical Features

- **JWT RS256 Authentication**: Stateless authentication using asymmetric key pairs with rotating refresh tokens stored in HttpOnly cookies.
- **Distributed Rate Limiting**: Tiered Bucket4j rate limiters (Auth, Public, Authenticated) integrated with Redis.
- **Idempotent Requests**: Request interception using custom hashing interceptors to block duplicate write requests.
- **Asynchronous Alerts**: Event-driven alert matcher that allows users to create search filters (neighbourhood, price, property type, bedrooms) and receive automated notifications.
- **Nearby Listing Search**: Radial coordinate search using spherical geo-distance (Haversine formula) to locate properties within a given radius.

---

## 🧪 Verification & Local Testing

### Isolated Test Execution (Testcontainers)
To overcome database seeding challenges, the project enforces production test discipline using **Testcontainers**. You do **not** need a running PostgreSQL database or Redis instance on your machine to verify the code:

1. **Prerequisites**: Ensure you have [Docker](https://www.docker.com/) running locally.
2. **Running Tests**:
   Execute the following maven command in the `api` folder. It will dynamically spin up an isolated, real `postgis/postgis` PostgreSQL container and an alpine Redis container, execute the schema migrations, and verify the backend API behaviors:
   ```bash
   ./mvnw clean test
   ```

---

## 📖 Integration & API Specifications

A complete reference guide, including database schemas, Flyway migrations, environment parameters, and detailed API specs for auth, listings, market trends, and geofencing, is available in the dedicated documentation directory:

👉 **[Detailed Project Integration Guide](https://github.com/islajr/property-intel/blob/master/docs/project_documentation.md)**
