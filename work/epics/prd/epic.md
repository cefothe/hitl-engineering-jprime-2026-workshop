---
name: prd
status: backlog
created: 2026-06-02T20:06:22Z
updated: 2026-06-03T03:18:18Z
prd: PRD.md
progress: 8%
hitl: ['0002', '0003', '0004', '0005', '0006', '0007', '0008', '0009', '0010', '0011', '0012', '0013', '0014', '0017']
---

# Epic — Reliable Webhook Processing for Payment Events

Turns `PRD.md` into an architecture-bound implementation blueprint. The hot/warm/cold
boundary from `CLAUDE.md` is honoured **verbatim**. Every architecturally- or
product-significant decision below is backed by an ADR in `work/decisions/` (see `hitl:`).

## Overview

A synchronous-looking requirement (apply each event exactly once, ACK <5s, fully process
within 5 min, survive a burst, survive mid-processing restart) is only deliverable as an
**asynchronous, durably-buffered pipeline**:

- **Hot path** receives the `POST`, verifies authenticity, durably writes the raw event,
  and ACKs — nothing more. The durable write *is* the "not lost" guarantee (ADR 0011).
- **Warm path** reads committed events and applies them idempotently to payments /
  subscriptions / refunds, retries transient failures, and diverts poison events to a DLQ.
- **Cold path** serves event lookup, DLQ inspection / replay-skip, and reconciliation.

The durable store (PostgreSQL, ADR 0002) between hot and warm is the contract: ingress ends
at "committed + ACKed"; processing begins at "read a committed event."

## Architecture decisions (boundary honoured verbatim)

- **Hot path — Pure Java 21, NO framework.** JDK `HttpServer` (or Helidon Níma), hand-wired.
  Signature verification via JDK `javax.crypto` (HMAC-SHA256, ADR 0013), durable write via
  **plain JDBC** to Postgres, then ACK. **No Spring, no DI container, no AOP, no
  framework-driven annotations.** `spring-security-crypto` is banned here (ADR 0013 / T2).
  The store is accessed as plain SQL — no JPA entity or Spring repository crosses into
  ingress (architect tension T1).
- **Warm path — Spring Boot 3.x.** Spring Data over Postgres, `@Scheduled` poller claiming
  events with `SELECT … FOR UPDATE SKIP LOCKED`, transactional idempotent apply.
- **Cold path — Spring Boot 3.x.** Admin endpoints / CLI for lookup, DLQ, reconciliation.

### Decisions recorded at the decomposition gate

| Topic | Decision | ADR |
|---|---|---|
| Data store | PostgreSQL (all paths; hot path via plain JDBC) | 0002 |
| Delivery semantics | "True exactly-once" (room's call; see ADR caveat) | 0003 |
| Dedup key | Trust PayHub event ID (spike to confirm stability) | 0004 |
| Identity mapping | Lazy just-in-time `customer_id → user_id` | 0005 |
| Out-of-order | Timestamp last-writer-wins per aggregate | 0006 |
| Retention | Config value; **go-live blocked** until compliance answers | 0007 |
| Failure alerting | Real-time paging | 0008 |
| DLQ ownership | On-call engineering owns + drains | 0009 |
| Replay scope | Any-stored-event replay by ID | 0010 |
| "Not lost" semantics | Durably stored (incl. DLQ) = not lost | 0011 |
| Burst behaviour | Validate the 5k/5min estimate first (measurement spike) | 0012 |
| Signature | HMAC-SHA256, JDK crypto only | 0013 |

> **Flagged tension (not overridden):** ADR 0003 records the room's choice of "true
> exactly-once," which the architect noted is not literally achievable across the PayHub
> network boundary. The implementation will deliver exactly-once *effect* (idempotent apply
> over at-least-once receipt); ADR 0003 is expected to be revisited. Surfaced, per protocol.

## Technical approach per path

### Hot path (`src/ingress/`) — Pure Java 21
1. Read body + signature header.
2. Verify HMAC-SHA256 over the raw body (`javax.crypto`); reject 4xx on mismatch.
3. Durable write (raw payload + PayHub event ID + received-at) via plain JDBC; unique
   constraint on event ID makes a duplicate POST an idempotent no-op-but-ACK (ADR 0004).
4. ACK 2xx only after the write commits; on write failure, return non-2xx so PayHub retries.

### Warm path — Spring Boot 3.x
- `@Scheduled` claim of unprocessed events (`FOR UPDATE SKIP LOCKED`).
- Resolve-or-create `user_id` (lazy JIT, ADR 0005); apply to the right aggregate within one
  transaction; timestamp LWW guard (ADR 0006) skips superseded events; processed-events
  ledger guarantees idempotent re-application.
- Retry with backoff; after N failures → DLQ; real-time page on DLQ entry (ADR 0008).

### Cold path — Spring Boot 3.x
- Event lookup by PayHub event ID (Goal 3, <30s).
- DLQ inspection + any-stored-event replay/skip (ADR 0010), for the on-call engineer (ADR 0009).
- Reconciliation read views for finance.
- Parameterised retention purge job (ADR 0007) — stays open until compliance answers.

## Story breakdown preview (decomposed in /epic:decompose)

- **S1** Durable ingest (hot path): server + HMAC verify + JDBC durable write + ACK + dup-ID idempotency.
- **S2** Warm processing core: claim → JIT identity → LWW-guarded idempotent apply → mark done.
- **S3** Retry + DLQ + real-time paging.
- **S4** Event lookup (cold path) — Goal 3 / <30s.
- **S5** DLQ inspection + any-stored replay/skip (cold path).
- **S6** Burst resilience — **measurement spike first** (ADR 0012), then behaviour.
- **S7** PayHub spike: confirm event-ID stability + signature scheme + timestamp semantics (ADR 0004/0006/0013).
- **S8** Reconciliation read views (cold path).
- **S9** Retention purge job — **blocked on compliance** (ADR 0007).

## Dependencies & sequencing risks

- **Postgres (ADR 0002) is the keystone** — S1 and S2 both depend on the store + the
  hot/warm shared-schema contract.
- **S7 spike should run early** — S1 dedup (ADR 0004), ordering (ADR 0006), and signature
  (ADR 0013) all rest on unconfirmed PayHub behaviour.
- **Cross-framework guard (T1):** the store contract is plain schema, never shared code;
  the code-reviewer must check no framework leaks into `src/ingress/` on every PR.
- **Burst (S6) interacts with the store** — the measurement spike validates Postgres.
- **Open / deferred:** Q11 (€2,400 attribution → finance, deferrable) and Q13 (timeline →
  `/epic:estimate`) are not resolved here.

## Technical success criteria (traced to PRD Success metrics, lines 60–64)

- **Tickets 5/week → 0 (line 61):** S4 lookup + S3 DLQ visibility remove the "is it lost?"
  and "what happened?" support classes.
- **99.9% within 5 min (line 62):** measured received-at → processed-at; held by S2
  throughput + S6 burst behaviour. Per ADR 0011 this measures timeliness, distinct from
  "not lost."
- **Reconciliation < €50/month (line 63):** S8 views + idempotent apply. Caveat: Q11
  (deferrable) — if the discrepancy isn't fully webhook-attributable this may not be met by
  this work alone.
- **Zero "can you check if we got event…" tickets (line 64):** directly served by S4.

`/epic:done` must verify each metric above (CLAUDE.md "Claiming the work is done"
checkpoint) and confirm the compliance retention number landed (ADR 0007) before victory.

## Stories

Decomposed by `/epic:decompose` (13 slices). Coverage asserted: every Goal (1–5) and
Functional requirement (lines 42–47) is covered; no gap. Decompose-time decisions:
ADR 0014 (reconciliation scope), 0015 (access control → loop 2), 0016 (purged lookup),
0017 (impl-detail assumed ACs → loop 2).

| Story | Title | path | metrics | depends_on | parallel |
|---|---|---|---|---|---|
| prd/001 | PayHub spike (ID/signature/timestamp) | n/a | — (de-risk) | — | yes |
| prd/002 | Durable ingest + ACK | hot | goal-1, 99.9% | — | yes |
| prd/003 | Signature verification | hot | (security) | 002, 001 | no |
| prd/004 | Idempotent duplicate ingest | hot | goal-2 | 002, 001 | no |
| prd/005 | Warm processing core | warm | goal-1, goal-2, 99.9% | 002, 001 | no |
| prd/006 | Timestamp last-writer-wins | warm | goal-1, reconciliation | 005, 001 | no |
| prd/007 | Retry + DLQ + paging | warm | goal-5, tickets, 99.9% | 005 | no |
| prd/008 | Event lookup <30s | cold | goal-3, tickets | 002 | yes |
| prd/009 | DLQ replay/skip | cold | goal-5, tickets | 007, 005 | no |
| prd/010 | Burst measurement spike | warm | goal-4 | 002 | yes |
| prd/011 | Burst behaviour (data-pending) | warm | goal-4, 99.9% | 010, 002, 005 | no |
| prd/012 | Reconciliation read views | cold | reconciliation, tickets | 005 | yes |
| prd/013 | Retention purge (compliance-blocked) | cold | — (go-live gate) | 002, 008 | yes |

**Ready now (no incomplete deps, parallel-safe):** prd/001, prd/002, prd/008, prd/010
(prd/008 and prd/010 depend only on prd/002). **Critical path (preview):**
002 → 005 → 007 → 009. Sized properly by `/epic:estimate`.
