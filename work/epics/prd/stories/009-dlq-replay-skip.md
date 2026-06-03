---
id: 'prd/009'
title: Inspect the DLQ and replay or skip any stored event by ID (cold path)
status: backlog
created: 2026-06-02T20:14:19Z
updated: 2026-06-02T20:14:19Z
epic: prd
path: cold
depends_on: ['prd/007', 'prd/005']
parallel: false
metrics: ['goal-5', 'tickets-zero']
hitl: ['0009', '0010', '0015']
---

# 009 — Inspect the DLQ and replay or skip any stored event by ID (cold path)

## User story

**As an** on-call engineer
**I want** to inspect DLQ entries and replay or skip individual events by ID
**So that** I can resolve failures without writing a one-off script.

## Acceptance criteria

1. `derived` (line 57; ADR 0009) — listing the DLQ shows each entry's event ID, failure reason, and attempt count.
2. `derived` (line 57; ADR 0010) — replay of a stored event ID (incl. DLQ) re-submits it to the warm path (idempotency from S005 makes replay safe).
3. `derived` (line 57; ADR 0010) — skip marks a DLQ event skipped and not reprocessed.
4. `derived` (non-goal line 37 vs ADR 0010) — scope is **any stored event**; pre-launch historical import remains out of scope.

## Non-goals for this story

No pre-launch/historical event import.

## Open questions (resurface as loop-2 checkpoints at /story:start — ADR 0015)

- Access control (authz) for destructive replay/skip — decided at /story:start. — owner: product-owner + security.

## Dependencies

S007 (DLQ must exist), S005 (replay target path).
