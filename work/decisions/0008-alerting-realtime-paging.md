---
id: '0008'
title: Failure alerting — real-time paging
status: accepted
created: 2026-06-02T20:06:22Z
loop: decomposition
triggered_by: /prd:to-epic (product framing; solution-architect CP-G)
artifact: work/epics/prd/epic.md
checkpoint: raised — PRD open question (paging vs digest)
---

# 0008 — Failure alerting: real-time paging

## Context

PRD open question (line 69): on repeated failure, do we page on-call or is a daily digest
enough for v1? Resolves analysis **Q2**.

## Options considered

- **Option A — Threshold-based:** digest normally, page when DLQ depth/rate crosses a bound;
  balances noise vs responsiveness; needs tuning.
- **Option B — Daily digest:** low noise; a customer could wait up to a day.
- **Option C — Real-time paging:** fast remediation; on-call burden + alert-fatigue risk.

## Decision

**Option C — real-time paging** when an event has failed too many times and lands in the DLQ.

## Implications

- Pairs with ADR 0009 (on-call engineering owns the DLQ) — the pager goes to that rota.
- The DLQ must avoid noisy false positives or paging becomes fatigue; tune the
  failure-threshold before a poison event can page repeatedly.
- Adds an alerting integration to S3 (retry + DLQ).

## Revisit if

Paging volume proves unsustainable (alert fatigue) — fall back to Option A (threshold).
