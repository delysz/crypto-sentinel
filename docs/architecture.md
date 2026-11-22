# Project Architecture

This document gives a high‑level view of the main components of the **Crypto Sentinel** application. The goal is to help developers quickly understand how the system is structured and how the different layers interact.

## 1. Technology Stack

| Layer | Technology | Why it was chosen |
|-------|------------|-------------------|
| **Backend** | Java 21 + Spring Boot 3.4.3 | Modern, lightweight, strong dependency injection and security support |
| | Spring Web MVC | Standard MVC framework for REST and view rendering |
| | Spring Data JPA | Simple ORM over H2 database |
| | Spring Security | Built‑in authentication & authorization |
| | Spring Scheduler | Handles the periodic polling of CoinGecko |
| | Project Reactor / WebClient | Reactive HTTP client for external APIs |
| **Frontend** | Thymeleaf | Server‑side templating for a single‑page dashboard |
| | Bootstrap 5.3 + Chart.js | Responsive styling & sparklines |
| **Infrastructure** | Docker | Containerised deployment |
| | H2 (file mode) | Lightweight, self‑contained persistence |
| | CoinGecko API | Price data source |
| | Alternative.me API | Fear & Greed Index |
| | Telegram Bot API | Alert delivery |

## 2. Core Packages

```
src/main/java/com/example/demo
├─ controller       // MVC controllers that expose web pages and form handling
├─ service          // Business logic: data fetching, monitoring, alerting
├─ model            // JPA entities representing persisted data
├─ repository       // Spring Data JPA repositories for database access
├─ config           // Application configuration (security, WebClient builder)
└─ CryptoCentinelaApplication.java // Main entry point
```

### 2.1 Controller Layer
* **DashboardController** – Renders the home page and handles add/delete actions for monitored assets.

### 2.2 Service Layer
* **CoinGeckoService** – Wraps the CoinGecko REST API, providing methods to fetch current prices and the Fear & Greed index.
* **MonitoringService** – Scheduled job that runs every 5 minutes, compares current prices with previous values, logs alerts, and triggers Telegram notifications.
* **TelegramService** – Sends formatted messages to a configured chat via the Telegram Bot API.

### 2.3 Persistence Layer
* **CryptoAsset** – Stores the asset symbol, name, current price, and timestamp of the last update.
* **AlertLog** – Stores each alert event (asset, price, timestamp). Queries are used to show recent alerts on the dashboard.

## 3. Data Flow
```
┌───────────────────────┐   HTTP   ┌───────────────────┐
│   Client Browser      │ ───────▶ │ DashboardController│
└───────────────────────┘           └───────────────────┘
                         │
                         ▼
┌───────────────────────┐   Scheduler   ┌─────────────────────┐
│ MonitoringService     │ ───────────▶ │ CoinGeckoService    │
└───────────────────────┘             └─────────────────────┘
                         │
                         ▼
┌───────────────────────┐   Telegram   ┌───────────────────┐
│ TelegramService       │ ───────────▶ │ Telegram Bot API  │
└───────────────────────┘             └───────────────────┘
```

All interactions are logged to the H2 database. The scheduled task persists new price information and any alerts. The Telegram service is only invoked when an alert condition is met.

## 4. Security
* Public endpoints (`GET /`) are accessible to anyone.
* Admin endpoints (`/add`, `/delete/{id}`) are protected by HTTP Basic authentication.
* Credentials are injected via environment variables `SPRING_SECURITY_USER_NAME` and `SPRING_SECURITY_USER_PASSWORD`.

## 5. Deployment
The application is containerised using the provided `Dockerfile`. It expects the same environment variables as the local setup.

```bash
docker build -t crypto-sentinel .
docker run -d --name sentinel \
  -p 8080:8080 \
  -e TELEGRAM_BOT_TOKEN=... \
  -e TELEGRAM_CHAT_ID=... \
  -e SPRING_SECURITY_USER_NAME=... \
  -e SPRING_SECURITY_USER_PASSWORD=... \
  crypto-sentinel
```

---
*Author: delysz* (2026)
