---
id: 'prd/005'
title: Process a committed event onto the right aggregate, idempotently (warm core)
status: backlog
created: 2026-06-02T20:14:19Z
updated: 2026-06-02T20:14:19Z
epic: prd
path: warm
depends_on: ['prd/002', 'prd/001']
parallel: false
metrics: ['goal-1', 'goal-2', 'metric-99.9-5min']
hitl: ['0002', '0003', '0005']
---

# 005 — Process a committed event onto the right aggregate, idempotently (warm core)

## User story

**As a** paying customer
**I want** my payment/subscription/refund event applied to my account
**So that** my account reflects reality within a few minutes, every time.

## Acceptance criteria

1. `derived` (line 45) — a `@Scheduled` poller claims a batch with `SELECT … FOR UPDATE SKIP LOCKED` (no two workers claim the same event).
2. `derived` (line 45; ADR 0005) — the internal `user_id` is resolved-or-created (lazy JIT) within the transaction.
3. `derived` (line 45) — the correct aggregate (payment/subscription/refund) is updated within one transaction and the event marked processed.
4. `derived` (Goal 2, line 28; ADR 0003) — a re-claimed already-processed event is a no-op (processed-events ledger → exactly-once *effect*).

## Non-goals for this story

No ordering guard (S006). No retry/DLQ (S007).

## Open questions (resurface as loop-2 checkpoints at /story:start — ADR 0017)

- Exact JIT user-creation guard, and is auto-provisioning acceptable to product? (ADR 0005 mitigation) — owner: product-owner + backend-developer.

## Dependencies

S002 (committed events to read), S001 (ID/timestamp confirmation). Postgres (ADR 0002).
