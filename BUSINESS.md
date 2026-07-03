# 💼 Business Context & Problem Statement

> This document explains **why** this project exists, **what problem** it solves, **who** it solves it for, and **how success is measured** — the context that turns a technical showcase into a production-grade system.

---

## 🔴 The Problem: Why Fintech Wallets Break Under Pressure

Digital wallet backends built on standard CRUD patterns have a well-documented failure mode: **they break under concurrent load in ways that silently cost money.**

When two requests — a transfer and a withdrawal — hit the same wallet simultaneously, a naive implementation reads the same balance twice, both see sufficient funds, and both write their deducted balance back. The wallet is now overdrawn. This isn't a theoretical edge case; it's a race condition that occurs at any meaningful traffic volume.

### The Real-World Cost

| Failure Mode | Root Cause | Business Impact |
|---|---|---|
| Double-spend / overdraft | No atomic locking on balance reads | Direct financial loss |
| Missing transaction history | Mutable ledger records overwritten | Audit failure, regulatory risk |
| Lost transactions under load | Connection pool exhaustion | Revenue loss, user trust damage |
| Stale balance reads | No caching strategy | Slow UX, repeated DB calls |
| Unexplained balance discrepancy | No double-entry validation | Compliance failure |

Most tutorial-grade wallet backends fail **all five** of these in production.

---

## ✅ The Solution: What This System Does Differently

This project replaces the standard mutable-record CRUD pattern with an **Immutable Double-Entry Ledger Architecture** — the same accounting principle used by every major financial institution.

### Core Design Decisions & Their Business Justifications

#### 1. Pessimistic Locking Over Optimistic Locking
**Technical choice:** `SELECT ... FOR UPDATE` on wallet rows before any balance mutation.

**Business reason:** In financial transactions, a failed optimistic lock means retrying a transfer that may have already partially executed. The user experience degrades (retries, errors) and reconciliation becomes complex. Pessimistic locking accepts a small throughput tradeoff in exchange for **absolute correctness guarantees** — the right choice when money is involved.

#### 2. Immutable Ledger Entries Over Mutable Balance Records
**Technical choice:** Every transaction appends a new ledger entry; no record is ever updated or deleted. The current balance is always derived from the ledger sum.

**Business reason:** A mutable balance field is a single point of truth that can be corrupted, and once corrupted there is no recovery path. An immutable ledger means **every balance discrepancy is auditable** — you can always reconstruct the exact sequence of events that produced any balance at any point in time. This is a regulatory requirement in any real fintech context (PCI-DSS, SOX, RBI guidelines).

#### 3. Redis TTL Caching on Transaction History
**Technical choice:** Transaction history responses cached in Redis with a configurable TTL.

**Business reason:** Transaction history is the highest-read, lowest-write endpoint in any wallet application. Without caching, every history request hits PostgreSQL — under load this becomes the bottleneck that degrades the entire service. Redis caching reduces database load for read-heavy operations while keeping data fresh within acceptable bounds.

#### 4. HikariCP Pool Tuning (`maximum-pool-size=20`)
**Technical choice:** Explicit connection pool configuration rather than defaults.

**Business reason:** Spring Boot's default HikariCP configuration is designed for general-purpose applications. Under concurrent financial transaction load, unconfigured pools either exhaust connections (requests queue and timeout) or hold idle connections that waste database resources. Explicit tuning is the difference between a system that works in demos and one that works under real traffic.

#### 5. Stateless JWT Authentication
**Technical choice:** Custom Spring Security filter chain with JWT; no server-side session storage.

**Business reason:** Stateful sessions don't scale horizontally. If the wallet API ever runs across multiple instances (load balanced), session state stored on one instance is invisible to others — users appear logged out on every request that hits a different node. JWT authentication ensures **every instance can validate any request independently**, making horizontal scaling seamless.

---

## 📊 Success Metrics

### What "Working" Means for This System

| Metric | Target | Why It Matters |
|---|---|---|
| Zero duplicate transactions under concurrent load | 0 duplicates across 50 concurrent threads | Direct financial correctness proof |
| Zero overdraft events | 0 overdrafts in concurrency tests | Core wallet integrity guarantee |
| Complete ledger auditability | Every balance reconstructable from entries | Regulatory compliance |
| No secrets in codebase | 0 hardcoded credentials | Security baseline for any real deployment |
| CI pipeline passes on every push | 100% automated validation | Code reliability gate |

### Performance Benchmarks (Live Deployment on Render Free Tier)

> **Note:** Results below are from the Render free tier (shared CPU, cold-start latency included). Production hardware would substantially improve these figures.

Run the concurrency test suite yourself:
```bash
cd wallet && mvn clean test -Dtest=WalletServiceConcurrencyTest
```

The test validates pessimistic locking correctness by firing concurrent transfer and withdrawal requests at the same wallet and asserting zero double-spend or overdraft events in the results.

---

## 🏢 Who Would Use This System

### Primary Users

**Fintech Startups Building Wallet Features**
Companies building neobanks, payment apps, or embedded finance products that need a backend wallet engine without building one from scratch. The API surface covers deposits, withdrawals, peer-to-peer transfers, and transaction history — the core operations any wallet product needs.

**Platform Engineers Evaluating Ledger Patterns**
Engineers deciding between a mutable-balance approach and a double-entry ledger approach for a new system. This project demonstrates the double-entry pattern implemented concretely in Spring Boot, with real ACID guarantees and a working concurrency test suite.

### Secondary Users

**Developers Learning Production Fintech Patterns**
The codebase explicitly demonstrates patterns that standard tutorials skip: pessimistic locking, immutable ledger design, connection pool tuning, JWT filter chain customization, and environment-based secret management.

---

## 🔄 Current Baseline vs. This System

| Approach | Race Condition Risk | Auditability | Scalability | Correctness Guarantee |
|---|---|---|---|---|
| Standard CRUD (mutable balance) | High — no atomic protection | None — records overwritten | Poor — no caching strategy | None — dependent on caller logic |
| Optimistic locking | Medium — retries on conflict | Partial | Moderate | Partial — conflicts cause retries |
| **This system (pessimistic + immutable ledger)** | **None — locked before read** | **Full — every event recorded** | **Good — Redis + pool tuning** | **Strong — enforced at DB level** |

---

## ⚠️ Constraints & Acknowledged Limitations

Being honest about limitations is part of production thinking.

**Free Tier Cold Start Latency**
The live Render deployment runs on a free tier instance that spins down after inactivity. First requests after a cold start will be slow (10–30 seconds). This is a hosting constraint, not an architectural one.

**AI Advisor Feature**
The Llama 3.3 spending analysis feature (via Groq Cloud) provides natural language summaries of transaction patterns. This is a UX enhancement, not a core financial feature. If the Groq API is unavailable, all core wallet operations remain unaffected.

**Single-Node Deployment**
The current architecture runs as a single application instance. Horizontal scaling would require extracting the pessimistic locking strategy to a distributed lock (Redis-based or database-level advisory locks) to maintain correctness across nodes. This is a known next step.

**No Webhook / Event Streaming**
Transaction events are not currently published to an event stream. A production system serving downstream services (notifications, analytics, fraud detection) would add Kafka or a webhook layer on top of the ledger writes.

---

## 🚀 Future Improvements (Prioritized)

| Priority | Improvement | Business Justification |
|---|---|---|
| 1 | Distributed locking for horizontal scale | Required before multi-instance deployment |
| 2 | Transaction event streaming (Kafka) | Enables downstream fraud detection, notifications |
| 3 | Per-user rate limiting (Bucket4j) | Prevents API abuse and velocity-based fraud |
| 4 | Webhook callbacks on transaction completion | Enables integration with partner systems |
| 5 | Multi-currency support with FX rate integration | Opens international use cases |
| 6 | Soft-delete with idempotency keys | Prevents duplicate submissions from retry storms |

---

## 📋 Design Decision Log

A record of non-obvious choices made during development, with the reasoning that would be asked in a technical interview.

| Decision | Alternative Considered | Why This Choice Won |
|---|---|---|
| Pessimistic locking | Optimistic locking with retry | Financial correctness > throughput; retries add latency and complexity |
| Immutable ledger entries | Mutable balance field | Auditability and recovery path outweigh storage overhead |
| Redis caching for history | No caching | History is the highest-read endpoint; DB load reduction is measurable |
| JWT (stateless auth) | Session-based auth | Horizontal scalability requires stateless validation |
| H2 in-memory DB for tests | Testcontainers with real PostgreSQL | Faster CI iteration; behavior difference acceptable for unit/integration tests |
| Environment variables for secrets | `.env` file in repo | Zero secrets in version control is a non-negotiable security baseline |

---

*This document follows the production project documentation standard outlined in [The Complete Guide to Building Production-Grade ML Projects](https://github.com/sumituppal03/advanced-wallet-ledger-api) — adapted for a backend systems context.*