# Finance Dashboard Backend

A role-based finance data processing and access control API built with **Spring Boot 3**, **MySQL**, and **JWT authentication**.

---

## Table of Contents

- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Setup & Running](#setup--running)
- [Role-Based Access Control](#role-based-access-control)
- [API Reference](#api-reference)
- [Design Decisions & Assumptions](#design-decisions--assumptions)
- [Running Tests](#running-tests)

---

## Tech Stack

| Layer      | Technology                        |
| ---------- | --------------------------------- |
| Language   | Java 17                           |
| Framework  | Spring Boot 3.2                   |
| Security   | Spring Security + JWT (JJWT 0.11) |
| Database   | MySQL 8                           |
| ORM        | Spring Data JPA / Hibernate       |
| Validation | Jakarta Bean Validation           |
| Build Tool | Maven                             |
| Testing    | JUnit 5 + Mockito + AssertJ       |

---

## Project Structure

```
src/main/java/com/finance/dashboard/
├── config/
│   ├── ApplicationConfig.java      # UserDetailsService bean
│   └── SecurityConfig.java         # Route-level access rules + JWT filter
├── controller/
│   ├── AuthController.java         # POST /api/auth/register|login
│   ├── UserController.java         # /api/users/** (ADMIN only)
│   ├── TransactionController.java  # /api/transactions/**
│   └── DashboardController.java    # /api/dashboard/**
├── dto/
│   ├── request/                    # Validated inbound payloads
│   └── response/                   # Outbound shapes (never exposes passwords)
├── exception/
│   ├── GlobalExceptionHandler.java # Maps every exception to structured JSON
│   ├── ResourceNotFoundException.java
│   └── ConflictException.java
├── model/
│   ├── User.java                   # Implements UserDetails
│   ├── Transaction.java            # Supports soft-delete
│   └── enums/                      # Role, TransactionType, UserStatus
├── repository/
│   ├── UserRepository.java
│   ├── TransactionRepository.java  # Custom JPQL for analytics
│   └── TransactionSpecification.java # Composable JPA filters
├── security/
│   ├── JwtUtil.java                # Token generation and validation
│   └── JwtAuthFilter.java          # OncePerRequestFilter
└── service/
    ├── AuthService.java
    ├── UserService.java
    ├── TransactionService.java
    └── DashboardService.java       # All aggregation logic lives here
```

---

## Setup & Running

### Prerequisites

- Java 17+
- Maven 3.8+
- MySQL 8 running locally

### 1. Create the database

```sql
CREATE DATABASE finance_dashboard;
```

### 2. Configure credentials

Edit `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/finance_dashboard?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=your_password
```

> Hibernate is set to `ddl-auto=update`, so all tables are created automatically on first run.

### 3. Build and run

```bash
mvn clean install
mvn spring-boot:run
```

The API is available at `http://localhost:8080`.

### 4. Seed an admin user

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "email": "admin@example.com",
    "password": "admin123",
    "role": "ADMIN"
  }'
```

Copy the `token` from the response and use it as `Authorization: Bearer <token>` on subsequent requests.

---

## Role-Based Access Control

| Endpoint                         | VIEWER | ANALYST | ADMIN |
| -------------------------------- | :----: | :-----: | :---: |
| `POST /api/auth/register`        |   Y    |    Y    |   Y   |
| `POST /api/auth/login`           |   Y    |    Y    |   Y   |
| `GET  /api/transactions`         |   Y    |    Y    |   Y   |
| `GET  /api/transactions/{id}`    |   Y    |    Y    |   Y   |
| `POST /api/transactions`         |   N    |    N    |   Y   |
| `PUT  /api/transactions/{id}`    |   N    |    N    |   Y   |
| `DELETE /api/transactions/{id}`  |   N    |    N    |   Y   |
| `GET  /api/dashboard/summary`    |   Y    |    Y    |   Y   |
| `GET  /api/dashboard/trends`     |   N    |    Y    |   Y   |
| `GET  /api/dashboard/categories` |   N    |    Y    |   Y   |
| `GET  /api/users`                |   N    |    N    |   Y   |
| `GET  /api/users/{id}`           |   N    |    N    |   Y   |
| `PUT  /api/users/{id}`           |   N    |    N    |   Y   |
| `DELETE /api/users/{id}`         |   N    |    N    |   Y   |

---

## API Reference

All successful responses follow this envelope:

```json
{
  "success": true,
  "message": "Success",
  "data": { ... }
}
```

All error responses:

```json
{
  "success": false,
  "message": "Human-readable error description",
  "data": null
}
```

---

### Auth

#### `POST /api/auth/register`

Creates a new user. Returns a JWT token immediately.

**Request body:**

```json
{
  "username": "alice",
  "email": "alice@example.com",
  "password": "pass1234",
  "role": "ANALYST"
}
```

> `role` accepts: `VIEWER`, `ANALYST`, `ADMIN`

**Response `201`:**

```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "token": "eyJhbGci...",
    "tokenType": "Bearer",
    "userId": 2,
    "username": "alice",
    "role": "ANALYST"
  }
}
```

---

#### `POST /api/auth/login`

**Request body:**

```json
{ "username": "alice", "password": "pass1234" }
```

**Response `200`:** Same shape as register.

**Error `401`** — wrong credentials:

```json
{ "success": false, "message": "Invalid username or password" }
```

---

### Transactions

All transaction endpoints require `Authorization: Bearer <token>`.

#### `GET /api/transactions`

Paginated list. All query params are optional.

| Param       | Type   | Example               |
| ----------- | ------ | --------------------- |
| `type`      | enum   | `INCOME` or `EXPENSE` |
| `category`  | string | `Salary`              |
| `startDate` | date   | `2025-01-01`          |
| `endDate`   | date   | `2025-03-31`          |
| `page`      | int    | `0` (default)         |
| `size`      | int    | `20` (default)        |

**Response `200`:**

```json
{
  "success": true,
  "message": "Success",
  "data": {
    "content": [
      {
        "id": 1,
        "amount": 5000.0,
        "type": "INCOME",
        "category": "Salary",
        "date": "2025-01-01",
        "notes": "January salary",
        "createdBy": "admin",
        "createdAt": "2025-01-01T10:00:00",
        "updatedAt": "2025-01-01T10:00:00"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1,
    "last": true
  }
}
```

---

#### `GET /api/transactions/{id}`

**Response `200`:** Single transaction object.
**Error `404`:** `{ "success": false, "message": "Transaction not found with id: 99" }`

---

#### `POST /api/transactions` _(ADMIN only)_

**Request body:**

```json
{
  "amount": 1200.5,
  "type": "EXPENSE",
  "category": "Rent",
  "date": "2025-02-01",
  "notes": "February rent payment"
}
```

**Response `201`:** The created transaction object.

---

#### `PUT /api/transactions/{id}` _(ADMIN only)_

All fields are optional — only provided fields are updated.

```json
{ "category": "Office Rent", "amount": 1300.0 }
```

**Response `200`:** Updated transaction object.

---

#### `DELETE /api/transactions/{id}` _(ADMIN only)_

Performs a **soft delete** — the record is flagged as deleted but remains in the database for audit purposes.

**Response `200`:**

```json
{ "success": true, "message": "Transaction deleted (soft)", "data": null }
```

---

### Dashboard

#### `GET /api/dashboard/summary` _(All roles)_

```json
{
  "data": {
    "totalIncome": 15000.00,
    "totalExpenses": 8500.00,
    "netBalance": 6500.00,
    "categoryTotals": [
      { "category": "Rent",   "type": "EXPENSE", "total": 3600.00 },
      { "category": "Salary", "type": "INCOME",  "total": 15000.00 }
    ],
    "recentTransactions": [ ... ]
  }
}
```

---

#### `GET /api/dashboard/trends` _(ANALYST, ADMIN)_

Monthly income vs expense for the past 12 months.

```json
{
  "data": {
    "trends": [
      {
        "year": 2025,
        "month": 1,
        "monthLabel": "Jan 2025",
        "income": 5000.0,
        "expenses": 2800.0,
        "net": 2200.0
      }
    ]
  }
}
```

---

#### `GET /api/dashboard/categories` _(ANALYST, ADMIN)_

Category totals broken out by type — suitable for pie and bar charts.

```json
{
  "data": [
    { "category": "Groceries", "type": "EXPENSE", "total": 450.0 },
    { "category": "Salary", "type": "INCOME", "total": 5000.0 }
  ]
}
```

---

### Users _(ADMIN only)_

#### `GET /api/users?page=0&size=20`

Paginated list of all users.

#### `GET /api/users/{id}`

Single user by ID.

#### `PUT /api/users/{id}`

Update any combination of `email`, `role`, `status`, or `password`. All fields are optional.

```json
{ "role": "ANALYST", "status": "INACTIVE" }
```

#### `DELETE /api/users/{id}`

Hard-deletes the user record.

---

## Design Decisions & Assumptions

### Soft Delete for Transactions

Transactions are never physically removed. Setting `deleted = true` hides a record from all queries while preserving it for auditing. This is a common requirement in financial systems where data integrity and traceability matter.

### Uniform API Response Envelope

Every endpoint — success or error — returns the same `{ success, message, data }` shape. This simplifies frontend error handling, as consumers always know the exact response structure regardless of HTTP status code.

### JPA Specification for Filtering

Rather than writing a separate repository method for every filter combination, a `Specification` is used to compose predicates dynamically. This keeps the repository clean and scales to more filter options without code duplication.

### Role Enforcement at Two Layers

- **Route level** — `SecurityConfig` blocks requests by HTTP method and path before they reach a controller.
- **Method level** — `@EnableMethodSecurity` is enabled, making it trivial to add `@PreAuthorize` guards on individual service methods if finer-grained control is needed later.

### Partial Updates (PUT behaves like PATCH)

`UpdateTransactionRequest` and `UpdateUserRequest` treat all fields as optional. Only non-null fields are applied. This avoids requiring the client to resend unchanged data and keeps API usage ergonomic.

### BigDecimal for All Monetary Values

`double` and `float` cannot represent decimal fractions precisely. All monetary amounts use `BigDecimal` with `precision=15, scale=2` to avoid floating-point rounding errors.

### Password Security

Passwords are hashed with BCrypt (cost factor 10 by default). The raw password is never stored or returned in any response DTO.

### JWT Expiry

Tokens expire after 24 hours (`jwt.expiration=86400000` ms). There is no refresh token in this implementation — adding one would be the natural next step for production use.

### Register Endpoint is Public

To simplify seeding and demo usage, `/api/auth/register` is left open. In a real system this would be locked to ADMIN only, with a separate admin-invite or onboarding flow for other roles.

---

## Running Tests

```bash
mvn test
```

The test suite covers:

- `AuthServiceTest` — registration (happy path, duplicate username, duplicate email), login (success, bad credentials)
- `TransactionServiceTest` — list, get by ID (found / not found), create, partial update, soft-delete (success / not found)
- `DashboardServiceTest` — net balance calculation, negative balance, recent transactions included, monthly trend merging, empty trend list

All tests use **Mockito** to isolate the service layer from the database, so no running MySQL instance is required to execute them.
