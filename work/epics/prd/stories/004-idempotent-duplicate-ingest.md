---
id: 'prd/004'
title: Make duplicate POSTs an idempotent no-op-but-ACK (hot path)
status: backlog
created: 2026-06-02T20:14:19Z
updated: 2026-06-02T20:14:19Z
epic: prd
path: hot
depends_on: ['prd/002', 'prd/001']
parallel: false
metrics: ['goal-2']
hitl: ['0004']
---

# 004 — Make duplicate POSTs an idempotent no-op-but-ACK (hot path)

## User story

**As a** PayHub integration
**I want** to resend the same event safely
**So that** my retries never cause the event to be stored or applied twice.

## Acceptance criteria

1. `derived` (Goal 2, line 28; ADR 0004) — a unique constraint on PayHub event ID makes a duplicate write a no-op (no second row).
2. `derived` (Goal 2; line 44) — a duplicate still returns a 2xx ACK (PayHub must not keep retrying).

## Non-goals for this story

No warm-path apply-idempotency (that is S005's processed-events ledger).

## Open questions (resurface as loop-2 checkpoints at /story:start — ADR 0017)

- Concurrent-duplicate race: two identical events arriving simultaneously must yield exactly one row and two 2xx — in scope for v1? — owner: backend-developer + solution-architect.

## Dependencies

S002 (write path), S001 (AC1 valid only if S001 confirms event-ID stability; else escalate per ADR 0004 "revisit if").
