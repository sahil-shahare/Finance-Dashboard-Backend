# Finance Dashboard Backend

> Role-based Finance Data Processing and Access Control API — built with **Spring Boot 3**, **MySQL**, **Redis**, **Razorpay**, and **Google Gemini AI**.

[![Java](https://img.shields.io/badge/Java-17-orange)](https://openjdk.org/projects/jdk/17/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-green)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8-blue)](https://www.mysql.com/)
[![Redis](https://img.shields.io/badge/Redis-Optional-red)](https://redis.io/)

---

## Table of Contents
- [Overview](#overview)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Features](#features)
- [Setup & Running](#setup--running)
- [Role-Based Access Control](#role-based-access-control)
- [API Reference](#api-reference)
- [AI Integration](#ai-integration)
- [Razorpay Payment Flow](#razorpay-payment-flow)
- [Redis Caching](#redis-caching)
- [Design Decisions](#design-decisions)
- [Running Tests](#running-tests)

---

## Overview

A production-style backend REST API for managing financial transactions with strict role-based access control. Built to demonstrate real-world backend engineering — not just CRUD, but security, caching, third-party payments, AI integration, and testability.

**Business purpose:** An internal finance tool where bookkeepers, analysts, and admins each have scoped access — mirroring the *segregation of duties* principle used in real accounting systems.

**Frontend:** A standalone `index.html` (zero build step) with Chart.js dashboards, full Razorpay checkout flow, and AI-powered financial insights.

---

## Tech Stack

| Layer | Technology | Why |
|---|---|---|
| Language | Java 17 | LTS, text blocks, records |
| Framework | Spring Boot 3.2 | Auto-configuration, production-ready |
| Security | Spring Security 6 + JWT (JJWT 0.11) | Stateless auth, scales horizontally |
| Database | MySQL 8 + Spring Data JPA / Hibernate 6 | ACID guarantees for financial data |
| Caching | Redis + Spring Cache | Reduces DB load on aggregation queries |
| Payments | Razorpay SDK 1.4.3 | HMAC-SHA256 tamper-proof verification |
| AI | Google Gemini API (free tier) | Insights, chat, auto-categorize |
| Testing | JUnit 5 + Mockito + AssertJ | Service isolation, no DB required |
| Build | Maven 3.8+ | Dependency management |

---

## Project Structure

```
src/main/java/com/finance/dashboard/
├── config/
│   ├── ApplicationConfig.java       # UserDetailsService bean
│   ├── CacheConstants.java          # Cache name constants registry
│   ├── CorsConfig.java              # CorsConfigurationSource (not CorsFilter)
│   ├── RazorpayConfig.java          # RazorpayClient singleton bean
│   ├── RedisConfig.java             # CachingConfigurer + graceful fallback
│   └── SecurityConfig.java          # Route-level RBAC + JWT filter chain
├── controller/
│   ├── AiController.java            # /api/ai/**
│   ├── AuthController.java          # /api/auth/**
│   ├── CacheController.java         # /api/cache/** (Admin)
│   ├── DashboardController.java     # /api/dashboard/**
│   ├── PaymentController.java       # /api/payments/**
│   ├── TransactionController.java   # /api/transactions/**
│   └── UserController.java          # /api/users/** (Admin)
├── dto/
│   ├── request/                     # Validated inbound payloads
│   └── response/                    # Outbound shapes — never exposes passwords
├── exception/
│   ├── GlobalExceptionHandler.java  # Maps all exceptions to structured JSON
│   ├── ConflictException.java
│   ├── PaymentException.java
│   └── ResourceNotFoundException.java
├── model/
│   ├── Payment.java                 # Razorpay payment lifecycle entity
│   ├── Transaction.java             # Soft-delete support
│   ├── User.java                    # Implements UserDetails
│   └── enums/                       # Role, TransactionType, UserStatus, PaymentStatus
├── repository/
│   ├── TransactionRepository.java   # Custom JPQL aggregations
│   ├── TransactionSpecification.java # Composable JPA Specification filters
│   ├── PaymentRepository.java
│   └── UserRepository.java
├── security/
│   ├── JwtAuthFilter.java           # OncePerRequestFilter — validates every request
│   └── JwtUtil.java                 # Token generation and validation
└── service/
    ├── AiService.java               # Gemini API — insights, chat, categorize
    ├── AuthService.java
    ├── DashboardService.java        # Aggregations + cache management
    ├── PaymentService.java          # Order creation + HMAC verification
    ├── TransactionService.java      # CRUD + cache eviction
    └── UserService.java
```

---

## Features

- **JWT Authentication** — stateless, 24h expiry, BCrypt password hashing (cost 10)
- **3-tier RBAC** — VIEWER / ANALYST / ADMIN enforced at route AND method level
- **Soft Delete** — transactions never physically removed, `deleted=true` flag preserves audit trail
- **Dynamic Filtering** — JPA Specification composes 4 optional filters without combinatorial repository methods
- **Dashboard Analytics** — income, expenses, net balance, category breakdowns, 12-month trends via JPQL aggregations
- **Redis Caching** — `@Cacheable` on all reads, `@CacheEvict` on writes, graceful fallback to MySQL when Redis is down
- **Razorpay Payments** — create-order → Razorpay checkout → HMAC-SHA256 verify, FAILED status saved before exception for audit
- **AI Assistant** — financial health insights, multi-turn chat, transaction auto-categorization
- **26 unit tests** — fully Mockito-isolated, no running database required

---

## Setup & Running

### Prerequisites

- Java 17+
- Maven 3.8+
- MySQL 8
- Redis (optional — app works without it)

### 1. Clone and configure

```bash
git clone https://github.com/sahilshahare/finance-dashboard.git
cd finance-dashboard
cp src/main/resources/application.properties.example src/main/resources/application.properties
```

Edit `application.properties` — fill in:
```properties
spring.datasource.password=YOUR_MYSQL_PASSWORD
razorpay.key.id=rzp_test_XXXXXXXX
razorpay.key.secret=YOUR_SECRET
gemini.api.key=YOUR_GEMINI_KEY    # Free at aistudio.google.com/apikey
```

### 2. Create database

```sql
CREATE DATABASE finance_dashboard;
```

Hibernate auto-creates all tables on first run (`ddl-auto=update`).

### 3. Run

```bash
mvn clean install
mvn spring-boot:run
```

API available at `http://localhost:8080`

### 4. Seed admin user

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","email":"admin@example.com","password":"admin123","role":"ADMIN"}'
```

### 5. Open frontend

Open `index.html` in browser — no build step, no npm.

---

## Role-Based Access Control

| Endpoint | VIEWER | ANALYST | ADMIN |
|---|:---:|:---:|:---:|
| `POST /api/auth/register|login` | Y | Y | Y |
| `GET /api/transactions/**` | Y | Y | Y |
| `POST|PUT|DELETE /api/transactions/**` | N | N | Y |
| `GET /api/dashboard/summary` | Y | Y | Y |
| `GET /api/dashboard/trends|categories` | N | Y | Y |
| `GET /api/payments/my` | Y | Y | Y |
| `POST /api/payments/create-order|verify` | Y | Y | Y |
| `GET /api/payments` (all users) | N | N | Y |
| `GET /api/payments/revenue` | N | N | Y |
| `GET|POST /api/ai/**` | Y | Y | Y |
| `/api/users/**` | N | N | Y |
| `/api/cache/**` | N | N | Y |

---

## API Reference

All responses use a uniform envelope:

**Success:**
```json
{ "success": true, "message": "Success", "data": { ... } }
```
**Error:**
```json
{ "success": false, "message": "Human-readable error description", "data": null }
```

### Auth

| Method | Endpoint | Body |
|---|---|---|
| POST | `/api/auth/register` | `{username, email, password, role}` |
| POST | `/api/auth/login` | `{username, password}` |

### Transactions

| Method | Endpoint | Notes |
|---|---|---|
| GET | `/api/transactions` | Params: `type, category, startDate, endDate, page, size` |
| GET | `/api/transactions/{id}` | Cached in Redis — 5 min TTL |
| POST | `/api/transactions` | ADMIN only — evicts dashboard cache |
| PUT | `/api/transactions/{id}` | ADMIN only — partial update, null fields skipped |
| DELETE | `/api/transactions/{id}` | ADMIN only — soft delete |

### Dashboard

| Method | Endpoint | Auth |
|---|---|---|
| GET | `/api/dashboard/summary` | All roles |
| GET | `/api/dashboard/trends` | ANALYST, ADMIN |
| GET | `/api/dashboard/categories` | ANALYST, ADMIN |

### Payments

| Method | Endpoint | Notes |
|---|---|---|
| POST | `/api/payments/create-order` | Returns `orderId + keyId` for Razorpay SDK |
| POST | `/api/payments/verify` | HMAC-SHA256 signature verification |
| GET | `/api/payments/my` | Paginated own payment history |
| GET | `/api/payments` | ADMIN — all payments |
| GET | `/api/payments/revenue` | ADMIN — sum of SUCCESS payments |

---

## AI Integration

Three endpoints powered by Google Gemini API (free tier). Get key at: https://aistudio.google.com/apikey

```
GET  /api/ai/insights       # Financial health analysis from live DB data
POST /api/ai/chat           # Multi-turn Q&A with transaction context injected
POST /api/ai/categorize     # Suggest category from transaction description
```

**Chat:**
```json
{ "message": "What is my biggest expense?", "history": [] }
```

**Categorize:**
```json
{ "description": "Amazon groceries", "type": "EXPENSE" }
```

App starts and runs fully without AI key — endpoints return a helpful message instead of crashing (`@Value("${gemini.api.key:}")` default empty string).

---

## Razorpay Payment Flow

```
Frontend              Backend                 Razorpay
   |                     |                       |
   |-- POST /create-order->|                       |
   |                     |-- orders.create() ----->|
   |                     |<---------- orderId -----|
   |<-- {orderId, keyId} -|                       |
   |                     |                       |
   |-- Razorpay.open() --------------------------------->|
   |<-- {paymentId, signature} -------------------------|
   |                     |                       |
   |-- POST /verify ----->|                       |
   |                     |-- HMAC-SHA256 check   |
   |                     |-- save SUCCESS/FAILED |
   |<-- {status: SUCCESS}-|                       |
```

**Critical detail:** `paymentRepository.save(FAILED)` is called **before** throwing `PaymentException` on invalid signature — ensures failed payments are persisted for audit trail even if the exception propagates up.

---

## Redis Caching

| Cache | TTL | Evicted when |
|---|---|---|
| `dashboard_summary` | 10 min | Any transaction write |
| `dashboard_trends` | 10 min | Any transaction write |
| `category_totals` | 10 min | Any transaction write |
| `transaction_by_id` | 5 min | Transaction updated or deleted |
| `user_by_id` | 30 min | User updated or deleted |
| `payment_by_id` | 15 min | Payment verified |

**Graceful fallback:** `RedisConfig` implements `CachingConfigurer` and overrides `errorHandler()`. Redis failures are logged as WARN and swallowed — app falls back to MySQL transparently. A standalone `@Bean CacheErrorHandler` is silently ignored by Spring and does NOT work.

---

## Design Decisions

**Soft delete** — Financial records must never be permanently destroyed. `deleted=true` preserves data for audit trails while hiding it from all normal queries.

**BigDecimal for money** — `double`/`float` cannot represent decimal fractions exactly (`0.1 + 0.2 = 0.30000000000000004`). `BigDecimal` stores exact values — non-negotiable for financial calculations.

**JPA Specification** — 4 optional transaction filters would require 16 repository methods if written exhaustively. One `Specification` composes predicates dynamically — cleaner and more scalable.

**Two-layer RBAC** — Route-level rules in `SecurityConfig` block by HTTP method and path. `@EnableMethodSecurity` allows `@PreAuthorize` for fine-grained method-level control. Defense in depth.

**CorsConfigurationSource not CorsFilter** — In Spring Boot 3 / Spring Security 6, a bare `CorsFilter @Bean` registers at order 0 (after Security at -100). OPTIONS preflight requests get 401/403 before CORS headers are set. `CorsConfigurationSource` integrates inside the Security filter chain and handles preflight correctly.

**PUT behaves like PATCH** — All update DTOs treat every field as optional. Only non-null fields are applied — clients don't resend unchanged data.

**No Lombok** — Annotation processor was unreliable on the Windows development environment. All classes use explicit builders, getters, and setters for full control and zero hidden magic.

---

## Running Tests

```bash
mvn test
```

**26 tests — all pass without a running database or Redis.**

| Test Class | Coverage |
|---|---|
| `AuthServiceTest` | register success, duplicate username, duplicate email, login success, wrong password |
| `TransactionServiceTest` | list, get by id (found/not found), create, partial update, soft-delete success/not-found |
| `DashboardServiceTest` | net balance, negative balance, recent transactions, monthly trend merging, empty trends |
| `PaymentServiceTest` | user not found, amount to paise, order not found, already verified, invalid signature (FAILED saved before throw), get by id, revenue sum, revenue zero |

---

## Author

**Sahil Shahare**
B.Tech Computer Science — G. H. Raisoni College of Engineering, Nagpur (2021–2025)

[GitHub](https://github.com/sahilshahare380) · [LinkedIn](https://linkedin.com/in/sahilshahare) · [Email](mailto:sahilshahare380@gmail.com)
