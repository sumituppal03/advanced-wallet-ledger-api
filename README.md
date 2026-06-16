# ⚡ Advanced Wallet Ledger API

> A production-grade fintech wallet backend that eliminates double-spend and overdraft under concurrent load — using an **Immutable Double-Entry Ledger** and **Pessimistic Locking**, not hope.

[![CI](https://github.com/sumituppal03/advanced-wallet-ledger-api/actions/workflows/ci.yml/badge.svg)](https://github.com/sumituppal03/advanced-wallet-ledger-api/actions/workflows/ci.yml)
[![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-brightgreen?logo=spring)](https://spring.io/projects/spring-boot)
[![Docker](https://img.shields.io/badge/Docker-ready-blue?logo=docker)](https://hub.docker.com/)
[![Live Demo](https://img.shields.io/badge/Live-Render-46E3B7?logo=render)](https://advanced-wallet-ledger-api.onrender.com)

---

## The Problem

Standard CRUD-based wallet backends have a well-known failure mode under concurrent load:

```
Thread A reads balance: $100  ─┐
Thread B reads balance: $100  ─┤  (both see enough funds)
Thread A withdraws $80        ─┤
Thread B withdraws $80        ─┘  ← wallet overdrawn by $60. Money lost.
```

This isn't theoretical — it happens at any real traffic volume. And when a mutable balance field gets corrupted, there's no audit trail. **The history is gone.**

This system solves both problems at the database level.

---

## What This Does Differently

| Approach | Race Condition | Audit Trail | Recovery Path |
|---|---|---|---|
| Standard CRUD (mutable balance) | ❌ High risk | ❌ None | ❌ None |
| Optimistic locking | ⚠️ Retry storms | ⚠️ Partial | ⚠️ Complex |
| **This system** | ✅ Eliminated | ✅ Full | ✅ Reconstructable |

**Two architectural guarantees:**

1. **Pessimistic locking** (`SELECT ... FOR UPDATE`) — wallet row is exclusively locked before any balance read. No concurrent thread can read a stale value.
2. **Immutable double-entry ledger** — every transaction appends a new entry. Nothing is ever updated or deleted. Current balance = sum of all ledger entries. Always.

---

## Tech Stack

| Layer | Technology | Why |
|---|---|---|
| Runtime | Java 21 + Spring Boot 3.x | Virtual threads, modern records, production-hardened |
| Database (prod) | PostgreSQL | ACID guarantees, row-level locking, managed backups on Render |
| Database (local) | MySQL 8.0 via Docker Compose | Fast local spin-up, JPA handles dialect differences |
| Cache | Redis | TTL-based caching on high-read history endpoints |
| Auth | Spring Security + JWT | Stateless — scales horizontally without session affinity |
| Connection pool | HikariCP (pool-size=20) | Tuned for concurrent financial transaction load |
| AI Advisor | Llama 3.3 (70B) via Groq + Spring AI | Natural language spending analysis; model-swappable via config |
| Container | Multi-stage Docker (Maven → JRE) | Minimal runtime image, no build tools in prod |
| CI/CD | GitHub Actions | Runs full test suite including concurrency tests on every push |
| Deployment | Render | Live at `advanced-wallet-ledger-api.onrender.com` |

---

## API Reference

### Authentication

```http
POST /api/auth/register
POST /api/auth/login
```

All subsequent requests require:
```
Authorization: Bearer <token>
```

---

### Wallet Operations

```http
POST   /api/wallet/deposit          # Add funds
POST   /api/wallet/withdraw         # Withdraw funds
POST   /api/wallet/transfer         # Peer-to-peer transfer
GET    /api/wallet/balance          # Current balance (live, never cached)
GET    /api/wallet/transactions     # Transaction history (Redis-cached)
```

**Transfer request:**
```json
POST /api/wallet/transfer
{
  "toUserId": 42,
  "amount": 150.00,
  "description": "Rent split"
}
```

**Response:**
```json
{
  "transactionId": "txn_9f3a2b",
  "status": "COMPLETED",
  "fromBalance": 350.00,
  "toBalance": 650.00,
  "timestamp": "2026-03-05T14:23:01Z"
}
```

---

### AI Spending Advisor

```http
GET /api/ai/advisor       # Natural language analysis of your spending patterns
```

**Response:**
```json
{
  "analysis": "Over the last 30 days, your largest spend category is transfers 
               ($1,240 — 62% of outflows). Three transactions over $300 occurred 
               on weekends. Your average daily balance has declined 18% month-over-month.",
  "model": "llama-3.3-70b-versatile",
  "generatedAt": "2026-03-05T14:23:05Z"
}
```

> The AI advisor is a UX layer. All core wallet operations work independently — no Groq dependency in the financial path.

---

## Quick Start

### Option 1: Docker Compose (recommended)

```bash
git clone https://github.com/sumituppal03/advanced-wallet-ledger-api.git
cd advanced-wallet-ledger-api

# Copy and fill in your environment variables
cp .env.example .env

docker-compose up --build
```

API is running at `http://localhost:8080`

---

### Option 2: Run locally with Maven

**Prerequisites:** Java 21, Maven 3.9+, PostgreSQL or MySQL running locally

```bash
git clone https://github.com/sumituppal03/advanced-wallet-ledger-api.git
cd advanced-wallet-ledger-api/wallet

# Set required environment variables
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/walletdb
export SPRING_DATASOURCE_USERNAME=your_user
export SPRING_DATASOURCE_PASSWORD=your_password
export JWT_SECRET=your_256_bit_secret
export GROQ_API_KEY=your_groq_api_key
export SPRING_DATA_REDIS_HOST=localhost

mvn spring-boot:run
```

---

### Environment Variables

| Variable | Description | Required |
|---|---|---|
| `SPRING_DATASOURCE_URL` | JDBC connection string | ✅ |
| `SPRING_DATASOURCE_USERNAME` | Database user | ✅ |
| `SPRING_DATASOURCE_PASSWORD` | Database password | ✅ |
| `JWT_SECRET` | 256-bit signing key for JWT tokens | ✅ |
| `GROQ_API_KEY` | Groq Cloud API key for AI advisor | ✅ |
| `SPRING_DATA_REDIS_HOST` | Redis hostname | ✅ |

> **No secrets in this repository.** All credentials are injected at runtime via environment variables.

---

## Run the Tests

```bash
cd wallet
mvn clean test
```

**Run the concurrency test specifically:**
```bash
mvn clean test -Dtest=WalletServiceConcurrencyTest
```

This test fires concurrent deposit, withdrawal, and transfer requests at the same wallet across 50 threads and asserts:
- **Zero duplicate transactions**
- **Zero overdraft events**
- **Ledger sum == wallet balance** at end of all operations

If pessimistic locking breaks, this test fails loudly. That's the point.

---

## Project Structure

```
advanced-wallet-ledger-api/
│
├── wallet/                          # Spring Boot application
│   ├── src/main/java/
│   │   └── com/wallet/
│   │       ├── controller/          # REST endpoints (Auth, Wallet, AI)
│   │       ├── service/             # Business logic + pessimistic locking
│   │       ├── repository/          # JPA repositories
│   │       ├── model/               # Wallet, LedgerEntry, User entities
│   │       ├── security/            # JWT filter chain, Spring Security config
│   │       ├── config/              # Redis config, HikariCP tuning, AI config
│   │       └── dto/                 # Request/response objects
│   │
│   ├── src/test/java/
│   │   └── com/wallet/
│   │       └── WalletServiceConcurrencyTest.java   # The important one
│   │
│   └── src/main/resources/
│       ├── application.properties   # Base config (no secrets)
│       └── application-test-env.properties  # H2 config for CI
│
├── Dockerfile                       # Multi-stage: Maven build → JRE runtime
├── docker-compose.yml               # Local dev: MySQL + Redis + app
├── .github/workflows/ci.yml         # GitHub Actions CI pipeline
└── BUSINESS.md                      # Problem context & design decisions
```

---

## Architecture

```
Client Request
      │
      ▼
┌─────────────────┐
│  Spring Security │  ← JWT validation (stateless)
│  Filter Chain   │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  REST Controller │  ← Input validation, request mapping
└────────┬────────┘
         │
         ▼
┌─────────────────────────────────────┐
│           Wallet Service             │
│                                     │
│  SELECT wallet FOR UPDATE  ◄──────  │  ← Pessimistic lock acquired
│  Validate sufficient funds          │
│  Write LedgerEntry (immutable)      │
│  Update wallet balance              │
│  COMMIT                  ──────►    │  ← Lock released
└────────┬────────────────────────────┘
         │
         ├──► PostgreSQL (ledger entries + wallet state)
         │
         └──► Redis (invalidate transaction history cache)
```

**Transaction history reads:**
```
GET /wallet/transactions
      │
      ▼
Redis cache HIT? ──► Return cached response (fast path)
      │
     MISS
      │
      ▼
PostgreSQL query ──► Store in Redis with TTL ──► Return response
```

---

## CI Pipeline

Every push to `main` triggers:

1. Provision JDK 21 environment
2. Spin up H2 in-memory database under `test-env` profile
3. Mock Spring AI `ChatModel` bean (no Groq API key needed in CI)
4. Run full test suite including `WalletServiceConcurrencyTest`
5. Build passes or merge is blocked

No secrets required in CI. No external database dependency. Every run starts clean.

---

## Design Decisions

Full rationale for every architectural choice — why pessimistic over optimistic locking, why immutable ledger, why dual database strategy, why multi-stage Docker — is documented in [`Buisness.md`](./BUSINESS.md).

The short version: **every decision optimises for financial correctness and production reliability over development convenience.**

---

## Known Limitations

**Render free tier cold start** — the live deployment spins down after inactivity. First request after idle takes 10–30 seconds. This is a hosting constraint, not architectural.

**Single-node deployment** — horizontal scaling requires replacing row-level pessimistic locking with distributed locks (Redis `SETNX` or PostgreSQL advisory locks).

**No idempotency keys** — duplicate API requests from client retries can create duplicate transactions. A production-ready next step.

**MySQL (local) vs PostgreSQL (production)** — JPA handles the dialect difference transparently, but full environment parity would use PostgreSQL in `docker-compose.yml` as well.

---

## Roadmap

- [ ] Idempotency keys on write endpoints
- [ ] PostgreSQL in `docker-compose.yml` for full local/prod parity
- [ ] Per-user rate limiting with Bucket4j
- [ ] Transaction event streaming (Kafka) for fraud detection downstream
- [ ] Distributed locking for horizontal scale
- [ ] Webhook callbacks on transaction settlement
- [ ] Multi-currency support with live FX rates

---

## Live Demo

API is deployed at: **`https://advanced-wallet-ledger-api.onrender.com`**

> Free tier — allow 15–30 seconds on first request if the instance has spun down.

Swagger UI (interactive API docs): **` https://advanced-wallet-ledger-api.onrender.com/swagger-ui.html`**

---

## Contributing

See [`CONTRIBUTING.md`](./CONTRIBUTING.md) for guidelines. Issues labelled [`good first issue`](https://github.com/sumituppal03/advanced-wallet-ledger-api/labels/good%20first%20issue) are a good starting point.

---

## Author

**Sumit Uppal** — Backend Engineer  
[GitHub](https://github.com/sumituppal03) · [LinkedIn](https://www.linkedin.com/in/sumit-uppal03/)

---

*Built to production standard — not tutorial standard. See [`Buisness.md`](./BUSINESS.md) for the full problem context and design rationale.*