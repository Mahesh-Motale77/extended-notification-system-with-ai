# Notification System

Event-driven notification system built with Java 21, Spring Boot 3.x, Apache Kafka, Redis, and MySQL.
Supports Email notifications with retry mechanism, Dead Letter Queue (DLQ), user preference management, and order status management.

---

## Architecture

```
┌──────────────────────────────────┐
│         Order Service            │
│         (Port: 8082)             │
│                                  │
│  POST /order/v1/create           │
│         ↓                        │
│  Saves to orders table           │
│         ↓                        │
│  Publishes to Kafka              │
│  Topic: order-event-topic        │
└──────────────┬───────────────────┘
               │
               ▼ Kafka
┌──────────────────────────────────────────────────────────┐
│                  Notification Service                    │
│                  (Port: 8083)                            │
│                                                          │
│  OrderEventNotificationConsumer                          │
│         ↓                                                │
│  IdempotencyService (Redis)                              │
│  Key: idempotency:{orderId}:{orderStatus} TTL: 24hrs     │
│         ↓                                                │
│  NotificationServiceImpl                                 │
│         ↓                                                │
│  Fetch NotificationPreferences from DB                   │
│         ↓                                                │
│  TemplateServiceImpl → build subject + body              │
│         ↓                                                │
│  EmailDispatcher → Gmail SMTP                            │
│         ↓                                                │
│  Save to notification_details table                      │
│         ↓                                                │
│  On Failure → RetryServiceImpl                           │
│  Retry 1 (2s) → Retry 2 (4s) → Retry 3 (8s)            │
│         ↓ All retries exhausted                          │
│  Push to notification-dlq topic                          │
│         ↓                                                │
│  DLQConsumer → saves DLQ status to DB                    │
└──────────────────────────────────────────────────────────┘
               │
               ▼
┌──────────────────────────────────────────────────────────┐
│           Notification Management API                    │
│           (Port: 8084)                                   │
│                                                          │
│  GET  /dlq/api/v1/records          → View DLQ records    │
│  POST /dlq/api/v1/retry/{orderId}  → Retry failed notif  │
│  POST /preferences/api/v1/add      → Add preference      │
│  GET  /preferences/api/v1/get/{userId} → Get preferences │
│  POST /template/api/v1/register    → Register template   │
│  POST /template/api/v1/get         → Get template        │
│  POST /order/api/v1/status         → Change order status │
└──────────────────────────────────────────────────────────┘
               │
               ▼
┌──────────────────────────────────┐
│             MySQL                │
│  notification_db                 │
│  ├── orders                      │
│  ├── notification_details        │
│  ├── notification_preferences    │
│  └── notification_template       │
└──────────────────────────────────┘
```

---

## Tech Stack

| Technology | Version | Purpose |
|---|---|---|
| Java | 21 | Programming language |
| Spring Boot | 3.5.x | Backend framework |
| Apache Kafka | 3.x | Async event streaming |
| Redis | 7.x | Idempotency check |
| MySQL | 8.0 | Persistent storage |
| JavaMail | - | Email via Gmail SMTP |
| Lombok | - | Boilerplate reduction |
| Maven Multi-Module | - | Project structure |

---

## Project Structure

```
notification-system/                          ← Parent Maven project
├── pom.xml                                   ← Parent pom (Java 21, Spring Boot 3.5.x)
│
├── order-service/                            ← Port 8082
│   └── src/main/java/com/mahesh/
│       ├── OrderServiceApplication.java
│       └── orderservice/
│           ├── controller/
│           │   └── OrderController.java       ← POST /order/v1/create
│           ├── service/impl/
│           │   └── OrderServiceImpl.java
│           ├── kafka/
│           │   ├── OrderEventPublisherService.java  ← Publishes to order-event-topic
│           │   ├── KafkaTopicConfig.java             ← Creates order-event-topic (3 partitions)
│           │   └── kafkaConfig.java
│           ├── model/
│           │   └── Order.java                 ← Table: orders
│           ├── filter/
│           │   └── MdcFilter.java             ← UUID in every log line
│           ├── dto/request/OrderRequest.java
│           ├── dto/response/OrderResponse.java
│           └── vo/OrderVo.java
│
├── notification-service/                     ← Port 8083
│   └── src/main/java/com/mahesh/
│       ├── NotificationServiceApplication.java
│       └── notificationservice/
│           ├── kafka/
│           │   ├── OrderEventNotificationConsumer.java ← Consumes order-event-topic
│           │   ├── DLQConsumer.java                    ← Consumes notification-dlq
│           │   └── KafkaTopicConfig.java               ← Creates notification-dlq topic
│           ├── service/impl/
│           │   ├── NotificationServiceImpl.java        ← Core orchestrator
│           │   ├── RetryServiceImpl.java               ← Retry + DLQ logic
│           │   └── TemplateServiceImpl.java            ← Builds email content
│           ├── channel/
│           │   └── EmailDispacher.java                 ← Gmail SMTP dispatcher
│           ├── redis/
│           │   └── IdempotencyService.java             ← Redis duplicate check
│           ├── model/
│           │   ├── NotificationDetails.java    ← Table: notification_details
│           │   ├── NotificationPreferences.java ← Table: notification_preferences
│           │   └── NotificationTemplate.java   ← Table: notification_template
│           ├── config/
│           │   ├── MailConfig.java
│           │   └── redisConfig.java
│           └── dto/
│               ├── EventRequest.java
│               └── DLQMessage.java
│
└── notification-management-api/             ← Port 8084
    └── src/main/java/com/mahesh/
        ├── ManagementApiApplication.java
        └── managementapi/
            ├── contoller/
            │   ├── DLQContoller.java                  ← DLQ endpoints
            │   ├── NotificationPreferencesController.java
            │   ├── NotificationTemplateController.java
            │   └── OrderStatusController.java
            ├── service/impl/
            │   ├── DLQRetryServiceImpl.java           ← Clears Redis + re-queues to Kafka
            │   ├── DLQServiceImpl.java                ← Fetches DLQ records
            │   ├── OrderStatusServiceImpl.java        ← Changes order status + publishes event
            │   ├── PreferencesServiceImpl.java
            │   └── TemplateServiceImpl.java
            ├── model/
            │   ├── NotificationDetails.java
            │   ├── NotificationPreferences.java
            │   └── NotificationTemplate.java
            ├── filter/
            │   └── MdcFilter.java
            └── vo/
                ├── EventRequestVo.java
                └── OrderVo.java
```

---

## Key Features

### 1. Event-Driven Architecture
Order Service publishes `OrderVo` to Kafka topic `order-event-topic` using `userId` as the partition key — ensuring all events for the same user go to the same partition and are processed in order.

### 2. MDC Filter (Request Tracing)
Every request generates a UUID stored in MDC. All log lines across the request lifecycle carry this UUID, making it easy to trace a complete request flow.

### 3. Redis Idempotency Check
Before processing any event, `IdempotencyService` checks Redis:
```
Key pattern : idempotency:{orderId}:{orderStatus}
Value       : "Processed"
TTL         : 24 hours
```
Duplicate Kafka messages are detected and skipped automatically.

### 4. User Preference Based Routing
`NotificationServiceImpl` fetches user preferences from `notification_preferences` table. Supports `EMAIL`, `SMS`, and `BOTH` channels. Defaults to EMAIL if no preference found.

### 5. Template Engine
Notification content stored in `notification_template` table with placeholders:
```
"Hi User {{userId}}, your order {{orderId}} has been placed. Amount: ₹{{amount}}"
→ "Hi User 101, your order ORD-001 has been placed. Amount: ₹1299.0"
```
Supported placeholders: `{{orderId}}`, `{{userId}}`, `{{amount}}`, `{{items}}`, `{{status}}`

### 6. Retry with Exponential Backoff
Failed notifications are retried 3 times with increasing delays:
```
Attempt 1 → wait 2 seconds  → retry
Attempt 2 → wait 4 seconds  → retry
Attempt 3 → wait 8 seconds  → retry
All failed → push to notification-dlq
```

### 7. Dead Letter Queue (DLQ)
Messages that fail all retries are published to `notification-dlq` topic. `DLQConsumer` saves the failure with status `DLQ` to `notification_details` table. Admin retries via `POST /dlq/api/v1/retry/{orderId}`.

### 8. DLQ Retry — Edge Case Handled
When retrying a DLQ record, `DLQRetryServiceImpl`:
1. Reads original event from `payload` column
2. **Deletes Redis idempotency key** — prevents skip due to duplicate detection
3. Re-publishes original event to `order-event-topic`
4. `NotificationServiceImpl` reuses existing DB entry instead of creating duplicate

### 9. Order Status Change
`OrderStatusController` allows changing order status and re-publishing event to Kafka — simulating real-world status updates (ORDER_CONFIRMED, ORDER_SHIPPED etc.) without needing separate microservices.

---

## Database Schema

```sql
-- Stores every order
CREATE TABLE orders (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id     VARCHAR(255),
    user_id      VARCHAR(255),
    items        VARCHAR(255),
    amount       DOUBLE,
    order_status VARCHAR(255),
    created_at   DATETIME,
    updated_at   DATETIME
);

-- Stores every notification attempt
CREATE TABLE notification_details (
    id                    BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id              VARCHAR(255),
    user_id               VARCHAR(255),
    notification_type     VARCHAR(255),
    notification_status   VARCHAR(255),
    channel               VARCHAR(255),
    error_message         TEXT,
    retry_count           INT,
    payload               TEXT,
    created_at            DATETIME,
    updated_at            DATETIME
);

-- User channel preferences per notification type
CREATE TABLE notification_preferences (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id           VARCHAR(255),
    notification_type VARCHAR(255),
    channel           VARCHAR(255),
    is_enable         BOOLEAN
);

-- Email templates with placeholders
CREATE TABLE notification_template (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    notification_type VARCHAR(255),
    channel           VARCHAR(255),
    subject           VARCHAR(255),
    body_template     TEXT
);
```

---

## Notification Types

| Type | Trigger |
|---|---|
| `ORDER_CREATED` | New order placed |
| `ORDER_CONFIRMED` | Order confirmed |
| `ORDER_SHIPPED` | Order shipped |
| `ORDER_DELIVERED` | Order delivered |
| `PAYMENT_SUCCESS` | Payment received |
| `PAYMENT_FAILED` | Payment failed |

---

## Kafka Topics

| Topic | Partitions | Producer | Consumer |
|---|---|---|---|
| `order-event-topic` | 3 | `OrderEventPublisherService` | `OrderEventNotificationConsumer` |
| `notification-dlq` | 3 | `RetryServiceImpl` | `DLQConsumer` |

---

## API Reference

### Order Service — Port 8082

| Method | Endpoint | Description |
|---|---|---|
| POST | `/order/v1/create` | Create order and publish Kafka event |

**Request:**
```json
{
    "userId": "USR-101",
    "items": "iPhone Case, Cable",
    "amount": 1299.00
}
```

**Response:**
```json
{
    "statusCode": "200",
    "statusMessage": "Success",
    "requestUUID": "uuid-here",
    "data": {
        "orderId": "ORD-001",
        "userId": "USR-101",
        "items": "iPhone Case, Cable",
        "amount": 1299.00,
        "orderStatus": "ORDER_CREATED",
        "createdAt": "2026-05-28T10:00:00",
        "updatedAt": "2026-05-28T10:00:00"
    }
}
```

---

### Notification Management API — Port 8084

#### DLQ Endpoints

| Method | Endpoint | Description |
|---|---|---|
| GET | `/dlq/api/v1/records` | Get all DLQ failed notifications |
| POST | `/dlq/api/v1/retry/{orderId}` | Retry failed notification by orderId |

**Retry Response:**
```json
{
    "statusCode": "200",
    "message": "Retry triggered successfully for orderId : ORD-001",
    "requestUUID": "uuid-here",
    "errorMessage": null
}
```

---

#### Preference Endpoints

| Method | Endpoint | Description |
|---|---|---|
| POST | `/preferences/api/v1/add` | Add user notification preference |
| GET | `/preferences/api/v1/get/{userId}` | Get preferences for a user |

**Add Preference Request:**
```json
{
    "userId": "USR-101",
    "notificationType": "ORDER_CREATED",
    "channel": "EMAIL",
    "isEnable": true
}
```

---

#### Template Endpoints

| Method | Endpoint | Description |
|---|---|---|
| POST | `/template/api/v1/register` | Register a notification template |
| POST | `/template/api/v1/get` | Get a template by type and channel |

**Register Template Request:**
```json
{
    "notificationType": "ORDER_CREATED",
    "channel": "EMAIL",
    "subject": "🎉 Order Confirmed - Order #{{orderId}}",
    "bodyTemplate": "Hi User {{userId}},\n\nYour order {{orderId}} has been placed.\nItems: {{items}}\nAmount: ₹{{amount}}\n\nTeam Notification System"
}
```

---

#### Order Status Endpoint

| Method | Endpoint | Description |
|---|---|---|
| POST | `/order/api/v1/status` | Change order status and re-publish event |

**Request:**
```json
{
    "orderId": "ORD-001",
    "newStatus": "ORDER_SHIPPED"
}
```

**Response:**
```json
{
    "statusCode": "200",
    "message": "Status changed for orderId :ORD-001",
    "requestUUID": "uuid-here",
    "orderDetails": {
        "orderId": "ORD-001",
        "oldStatus": "ORDER_CREATED",
        "newStatus": "ORDER_SHIPPED",
        "createdAt": "2026-05-28T10:00:00"
    }
}
```

---

## Complete Notification Flow

```
1.  Client hits POST /order/v1/create
2.  OrderServiceImpl saves order to orders table
3.  OrderEventPublisherService publishes OrderVo to order-event-topic
    (userId used as Kafka partition key)
4.  OrderEventNotificationConsumer picks up event
5.  IdempotencyService checks Redis:
    key = idempotency:{orderId}:{orderStatus}
    → EXISTS: skip (duplicate) 
    → NOT EXISTS: continue
6.  NotificationServiceImpl.processForNotification() called
7.  Fetch NotificationPreferences for userId from DB
    → No preference found: default to EMAIL
    → Preference found: use configured channel
8.  TemplateServiceImpl fetches template from notification_template
9.  Placeholders replaced: {{orderId}}, {{userId}}, {{amount}}, {{items}}
10. EmailDispacher sends email via Gmail SMTP
11. NotificationDetails saved to notification_details table (status=SENT)
12. IdempotencyService marks event as processed in Redis (TTL: 24 hours)

--- ON FAILURE ---
11. NotificationDetails saved (status=FAILED)
12. RetryServiceImpl.handleFailure() called
13. Retry 1: wait 2s → sendMail() → success: status=SENT, return
14. Retry 2: wait 4s → sendMail() → success: status=SENT, return
15. Retry 3: wait 8s → sendMail() → success: status=SENT, return
16. All retries failed → sendToDLQ()
17. DLQMessage published to notification-dlq topic
18. DLQConsumer receives it → saves status=DLQ to notification_details

--- DLQ RETRY ---
19. Admin hits POST /dlq/api/v1/retry/{orderId}
20. DLQRetryServiceImpl reads payload from notification_details
21. Deletes Redis key: idempotency:{orderId}:{orderStatus}
22. Re-publishes original event to order-event-topic
23. Flow repeats from step 4
```

---

## Edge Cases Handled

| Edge Case | Solution |
|---|---|
| Duplicate Kafka message delivery | Redis idempotency check — skips if already processed |
| Email server temporarily down | Exponential backoff retry (2s → 4s → 8s) |
| All retries exhausted | Dead Letter Queue — message preserved for manual retry |
| DLQ retry blocked by Redis | Redis key deleted before re-publishing in DLQRetryServiceImpl |
| DLQ retry creates duplicate DB row | `findByOrderId` reuses existing entry — resets status to PENDING |
| Request tracing across logs | MDC UUID filter on every request in all 3 services |

---

## Local Setup

### Prerequisites

- Java 21
- Maven 3.8+
- MySQL 8.0 (local install, port 3306)
- Apache Kafka 3.x (local install)
- Redis (local install, port 6379)

### Step 1 — Create Database

```sql
CREATE DATABASE notification_db;
```

### Step 2 — Start Zookeeper

Open Terminal 1:
```bash
cd C:\kafka
bin\windows\zookeeper-server-start.bat config\zookeeper.properties
```

Wait for:
```
binding to port 0.0.0.0/0.0.0.0:2181
```

### Step 3 — Start Kafka

Open Terminal 2:
```bash
cd C:\kafka
bin\windows\kafka-server-start.bat config\server.properties
```

Wait for:
```
[KafkaServer id=0] started
```

### Step 4 — Start Redis

```bash
redis-server
```

Verify:
```bash
redis-cli ping
# → PONG
```

### Step 5 — Configure Credentials

Update `application-dev.yml` in each service:

```yaml
spring:
  datasource:
    username: your_mysql_username
    password: your_mysql_password
  mail:
    username: your_gmail@gmail.com
    password: your_16char_app_password
```

> **Gmail App Password:** Go to myaccount.google.com → Security → 2-Step Verification → App Passwords → Generate

### Step 6 — Run Services

```bash
# Terminal 1 - Order Service (port 8082)
cd order-service
mvn spring-boot:run

# Terminal 2 - Notification Service (port 8083)
cd notification-service
mvn spring-boot:run

# Terminal 3 - Management API (port 8084)
cd notification-management-api
mvn spring-boot:run
```

---

## Quick Test Guide

### 1. Register Template
```bash
POST http://localhost:8084/template/api/v1/register
{
    "notificationType": "ORDER_CREATED",
    "channel": "EMAIL",
    "subject": "🎉 Order Confirmed - Order #{{orderId}}",
    "bodyTemplate": "Hi User {{userId}}, your order {{orderId}} has been placed. Items: {{items}}. Amount: ₹{{amount}}"
}
```

### 2. Add User Preference
```bash
POST http://localhost:8084/preferences/api/v1/add
{
    "userId": "USR-101",
    "notificationType": "ORDER_CREATED",
    "channel": "EMAIL",
    "isEnable": true
}
```

### 3. Place Order → Triggers Email
```bash
POST http://localhost:8082/order/v1/create
{
    "userId": "USR-101",
    "items": "iPhone Case, Cable",
    "amount": 1299.00
}
```

### 4. Change Order Status → Triggers Another Email
```bash
POST http://localhost:8084/order/api/v1/status
{
    "orderId": "ORD-001",
    "newStatus": "ORDER_SHIPPED"
}
```

### 5. View DLQ Records
```bash
GET http://localhost:8084/dlq/api/v1/records
```

### 6. Retry Failed Notification
```bash
POST http://localhost:8084/dlq/api/v1/retry/ORD-001
```

---

## Author

**Mahesh Motale**
- GitHub: [Mahesh-Motale77](https://github.com/Mahesh-Motale77)
- LinkedIn: [mahesh-motale-7281a7225](https://www.linkedin.com/in/mahesh-motale-7281a7225/)
