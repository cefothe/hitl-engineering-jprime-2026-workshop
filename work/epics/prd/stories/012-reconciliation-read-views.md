---
id: 'prd/012'
title: Reconciliation read views for finance (cold path)
status: backlog
created: 2026-06-02T20:14:19Z
updated: 2026-06-02T20:14:19Z
epic: prd
path: cold
depends_on: ['prd/005']
parallel: true
metrics: ['metric-reconciliation', 'tickets-zero']
hitl: ['0014']
---

# 012 — Reconciliation read views for finance (cold path)

## User story

**As a** finance analyst
**I want** read views comparing received events against applied state
**So that** my monthly reconciliation isn't a manual diff.

## Acceptance criteria

1. `derived` (line 55) — the reconciliation view lists received events and their applied outcome per period.
2. `derived` (line 63; ADR 0014) — the view surfaces received-but-not-applied and applied-without-source discrepancies (inputs to the <€50/month metric), reconciling **our store vs our applied state only**.

## Non-goals for this story

Does **not** pull PayHub's dashboard totals (ADR 0014 — store-vs-applied only for v1).

## Open questions (deferrable — finance-owned, Q11)

- Is store-vs-applied reconciliation sufficient to hit €50/month, or must we ingest PayHub totals (v1.1)? — owner: finance (ADR 0014 caveat).

## Dependencies

S005 (applied state must exist).
