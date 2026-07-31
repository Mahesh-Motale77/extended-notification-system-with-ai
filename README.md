# AI-Powered Autonomous Notification Recovery Platform

> An AI-powered extension to an event-driven notification platform — adding autonomous failure analysis, intelligent recovery decision-making, and automated notification recovery using AI agents, built with **Spring AI (Google Gemini)** on top of **Java 21**, **Spring Boot 3.5**, **Apache Kafka**, **Redis**, and **MySQL**.

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-brightgreen)
![Kafka](https://img.shields.io/badge/Apache%20Kafka-Event%20Driven-black)
![Spring AI](https://img.shields.io/badge/Spring%20AI-Gemini-blue)
![Redis](https://img.shields.io/badge/Redis-Idempotency-red)
![MySQL](https://img.shields.io/badge/MySQL-Persistence-blue)
![License](https://img.shields.io/badge/License-MIT-lightgrey)


## Project Overview

This repository extends an existing, already-documented **Notification Platform** (Order Service → Kafka → Notification Service → email delivery, with retries and a DLQ) with a new layer of **autonomy**: when a notification exhausts its retries and lands in the dead-letter queue, the system no longer just sits there waiting for a human. It hands the failure to Gemini for root-cause analysis, asks a second AI agent to decide on a recovery strategy, and executes that strategy through a pluggable tool framework — with no human in the loop for the common cases.

This README focuses on that extension: the AI agents, the recovery pipeline, and the tool-based execution engine built around them.

---

## Base Notification System (Summary)

The underlying platform this project builds on is a standard event-driven notification pipeline, already covered in its own documentation:

- **Order Service** (`:8082`) — creates orders, persists them, publishes events to `order-event-topic`.
- **Notification Service** (`:8083`) — consumes order events, applies a Redis idempotency check, looks up preferences, renders a template, and dispatches email. Failed sends retry with backoff (2s / 4s / 8s) before being pushed to `notification-dlq`.
- **Notification Management API** (`:8084`) — manages templates and preferences, and exposes DLQ visibility and manual retry.

That base flow is treated here as a given. Everything below is what happens **after** a notification reaches the DLQ.

---

## What This Extension Adds

- AI-driven **failure analysis** (root cause, severity, confidence) for every DLQ event
- AI-driven **recovery decisions** (retry now, retry later, escalate, manual review, discard)
- A **Recovery Scheduler** that executes due decisions every minute
- A **Recovery Agent** that contains no business logic and simply delegates to tools
- A **Tool-Based Recovery Engine** — independently implemented, Spring-discoverable tools, one per recovery action
- Two new database tables (`failure_analysis`, `recovery_decision`) capturing every AI decision for auditability

---

## AI Recovery Architecture

```
                     ┌─────────────────────────────────────────────────┐
                     │         notification-dlq  (Kafka topic)           │
                     └────────────────────────┬───────────────────────────┘
                                                │
                                                ▼
                     ┌─────────────────────────────────────────────────┐
                     │             AI Failure Analysis Agent              │
                     │                    (Gemini)                        │
                     │  Notification + Retry Count + Error + Channel      │
                     │      → Incident Summary / Root Cause / Severity /  │
                     │        Retry Recommended / Suggested Fix /         │
                     │        Confidence                                  │
                     │             → stored in failure_analysis           │
                     └────────────────────────┬───────────────────────────┘
                                                ▼
                     ┌─────────────────────────────────────────────────┐
                     │            AI Recovery Decision Agent              │
                     │                    (Gemini)                        │
                     │  Notification + Failure Analysis                   │
                     │      → RecoveryAction / RetryAfterMinutes /        │
                     │        Reason / Confidence                         │
                     │             → stored in recovery_decision          │
                     └────────────────────────┬───────────────────────────┘
                                                ▼
                     ┌─────────────────────────────────────────────────┐
                     │     Recovery Scheduler  (runs every minute)        │
                     │       loads pending, due recovery decisions        │
                     └────────────────────────┬───────────────────────────┘
                                                ▼
                     ┌─────────────────────────────────────────────────┐
                     │                  Recovery Agent                    │
                     │            (reads decision, picks a tool)          │
                     └────────────────────────┬───────────────────────────┘
                                                ▼
                     ┌─────────────────────────────────────────────────┐
                     │                   Tool Factory                     │
                     ├───────────┬───────────┬───────────┬───────────────┤
                     │Retry Tool │Escalation │Manual Rev.│ Discard Tool   │
                     │           │Tool       │Tool       │                │
                     └───────────┴───────────┴───────────┴───────────────┘
```

---

## Spring AI Integration

The AI layer is built entirely on **Spring AI**, using:

- Google GenAI Starter (`spring-ai-google-genai-spring-boot-starter`)
- **Gemini Flash** as the underlying model — chosen for low latency and cost, since analysis runs on every DLQ event
- Prompt templates for consistent, structured prompting
- Structured JSON output via `BeanOutputConverter`, so Gemini's response deserializes directly into typed Java records/DTOs
- `ChatClient` as the single entry point used by both AI agents

Because the output is coerced into a typed Java object at the boundary, none of the downstream components (scheduler, recovery agent, tools) need to know they're dealing with an LLM at all — from their point of view, it's just another service call that returns a POJO.

---

## AI Failure Analysis Agent

Triggered the moment a notification reaches the DLQ.

**Flow:**

1. A notification reaches the DLQ.
2. `FailureAnalysisService` is invoked.
3. Gemini receives:
   - Notification details
   - Retry count
   - Error message
   - Notification channel
   - Current status
4. Gemini returns a structured JSON object containing:
   - **Incident Summary**
   - **Root Cause**
   - **Severity**
   - **Retry Recommended** (boolean)
   - **Suggested Fix**
   - **Confidence**
5. The response is persisted in the `failure_analysis` table.

```java
public record FailureAnalysisResult(
    String incidentSummary,
    String rootCause,
    String severity,
    boolean retryRecommended,
    String suggestedFix,
    double confidence
) {}
```

---

## AI Recovery Decision Agent

Once a failure has been analyzed, a second, independent agent decides what to actually *do* about it.

**Flow:**

1. `RecoveryDecisionService` reads:
   - The notification record
   - The corresponding failure analysis
2. Gemini decides on:
   - **RecoveryAction** — one of `RETRY_NOW`, `RETRY_LATER`, `ESCALATE`, `MANUAL_REVIEW`, `DISCARD`
   - **RetryAfterMinutes** (when applicable)
   - **Reason**
   - **Confidence**
3. The decision is persisted in the `recovery_decision` table.

```java
public record RecoveryDecision(
    RecoveryAction action,
    Integer retryAfterMinutes,
    String reason,
    double confidence
) {}
```

Separating *analysis* from *decision* into two distinct agent calls keeps each prompt focused and each response easier to validate — the failure analysis agent never has to reason about scheduling or business policy, and the decision agent never has to re-derive the root cause.

---

## Recovery Scheduler

A `@Scheduled` job that runs **every minute**:

1. Loads all pending recovery decisions whose scheduled time has arrived.
2. Invokes the **Recovery Agent** for each one.

This decouples "when the AI decided" from "when the recovery actually executes," which matters most for `RETRY_LATER` decisions with a delay attached.

---

## Recovery Agent

The Recovery Agent is deliberately "dumb" — it contains **no business logic** of its own. Its only two jobs:

1. Read the AI's decision.
2. Delegate execution to the correct tool.

```
Recovery Agent
      │
      ▼
 Reads RecoveryDecision
      │
      ▼
 Asks ToolFactory for the matching RecoveryTool
      │
      ▼
 Delegates execution to that tool
```

All actual recovery behavior — retrying, escalating, flagging for manual review, discarding — lives inside independently testable tool classes, not inside the agent.

---

## Tool-Based Architecture

```
Recovery Agent
      │
      ▼
 Tool Factory
      │
      ├──► Retry Tool
      ├──► Escalation Tool
      ├──► Manual Review Tool
      └──► Discard Tool
```

Every tool implements a common contract:

```java
public interface RecoveryTool {
    RecoveryAction getAction();
    void execute(Notification notification, RecoveryDecision decision);
}
```

- `ToolFactory` auto-discovers every `RecoveryTool` bean via Spring dependency injection — no manual registration.
- The Recovery Agent picks a tool dynamically by matching `RecoveryDecision.action()` against each tool's `getAction()`.
- There is **no switch-case** inside the agent; adding a new recovery action means adding a new `RecoveryTool` implementation, nothing else.

```java
@Component
public class ToolFactory {

    private final Map<RecoveryAction, RecoveryTool> toolMap;

    public ToolFactory(List<RecoveryTool> tools) {
        this.toolMap = tools.stream()
            .collect(Collectors.toMap(RecoveryTool::getAction, Function.identity()));
    }

    public RecoveryTool getTool(RecoveryAction action) {
        return toolMap.get(action);
    }
}
```

---

## Design Patterns Used

| Pattern | Where it's applied |
|---|---|
| **Strategy Pattern** | Each `RecoveryTool` is an interchangeable strategy for handling a `RecoveryAction` |
| **Factory Pattern** | `ToolFactory` resolves the correct tool at runtime |
| **Open/Closed Principle** | New recovery actions are added without modifying the Recovery Agent |
| **Dependency Injection** | Tools are auto-wired and auto-discovered by Spring |
| **Tool Pattern** | AI decisions are translated into discrete, executable capabilities |
| **Event-Driven Architecture** | Kafka decouples the base notification pipeline from the AI recovery pipeline |

---

## Database Schema (AI Tables)

The base platform's tables (`orders`, `notification_details`, `notification_preferences`, `notification_template`) are unchanged and documented elsewhere. This extension adds two new tables:

### `failure_analysis`
Output of the AI Failure Analysis Agent, one row per DLQ event.

| Column | Type | Notes |
|---|---|---|
| id | BIGINT (PK) | |
| notification_id | BIGINT (FK) | |
| incident_summary | TEXT | |
| root_cause | TEXT | |
| severity | VARCHAR | |
| retry_recommended | BOOLEAN | |
| suggested_fix | TEXT | |
| confidence | DOUBLE | |
| created_at | TIMESTAMP | |

### `recovery_decision`
Output of the AI Recovery Decision Agent, and the input the scheduler consumes.

| Column | Type | Notes |
|---|---|---|
| id | BIGINT (PK) | |
| notification_id | BIGINT (FK) | |
| action | VARCHAR | `RETRY_NOW` / `RETRY_LATER` / `ESCALATE` / `MANUAL_REVIEW` / `DISCARD` |
| retry_after_minutes | INT | Nullable |
| reason | TEXT | |
| confidence | DOUBLE | |
| executed | BOOLEAN | Set once the Recovery Agent has processed it |
| created_at | TIMESTAMP | |

---

## Project Structure (AI Modules)

Only the new packages inside `notification-service` are shown below; the rest of the module layout (base controllers, entities, Kafka consumers) is unchanged.

```
notification-service/
└── src/main/java/com/mahesh/notificationservice/
    ├── ai/
    │   ├── config/
    │   │   └── AiConfig.java
    │   ├── controller/
    │   │   └── AiTestController.java
    │   ├── dto/
    │   │   ├── FailureAnalysisResponse.java
    │   │   └── RecoveryDecisionResponse.java
    │   ├── enums/
    │   │   ├── RecoveryAction.java
    │   │   └── Severity.java
    │   ├── model/
    │   │   ├── FailureAnalysisEntity.java
    │   │   └── RecoveryDecisionEntity.java
    │   ├── repository/
    │   │   ├── FailureAnalysisRepository.java
    │   │   └── RecoveryDecisionRepository.java
    │   ├── scheduler/
    │   │   └── RecoveryScheduler.java
    │   ├── service/
    │   │   ├── impl/
    │   │   │   ├── FailureAnalysisServiceImpl.java
    │   │   │   ├── RecoveryAgentImpl.java
    │   │   │   └── RecoveryDecisionServiceImpl.java
    │   │   ├── FailureAnalysisService.java
    │   │   ├── RecoveryAgent.java
    │   │   └── RecoveryDecisionService.java
    │   ├── tool/
    │   │   ├── DiscardTool.java
    │   │   ├── EscalationTool.java
    │   │   ├── ManualReviewTool.java
    │   │   ├── RecoveryTool.java
    │   │   ├── RetryTool.java
    │   │   └── ToolFactory.java
    │   └── util/
    ├── channel/
    ├── config/
    ├── dto/
    ├── kafka/
    ├── model/
    ├── redis/
    ├── repository/
    ├── service/
    └── NotificationServiceApplication.java

src/main/resources/
├── prompts/
│   ├── failure-analysis.st
│   └── recovery-decision.st
├── application.yml
├── application-dev.yml
└── logback-spring.xml
```


## Local Setup

### Prerequisites

- Java 21
- Maven 3.9+
- MySQL 8+
- Redis 7+
- Apache Kafka (with Zookeeper or KRaft)
- A Google Gemini API key

### Environment Variables

```bash
export DB_URL=jdbc:mysql://localhost:3306/notification_platform
export DB_USERNAME=root
export DB_PASSWORD=yourpassword
export REDIS_HOST=localhost
export REDIS_PORT=6379
export KAFKA_BOOTSTRAP_SERVERS=localhost:9092
export GEMINI_API_KEY=your_google_gemini_api_key
```

### Steps

```bash
# 1. Clone the repository
git clone https://github.com/your-username/ai-notification-recovery-platform.git
cd ai-notification-recovery-platform

# 2. Start infrastructure (MySQL, Redis, Kafka)
docker-compose up -d

# 3. Build all modules
mvn clean install

# 4. Run each service
cd order-service && mvn spring-boot:run
cd ../notification-service && mvn spring-boot:run
cd ../notification-management-api && mvn spring-boot:run
```

Services will be available at:

- Order Service → `http://localhost:8082`
- Notification Service → `http://localhost:8083`
- Notification Management API → `http://localhost:8084`

---

## Testing Guide

- **Unit tests** — each `RecoveryTool` implementation is tested in isolation with mocked notification/decision inputs.
- **Service tests** — `FailureAnalysisService` and `RecoveryDecisionService` are tested with a mocked `ChatClient`, so the AI prompt/response contract can be verified without calling Gemini.
- **Integration tests** — the DLQ → analysis → decision → scheduler → agent → tool chain is tested end-to-end with an embedded Kafka broker.
- **End-to-end** — a full run from a forced failure through DLQ, AI analysis, AI decision, and tool execution, asserting on the final notification status.

```bash
mvn test                     # unit tests
mvn verify -Pintegration     # integration tests
```

---

## Edge Cases Handled

- **AI unavailability** — failure analysis and recovery decision calls are wrapped so a Gemini outage doesn't block the DLQ consumer; affected notifications remain queryable for manual handling.
- **Low-confidence AI decisions** — decisions below a confidence threshold are routed toward `MANUAL_REVIEW` rather than being auto-executed.
- **Scheduler overlap** — the Recovery Scheduler only picks up decisions that are both pending and due, preventing double execution.
- **Autonomous retry loops** — a recovered notification that fails again is treated as a new failure event with its own analysis, rather than replaying the previous one.
