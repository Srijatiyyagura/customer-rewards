# Customer Rewards Program Web API

A Spring Boot RESTful Web API for a retailer rewards program that calculates reward points earned by customers per month
and total, based on recorded purchase transactions.

---

## Table of Contents

- [Business Problem & Point Rules](#business-problem--point-rules)
- [Key Design Highlights](#key-design-highlights)
- [Architecture & Tech Stack](#architecture--tech-stack)
- [Production & Security Architecture](#production--security-architecture)
- [Prerequisites](#prerequisites)
- [Getting Started & Running the Application](#getting-started--running-the-application)
- [Running Unit & Integration Tests](#running-unit--integration-tests)
- [Interactive API Documentation (Swagger UI)](#interactive-api-documentation-swagger-ui)
- [Actuator & Health Checks](#actuator--health-checks)
- [H2 In-Memory Database Console](#h2-in-memory-database-console)
- [REST API Reference & Curl Examples](#rest-api-reference--curl-examples)
- [Preloaded Demonstration Dataset](#preloaded-demonstration-dataset)

---

## Business Problem & Point Rules

A retailer offers a rewards program to its customers, awarding points based on each recorded purchase transaction.

### Point Calculation Rules

- **2 points** for every dollar spent **over $100** in each transaction.
- **1 point** for every dollar spent **between $50 and $100** in each transaction.
- **0 points** for purchases of **$50.00 or less**.
- Points are earned on **whole dollars spent** per transaction (standard retail loyalty program rule).

### Calculation Examples & Edge Cases

| Purchase Amount | Calculation Breakdown                 | Points Earned | Notes                                         |
|:----------------|:--------------------------------------|:-------------:|:----------------------------------------------|
| **$45.00**      | Under $50 threshold                   |     **0**     | Under lower threshold                         |
| **$50.00**      | At lower threshold                    |     **0**     | Lower boundary condition                      |
| **$51.00**      | 1 x $1 ($50–$100 tier)                |     **1**     | First point earned                            |
| **$75.00**      | 1 x $25 ($50–$100 tier)               |    **25**     | Mid-tier spend                                |
| **$99.00**      | 1 x $49 ($50–$100 tier)               |    **49**     | Just under upper threshold                    |
| **$100.00**     | 1 x $50 ($50–$100 tier) + 0 over $100 |    **50**     | Upper threshold boundary ($50–$100 tier full) |
| **$101.00**     | 1 x $50 + 2 x $1 (over $100)          |    **52**     | First point in upper tier                     |
| **$120.00**     | 1 x $50 + 2 x $20 (over $100)         |    **90**     | Standard program example                      |
| **$200.00**     | 1 x $50 + 2 x $100 (over $100)        |    **250**    | High spend                                    |

---

## Key Design Highlights

The system has been architected to follow component-based software engineering principles and enterprise best practices:

1. **RESTful Resource Granularity**:
    - Provides both full-catalog aggregation (`GET /api/rewards`) and targeted customer-specific lookups (
      `GET /api/rewards/{customerId}`).
    - Path aliases without `/api` prefix (`/rewards`, `/rewards/{customerId}`) are supported for consumer convenience.

2. **Dynamic 3-Month Date Range Windowing**:
    - The rewards engine dynamically queries date ranges via repository methods (
      `findByCustomerIdAndTransactionDateBetween`).
    - Defaults to the active 3-calendar-month period without requiring older historical records in the database to be
      purged.

3. **UI-Optimized Response Models**:
    - Monthly reward breakdowns are serialized as a structured array of objects (`MonthlyRewardResponse`) containing
      `year`, `month`, `monthNumber`, `points`, `totalSpent`, and `transactionCount` for straightforward presentation in
      client interfaces.

4. **Strict Domain Encapsulation**:
    - All domain entities, DTOs, and service components enforce private field visibility with explicit accessor methods.
    - Clean, descriptive domain naming is used throughout the codebase (`RewardCalculationService`,
      `CustomerRewardResponse`, etc.).

5. **Precise Currency & Boundary Handling**:
    - Uses `BigDecimal` for transaction amounts to eliminate floating-point arithmetic inaccuracies.
    - Accurately computes points across all boundary points (e.g. $50.00, $100.00, $101.00, $120.00).

6. **Modern Java & Spring Boot Standards**:
    - Built on Java 17 and Spring Boot 3.3.4, leveraging modern `java.time` APIs (`LocalDate`, `YearMonth`) and Java
      records.

7. **Comprehensive Automated Test Coverage**:
    - Includes 57 automated tests spanning boundary verification, multi-month aggregations, MVC web slice tests, and
      full end-to-end integration tests.

---

## Architecture & Tech Stack

- **Java**: 17
- **Framework**: Spring Boot 3.3.4
- **Persistence**: Spring Data JPA with Hibernate
- **Database**: H2 In-Memory Database
- **Validation**: Jakarta Bean Validation (`@Valid`, `@NotNull`, `@DecimalMin`, etc.)
- **API Documentation**: Springdoc OpenAPI 3 / Swagger UI
- **Testing**: JUnit 5, AssertJ, Mockito, Spring Test (`@WebMvcTest`, `@SpringBootTest`)
- **Build Tool**: Apache Maven with Maven Wrapper (`./mvnw`)

### Project Package Structure

```
src/
├── main/
│   ├── java/com/retailer/rewards/
│   │   ├── CustomerRewardsApplication.java    # Application Entry Point
│   │   ├── config/
│   │   │   ├── DataInitializer.java          # Seed data with boundary test cases
│   │   │   └── OpenApiConfig.java            # Swagger / OpenAPI documentation
│   │   ├── controller/
│   │   │   ├── CustomerController.java       # /api/customers & /customers
│   │   │   ├── RewardController.java         # /api/rewards & /rewards
│   │   │   └── TransactionController.java    # /api/transactions & /transactions
│   │   ├── dto/
│   │   │   ├── CustomerRequest.java
│   │   │   ├── CustomerResponse.java
│   │   │   ├── CustomerRewardResponse.java   # Aggregated rewards response
│   │   │   ├── ErrorResponse.java            # Unified API error payload
│   │   │   ├── MonthlyRewardResponse.java    # UI-friendly monthly breakdown
│   │   │   ├── TransactionRequest.java
│   │   │   └── TransactionResponse.java
│   │   ├── entity/
│   │   │   ├── Customer.java                 # Customer JPA Entity
│   │   │   └── Transaction.java              # Transaction JPA Entity
│   │   ├── exception/
│   │   │   ├── CustomerNotFoundException.java
│   │   │   ├── GlobalExceptionHandler.java   # Centralized @RestControllerAdvice
│   │   │   ├── InvalidDateRangeException.java
│   │   │   └── ResourceNotFoundException.java
│   │   ├── repository/
│   │   │   ├── CustomerRepository.java
│   │   │   └── TransactionRepository.java    # Date range query methods
│   │   └── service/
│   │       ├── CustomerService.java
│   │       ├── RewardCalculationService.java # Pure calculation engine
│   │       ├── RewardService.java            # Multi-month aggregation
│   │       └── TransactionService.java
│   └── resources/
│       └── application.properties
└── test/
    └── java/com/retailer/rewards/
        ├── CustomerRewardsIntegrationTest.java   # Full end-to-end integration test
        ├── controller/
        │   ├── CustomerControllerTest.java       # Slice MVC tests
        │   ├── RewardControllerTest.java
        │   └── TransactionControllerTest.java
        └── service/
            ├── CustomerServiceTest.java
            ├── RewardCalculationServiceTest.java # Boundary & parameterized tests
            ├── RewardServiceTest.java
            └── TransactionServiceTest.java
```

---

## Production & Security Architecture

For local evaluation and automated testing, authentication challenges (such as basic auth or mandatory JWT authorization
headers) are disabled by default to facilitate immediate testing via Swagger UI and cURL. In an enterprise production
deployment, this microservice is designed to integrate into a standard zero-trust security perimeter:

### 1. Authentication & Authorization (OAuth 2.0 / OIDC)

- **Token Verification**: Integrate `spring-boot-starter-oauth2-resource-server` to validate JWT bearer tokens issued by
  an identity provider (Okta, Keycloak, or AWS Cognito).
- **Role-Based Access Control (RBAC)**:
    - `@PreAuthorize("hasRole('ADMIN')")`: Restricted write operations (e.g. `POST /api/transactions`, creating
      customers).
    - `@PreAuthorize("hasRole('CUSTOMER') and #customerId == authentication.principal.claims['customer_id']")`:
      Restricts customers to reading only their personal rewards data (`GET /api/rewards/{customerId}`).

### 2. Transport Security & Network Isolation

- **TLS/HTTPS**: Enforce HTTPS with TLS 1.3 across all environments.
- **HSTS Headers**: Configured via Spring Security HTTP security headers.
- **Private Subnets**: The application service and database reside in private VPC subnets behind an API Gateway /
  Ingress Controller.

### 3. Input Validation & Injection Defense

- **Jakarta Bean Validation**: All input DTOs (`TransactionRequest`, `CustomerRequest`) enforce strict validation (
  `@NotNull`, `@DecimalMin`, `@Email`, etc.) before reaching the service layer.
- **SQL / HQL Injection Prevention**: All persistence operations utilize Spring Data JPA / Hibernate parameterized
  queries (`:customerId`, `:startDate`, `:endDate`), completely preventing SQL injection.

### 4. Cross-Origin Resource Sharing (CORS)

- Configured
  via [WebConfig.java](file:///Users/venkat/Desktop/code/customer-rewards/src/main/java/com/retailer/rewards/config/WebConfig.java)
  with explicit allowed origins, methods, and headers to support web dashboards and Swagger UI.

### 5. Rate Limiting & Throttling

- Implemented at the API Gateway layer (or via Redis + Bucket4j token bucket filter) to guard against brute-force calls
  or calculation DoS attacks.

### 6. PII & Data Privacy (GDPR / CCPA)

- Customer PII (names, emails) masked in application logs.
- Audit logging for financial transaction modifications.

---

## Prerequisites

- **Java Development Kit (JDK)**: Version 17 or higher
- **Git**

*(Maven is not required to be installed globally; the project includes `./mvnw`)*

---

## Getting Started & Running the Application

### 1. Run using the Maven Wrapper:

```bash
./mvnw spring-boot:run
```

### 2. Alternatively, package and run as a JAR:

```bash
./mvnw clean package
java -jar target/customer-rewards-1.0.0.jar
```

The application starts on port `8080` by default.

---

## Running Unit & Integration Tests

Execute the full automated test suite (57 tests):

```bash
./mvnw clean test
```

### Test Suite Overview:

- `RewardCalculationServiceTest`: Parameterized and targeted tests verifying all boundaries ($0, $45, $50, $51, $75, $
  99, $100, $101, $120, $200), floor rounding on cents, and null/negative handling.
- `RewardServiceTest`: Verifies monthly grouping, total points aggregation, date range resolution, empty transaction
  handling, and 404/400 exceptions.
- `CustomerServiceTest` & `TransactionServiceTest`: Validates business operations, repository interactions, and domain
  rules.
- `RewardControllerTest`, `TransactionControllerTest`, `CustomerControllerTest`: Verifies HTTP status codes (200, 201,
  400, 404), route aliases (with and without `/api`), and JSON serialization.
- `CustomerRewardsIntegrationTest`: Boots the complete Spring application context with an in-memory database, validates
  seeded data calculations, creates live transactions, and validates point updates.

---

## Interactive API Documentation (Swagger UI)

Once the application is running, open your web browser to:

- **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI 3 JSON Spec**: [http://localhost:8080/api-docs](http://localhost:8080/api-docs)

You can explore all endpoints, view input models, and execute requests directly from the browser.

---

## Actuator & Health Checks

Spring Boot Actuator is configured to provide production-readiness health and liveness indicators:

- **Health Check Endpoint**: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)
- **Application Info Endpoint**: [http://localhost:8080/actuator/info](http://localhost:8080/actuator/info)

**Health Check Request**:

```bash
curl -s http://localhost:8080/actuator/health
```

**Response**:

```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "H2",
        "validationQuery": "isValid()"
      }
    },
    "diskSpace": {
      "status": "UP"
    },
    "ping": {
      "status": "UP"
    }
  }
}
```

---

## H2 In-Memory Database Console

- **URL**: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
- **JDBC URL**: `jdbc:h2:mem:rewardsdb`
- **User Name**: `sa`
- **Password**: *(leave blank)*

---

## REST API Reference & Curl Examples

> **Note**: Both `/api/rewards` and `/rewards`, as well as `/api/transactions` and `/transactions`, are supported for
> convenience.

### 1. Get Rewards for All Customers

Calculates the reward points earned per month and the overall total for each customer over the default 3-month window (
or optional custom date range).

**Request**:

```bash
curl -s http://localhost:8080/api/rewards
```

**Optional Date Filter Parameters**:

```bash
curl -s "http://localhost:8080/api/rewards?startDate=2026-07-01&endDate=2026-09-30"
```

**Example JSON Response**:

```json
[
  {
    "customerId": 1,
    "customerName": "Alice Johnson",
    "email": "alice.johnson@example.com",
    "monthlyRewards": [
      {
        "year": 2026,
        "month": "JULY",
        "monthNumber": 7,
        "points": 90,
        "totalSpent": 120.0,
        "transactionCount": 1
      },
      {
        "year": 2026,
        "month": "AUGUST",
        "monthNumber": 8,
        "points": 50,
        "totalSpent": 150.0,
        "transactionCount": 2
      },
      {
        "year": 2026,
        "month": "SEPTEMBER",
        "monthNumber": 9,
        "points": 250,
        "totalSpent": 245.0,
        "transactionCount": 2
      }
    ],
    "totalPoints": 390,
    "totalSpent": 515.0
  }
]
```

---

### 2. Get Rewards for Specific Customer

Retrieves monthly reward breakdowns and total points for a single customer by ID.

**Request**:

```bash
curl -s http://localhost:8080/api/rewards/1
```

*(Also available as: `curl -s http://localhost:8080/rewards/1`)*

**Example JSON Response**:

```json
{
  "customerId": 1,
  "customerName": "Alice Johnson",
  "email": "alice.johnson@example.com",
  "monthlyRewards": [
    {
      "year": 2026,
      "month": "JULY",
      "monthNumber": 7,
      "points": 90,
      "totalSpent": 120.0,
      "transactionCount": 1
    },
    {
      "year": 2026,
      "month": "AUGUST",
      "monthNumber": 8,
      "points": 50,
      "totalSpent": 150.0,
      "transactionCount": 2
    },
    {
      "year": 2026,
      "month": "SEPTEMBER",
      "monthNumber": 9,
      "points": 250,
      "totalSpent": 245.0,
      "transactionCount": 2
    }
  ],
  "totalPoints": 390,
  "totalSpent": 515.0
}
```

---

### 3. Get All Transactions

Retrieves transactions with optional filtering by `customerId` and/or date range.

**Request (All Transactions)**:

```bash
curl -s http://localhost:8080/api/transactions
```

**Request (Filtered by Customer)**:

```bash
curl -s "http://localhost:8080/api/transactions?customerId=1"
```

**Example JSON Response**:

```json
[
  {
    "id": 5,
    "customerId": 1,
    "customerName": "Alice Johnson",
    "amount": 45.0,
    "transactionDate": "2026-09-18",
    "description": "Coffee shop ($45 = 0 pts)",
    "pointsEarned": 0
  },
  {
    "id": 4,
    "customerId": 1,
    "customerName": "Alice Johnson",
    "amount": 200.0,
    "transactionDate": "2026-09-05",
    "description": "Department store ($200 = 250 pts)",
    "pointsEarned": 250
  },
  {
    "id": 2,
    "customerId": 1,
    "customerName": "Alice Johnson",
    "amount": 100.0,
    "transactionDate": "2026-08-10",
    "description": "Home goods ($100 = 50 pts)",
    "pointsEarned": 50
  },
  {
    "id": 1,
    "customerId": 1,
    "customerName": "Alice Johnson",
    "amount": 120.0,
    "transactionDate": "2026-07-12",
    "description": "Electronics purchase ($120 = 90 pts)",
    "pointsEarned": 90
  }
]
```

---

### 4. Record a New Purchase Transaction

Records a new purchase and immediately returns the calculated reward points earned for the transaction.

**Request**:

```bash
curl -s -X POST http://localhost:8080/api/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": 1,
    "amount": 120.00,
    "transactionDate": "2026-09-30",
    "description": "New Fall Jacket"
  }'
```

**Example JSON Response (HTTP 201 Created)**:

```json
{
  "id": 14,
  "customerId": 1,
  "customerName": "Alice Johnson",
  "amount": 120.0,
  "transactionDate": "2026-09-30",
  "description": "New Fall Jacket",
  "pointsEarned": 90
}
```

---

### 5. Customer Endpoints

#### Get All Customers

```bash
curl -s http://localhost:8080/api/customers
```

#### Get Customer By ID

```bash
curl -s http://localhost:8080/api/customers/1
```

#### Create a Customer

```bash
curl -s -X POST http://localhost:8080/api/customers \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Sarah Connor",
    "email": "sarah.connor@example.com"
  }'
```

---

## Preloaded Demonstration Dataset

On application startup, `DataInitializer` populates realistic demonstration data configured relative to the launch date
so the 3-month window is always active:

1. **Alice Johnson (ID: 1)** - Multi-month active customer:
    - 2 months ago: $120.00 purchase (**90 points** — 2x$20 + 1x$50)
    - 1 month ago: $100.00 purchase (**50 points** — full $50–$100 tier), $50.00 purchase (**0 points**)
    - Current month: $200.00 purchase (**250 points**), $45.00 purchase (**0 points**)
    - Total Points: **390 points**
2. **Bob Smith (ID: 2)** - Boundary condition tester:
    - 2 months ago: $51.00 (**1 point**), $75.00 (**25 points**)
    - 1 month ago: $99.00 (**49 points**), $101.00 (**52 points**)
    - Current month: $150.00 (**150 points**)
    - Total Points: **277 points**
3. **Charlie Brown (ID: 3)** - Single month activity:
    - 1 month ago: $80.00 (**30 points**)
    - Demonstrates months without activity are handled gracefully.
4. **Diana Prince (ID: 4)** - Historical transaction tester:
    - 4 months ago (outside 3-month window): $300.00 (**450 points**)
    - Current month: $110.00 (**70 points**)
    - Demonstrates that historical transactions older than 3 months are **automatically excluded** from the 3-month
      reward calculation without purging the database.
