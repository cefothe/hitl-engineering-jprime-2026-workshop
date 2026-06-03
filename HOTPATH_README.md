# PayHub Hot Path — Durable Ingest + ACK (story prd/002)

The synchronous webhook receiver: it accepts a PayHub `POST`, durably stores the
raw event in PostgreSQL, and acknowledges **only after the write commits** so no
event is lost across a restart (Goal 1 / ADR 0011).

This module is **pure Java 21 with no framework** — no Spring, no DI container,
no AOP, no JPA. Everything is hand-wired in `main()`. That ban is the whole point
of the hot path (CLAUDE.md "Architecture constraints", ADR 0019) and is enforced
by an ArchUnit rule that fails the build on any `org.springframework..` /
`jakarta.persistence..` import under `com.example.payhub.ingress`.

> Scope is exactly prd/002. **No** signature/HMAC verification (that is prd/003),
> **no** dedup logic (prd/004 — though the `event_id` UNIQUE column it needs is
> created here), **no** warm/cold path.

---

## Architecture (locked decisions)

| Concern | Choice | ADR |
|---|---|---|
| Data store | PostgreSQL, **plain JDBC only** | 0002 |
| "Not lost" | durable write **commits before** any ACK | 0011 |
| HTTP server | Helidon Níma (virtual-thread native) | 0020 |
| Connection pool | HikariCP standalone, hand-wired in `main` | 0021 |
| Build | single Maven module + ArchUnit framework-ban gate | 0019 |

### Request flow

1. `POST /webhooks/payhub` arrives at Helidon Níma.
2. Body is read as **raw bytes, verbatim** (preserved for prd/003 HMAC).
3. Event id is extracted (see assumption below).
4. `setAutoCommit(false)` → `INSERT` → `commit()` via plain JDBC (`WebhookEventRepository`).
5. ACK is returned **after** the commit succeeds.

### HTTP status contract

| Status | When |
|---|---|
| `200` | durable commit succeeded — the event is "not lost" |
| `400` | no event id could be extracted (cannot satisfy the UNIQUE column; not retryable as-is) |
| `405` | non-`POST` method on the webhook path |
| `413` | body exceeded the configured cap (rejected before any write) |
| `503` | durable write failed at/before commit — **retryable**, PayHub re-delivers |

---

## Build-time assumptions (documented, reversible)

- **Event-id extraction.** The prd/001 spike could not run (no PayHub sandbox in
  this repo), so PayHub's real envelope is unconfirmed. The default is:
  read a configurable header (`PayHub-Event-Id`), falling back to a minimal scan
  for a top-level JSON `"id"` field. This lives in one place — `EventIdExtractor`
  — and is the single thing to correct when the spike lands. (Carried open
  question on the story.)
- **Schema bootstrap.** `src/main/resources/schema/webhook_event.sql` is the
  single source of truth (the cross-framework contract, ADR 0002 / tension T1).
  For now the app applies it idempotently at startup (`CREATE TABLE IF NOT
  EXISTS`). **Migration ownership moves to the warm-path module (Flyway) later.**
- **PII.** Raw payloads are PII. They are **never logged** — not at INFO, not in
  error paths. Only the event id and received-at timestamp are logged. No bound
  JDBC parameter is ever placed into a log line or exception message.

---

## Configuration (environment variables only)

No secrets in the repo — everything comes from `System.getenv()`.

| Variable | Required | Default | Purpose |
|---|---|---|---|
| `PAYHUB_DB_URL` | **yes** | — | JDBC URL, e.g. `jdbc:postgresql://localhost:5432/payhub` |
| `PAYHUB_DB_USER` | **yes** | — | DB user |
| `PAYHUB_DB_PASSWORD` | **yes** | — | DB password (secret) |
| `PAYHUB_HTTP_PORT` | no | `8080` | Listen port (`0` = ephemeral) |
| `PAYHUB_EVENT_ID_HEADER` | no | `PayHub-Event-Id` | Header carrying the event id |
| `PAYHUB_MAX_BODY_BYTES` | no | `1048576` (1 MiB) | Body cap; larger → `413` |
| `PAYHUB_DB_POOL_MAX_SIZE` | no | `10` | HikariCP max pool size |

The process **fails fast at startup** if any required variable is missing.

---

## Running the hot-path receiver

The receiver reads **all** configuration — including the database secret — from
environment variables only (CLAUDE.md secrets rule); see the
[Configuration](#configuration-environment-variables-only) table above for the full
list. The three required variables are:

| Variable | Read by | Purpose |
|---|---|---|
| `PAYHUB_DB_URL` | `IngressConfig.fromEnvironment()` | JDBC URL, e.g. `jdbc:postgresql://localhost:5432/payhub` |
| `PAYHUB_DB_USER` | `IngressConfig.fromEnvironment()` | DB user |
| `PAYHUB_DB_PASSWORD` | `IngressConfig.fromEnvironment()` | DB password (**secret** — set in the shell/orchestrator, never commit a value) |

Optional variables (`PAYHUB_HTTP_PORT`, `PAYHUB_EVENT_ID_HEADER`,
`PAYHUB_MAX_BODY_BYTES`, `PAYHUB_DB_POOL_MAX_SIZE`) have safe defaults — see the
table above. No secret **value** ever lives in this repo; only the variable names
are documented here. The process **fails fast at startup** if a required variable
is missing.

Start a Postgres (any method); for example with Docker:

```bash
docker run --rm -d --name payhub-pg \
  -e POSTGRES_DB=payhub -e POSTGRES_USER=payhub -e POSTGRES_PASSWORD=payhub \
  -p 5432:5432 postgres:16-alpine
```

Build and run the hot path:

```bash
mvn -q package -DskipTests

PAYHUB_DB_URL='jdbc:postgresql://localhost:5432/payhub' \
PAYHUB_DB_USER='payhub' \
PAYHUB_DB_PASSWORD='payhub' \
java -jar target/payhub-webhook-receiver.jar
```

Send a test event:

```bash
curl -i -X POST http://localhost:8080/webhooks/payhub \
  -H 'PayHub-Event-Id: evt_demo_1' \
  -H 'Content-Type: application/json' \
  -d '{"type":"payment.succeeded","amount":4200}'
# => HTTP/1.1 200
```

---

## Tests

| Command | Runs | Needs Docker |
|---|---|---|
| `mvn test` | ArchUnit framework-ban + `EventIdExtractor` unit tests (Surefire) | no |
| `mvn verify` | the above **plus** the `*IT` integration tests (Failsafe) | **yes** |

Integration tests (`DurableIngestIT`, `WebhookEndpointIT`) use **Testcontainers
PostgreSQL** at **test scope only** — these libraries are not on the runtime/
ingress classpath. The headline test, `committedEventSurvivesHardRestart`,
commits a row through one HikariCP pool, **closes that pool entirely** (the
in-test analogue of killing the JVM — all in-memory state is discarded), opens a
fresh pool against the same container, and asserts the row is still there
(prd/002 AC4).
