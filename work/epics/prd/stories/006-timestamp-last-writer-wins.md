---
id: 'prd/006'
title: Apply timestamp last-writer-wins so out-of-order events don't corrupt state (warm)
status: backlog
created: 2026-06-02T20:14:19Z
updated: 2026-06-02T20:14:19Z
epic: prd
path: warm
depends_on: ['prd/005', 'prd/001']
parallel: false
metrics: ['goal-1', 'metric-reconciliation']
hitl: ['0006']
---

# 006 — Apply timestamp last-writer-wins so out-of-order events don't corrupt state (warm)

## User story

**As a** finance analyst
**I want** a late-arriving older event to not overwrite a newer state
**So that** a `refund.created` arriving after a later `payment.succeeded` doesn't leave the account wrong.

## Acceptance criteria

1. `derived` (line 70; ADR 0006) — an aggregate records the timestamp of its last applied event; an event with an **older** timestamp is not applied but is still marked received/processed.
2. `derived` (ADR 0006) — an event with a **newer** timestamp supersedes current aggregate state and updates the last-applied timestamp.

## Non-goals for this story

No per-aggregate version vectors unless escalated (see open questions).

## Open questions (resurface as loop-2 checkpoints at /story:start — ADR 0017)

- Tie-break rule when two events carry equal timestamps. — owner: backend-developer.
- Escalation path to per-aggregate version guards (ADR 0006 Option A) if S001 finds PayHub timestamps unreliable. — owner: solution-architect.

## Dependencies

S005 (extends the apply path), S001 (timestamp trustworthiness).
