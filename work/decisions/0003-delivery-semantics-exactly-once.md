---
id: '0003'
title: Delivery & idempotency semantics — true exactly-once (room's call)
status: accepted
created: 2026-06-02T20:06:22Z
loop: decomposition
triggered_by: /prd:to-epic (solution-architect CP-B)
artifact: work/epics/prd/epic.md
checkpoint: raised — resolves silent assumption "exactly once is achievable"
---

# 0003 — Delivery & idempotency semantics: true exactly-once

## Context

The PRD promises events are applied "exactly once" (lines 23, 27–28). The architect flagged
that end-to-end exactly-once is **not literally achievable across the PayHub network
boundary** — the buildable equivalent is at-least-once delivery + idempotent application.
Resolves analysis **Q4**.

## Options considered

- **Option A — At-least-once + idempotent application:** event-ID-keyed ledger +
  transactional apply. Actually buildable; matches "never applied twice"; requires
  restating "exactly once" as "exactly-once *effect*."
- **Option B — True exactly-once:** attempt literal exactly-once. Matches the PRD wording
  but is not achievable across the network hop; far more complex; schedule risk.

## Decision

The room chose **Option B — true exactly-once**, preferring to hold the PRD's literal
wording.

## Implications

- The team must reconcile this with physical reality: PayHub *will* re-deliver, so the
  warm-path apply still has to be idempotent regardless of the label. In practice the
  implementation will deliver exactly-once **effect** (idempotent apply over at-least-once
  receipt); "true exactly-once" across the network cannot be guaranteed by us.
- Idempotency ledger (keyed per ADR 0004) remains required.

## Revisit if

Implementation confirms (as the architect warned) that literal cross-network exactly-once
is unbuildable — at which point this should be superseded by an "exactly-once effect" ADR,
and the PRD wording updated with the product-owner. **This decision is in known tension
with networking reality and is expected to be revisited.**
