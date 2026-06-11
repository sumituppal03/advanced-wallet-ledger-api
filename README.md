# 💰 Advanced Fintech Wallet API

# 💰 Advanced Fintech Wallet API

A highly secure, high-performance distributed backend architecture for a digital wallet application. Built using Spring Boot, PostgreSQL, and Redis cache, featuring advanced JWT authorization and an integrated LLM-powered financial advisor assistant.

🚀 **Live API Deployment URL:** https://advanced-wallet-ledger-api.onrender.com  
📖 **Live API Documentation (Swagger UI):** https://advanced-wallet-ledger-api.onrender.com/swagger-ui.html

💻 **Local Development API Documentation:** http://localhost:8080/swagger-ui.html
---

## 🏛️ System Architecture

This system leverages a decoupled cloud architecture designed to ensure transactional integrity, lightning-fast data retrieval, and secure communication.

* **Core Engine:** Spring Boot 3.x (Java 17) handling REST controllers, security routing, and financial business logic.
* **Persistent Storage:** Managed PostgreSQL on Render with custom HikariCP connection pooling tuned for low latency.
* **Caching Layer:** Distributed Redis cluster to optimize frequently read wallet transaction histories and reduce database load.
* **AI Engine:** Spring AI abstraction layer connected to Groq Cloud running Llama 3.3 (70B) for automated customer spending analysis.

---

## ✨ Key Features

* **Secure Ledger Processing:** Full ACID-compliant transaction tracking for transfers, deposits, and withdrawals.
* **Stateless Security Framework:** Enterprise-grade implementation of JWT authentication with customized Spring Security filter chains.
* **High-Concurrency Performance:** Optimized HikariCP connection pool parameters (`maximum-pool-size=20`) alongside Redis TTL caching strategies to prevent database choking during peak traffic.
* **Automated API Contract Lifecycle:** Native Swagger/OpenAPI integration running on a custom `/v3/api-docs` layout for rapid front-end onboarding and endpoint testing.
* **Cloud-Native Security:** Production-grade DevOps environment configuration ensuring zero hardcoded secrets in the code repository (`application.properties` utilizes runtime environment variables).

---

## 🛠️ Tech Stack & Ecosystem

* **Framework:** Spring Boot 3.x, Spring Data JPA, Spring Security, Spring AI
* **Database:** PostgreSQL
* **Caching:** Redis Cache Server
* **API Management:** Springdoc OpenAPI UI (Swagger)
* **Deployment Platform:** Render Cloud Infrastructure
* **Build Tool & Language:** Maven, Java 17

---

## ⚙️ Environment Configurations & Security Policies

This application strictly enforces secure DevOps practices. To spin up this project in a continuous integration or deployment pipeline, ensure the following environment keys are populated within your hosting platform:

| Environment Variable | Description |
| :--- | :--- |
| `SPRING_DATASOURCE_URL` | JDBC target connection address pointing to the active private network database |
| `SPRING_DATASOURCE_USERNAME` | Restricted user profile handling database operations |
| `SPRING_DATASOURCE_PASSWORD` | Encrypted high-entropy connection key |
| `JWT_SECRET` | Custom cryptographic hash signature for signing Web Tokens |
| `GROQ_API_KEY` | Bearer authorization token for integrated cloud AI operations |
| `SPRING_DATA_REDIS_HOST` | Hostname address mapping to the active Redis caching partition |

---

## 🚀 Local Installation & Execution

### Prerequisites
* JDK 17 or higher
* Apache Maven
* Local instances of PostgreSQL and Redis running

### Setup Steps
1. **Clone the repository:**
   ```bash
   git clone [https://github.com/YOUR_GITHUB_USERNAME/YOUR_REPO_NAME.git](https://github.com/YOUR_GITHUB_USERNAME/YOUR_REPO_NAME.git)
   cd YOUR_REPO_NAME
