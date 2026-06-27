# Contributing to Advanced Fintech Wallet API

Thank you for your interest in contributing. This is a production-grade fintech backend built to be a reference implementation for Java engineers — the more people who contribute, the stronger it gets.

This guide gets you from zero to a running local environment in about 10 minutes.

---

## What this project is

A highly secure, high-performance digital wallet backend engine built with **Java 21, Spring Boot 3.5, PostgreSQL, Redis, and Docker**. It implements the **Immutable Double-Entry Ledger Pattern** for financial transaction integrity, with JWT authentication, HikariCP connection pooling, and an LLM-powered financial advisor via Groq/Llama.

The goal is to be the clearest, most production-honest example of a fintech backend in the Java ecosystem. Every architectural decision prioritises correctness and security over convenience.

---

## Ways to contribute

- **⭐ Star the repo** — helps others find it
- **🐛 Report a bug** — open an issue with reproduction steps
- **📖 Improve documentation** — Swagger descriptions, README, inline comments
- **🧪 Write tests** — especially concurrency tests and edge cases
- **🔧 Pick up a good-first-issue** — see below

---

## Good first issues

Look for the `good-first-issue` label on the [issues page](https://github.com/sumituppal03/advanced-wallet-ledger-api/issues).

### Beginner-friendly

| Task | Description |
|------|-------------|
| Improve Swagger endpoint descriptions | Many endpoints have minimal descriptions — add real request/response examples |
| Add `@NotNull` / `@Positive` validation to transfer request DTO | Validate amount > 0, accountId not null before hitting the service layer |
| Write a README section on the double-entry ledger pattern | Explain what it is and why it matters for financial systems, with a worked example |
| Add GitHub topics to repo | `java`, `spring-boot`, `fintech`, `wallet`, `ledger`, `microservices` |

### Intermediate

| Task | Description |
|------|-------------|
| Add idempotency key support to transfer endpoint | Allow clients to retry failed requests safely without double-spending |
| Add transaction history pagination | Currently returns flat list — add cursor-based or offset pagination |
| Write Testcontainers integration tests | Replace H2 with a real PostgreSQL container for integration tests |
| Add a `GET /balance` endpoint with Redis cache-aside pattern | Return current balance, cache in Redis with TTL, invalidate on transaction |
| Add structured request logging with MDC correlation IDs | Each request should carry a trace ID through all log statements |

### Advanced

| Task | Description |
|------|-------------|
| Implement account statement PDF generation | Generate a downloadable PDF statement for a date range using Apache PDFBox |
| Add event sourcing for the ledger | Instead of mutable state, derive balance from an immutable event log |
| Add multi-currency support | Handle FX conversion with configurable exchange rates |
| Implement spending analytics endpoint | Aggregate transactions by category, return weekly/monthly summary |

---

## Local setup

### Prerequisites

- Java 21+
- Maven 3.9+
- Docker and Docker Compose
- A Groq API key (free at https://console.groq.com) — optional, only for the AI advisor feature

### Step 1 — Clone

```bash
git clone https://github.com/sumituppal03/advanced-wallet-ledger-api.git
cd advanced-wallet-ledger-api/wallet
```

### Step 2 — Start PostgreSQL and Redis

```bash
docker-compose up -d
```

### Step 3 — Set environment variables

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/walletdb
export SPRING_DATASOURCE_USERNAME=postgres
export SPRING_DATASOURCE_PASSWORD=postgres
export JWT_SECRET=your-256-bit-secret-here
export GROQ_API_KEY=your_groq_key   # optional
export SPRING_DATA_REDIS_HOST=localhost
```

### Step 4 — Run

```bash
./mvnw spring-boot:run
```

API at `http://localhost:8080`. Swagger UI at `http://localhost:8080/swagger-ui.html`.

---

## Running the tests

```bash
cd wallet
./mvnw clean test
```

The test suite includes `WalletServiceConcurrencyTest` — a multi-threaded test that verifies pessimistic locking blocks double-spending under high concurrent load. This is the most important test in the project. Please don't break it.

---

## How to submit a PR

1. Fork the repo, create a branch: `git checkout -b feat/your-feature`
2. Make your changes with focused, well-named commits
3. Add or update tests
4. Run `./mvnw clean test` — all tests must pass
5. Open a PR against `main` describing what you changed and why

PRs are reviewed within 48 hours.

---

## Questions?

- GitHub: [@sumituppal03](https://github.com/sumituppal03)
- LinkedIn: [sumit-uppal03](https://www.linkedin.com/in/sumit-uppal03/)
- Email: sumituppal2004@gmail.com

---

*This is a production-grade reference implementation, not a tutorial project. Code quality and correctness are the bar — bring your best thinking.*
