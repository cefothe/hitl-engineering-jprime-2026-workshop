---
id: '0006'
title: Out-of-order events — timestamp last-writer-wins
status: accepted
created: 2026-06-02T20:06:22Z
loop: decomposition
triggered_by: /prd:to-epic (product framing; solution-architect CP-E)
artifact: work/epics/prd/epic.md
checkpoint: declared — "Whether out-of-order events matter, and how" (CLAUDE.md)
---

# 0006 — Out-of-order events: timestamp last-writer-wins

## Context

PayHub "sometimes sends events out of order" (PRD line 70). `CLAUDE.md` requires this be
*decided*, not silently omitted. Resolves analysis **Q3**.

## Options considered

- **Option A — Per-aggregate version guards:** apply only if newer than current aggregate
  state; correct under reordering; needs per-aggregate versioning + conflict policy.
- **Option B — Ignore ordering:** process in arrival order; a late `payment.succeeded`
  after `refund.created` could yield wrong final state.
- **Option C — Timestamp last-writer-wins:** lighter than full versioning; relies on
  trustworthy PayHub timestamps; clock-skew risk.

## Decision

**Option C — timestamp last-writer-wins**, using PayHub's event timestamp to decide whether
an arriving event supersedes the current aggregate state.

## Implications

- Each aggregate (payment/subscription/refund) records the timestamp of the last applied
  event; an event older than that is ignored (but still marked received/processed).
- **Risk:** correctness depends on PayHub timestamp trustworthiness and bounded clock skew —
  the spike (ADR 0004) should also capture timestamp semantics.
- Affects S2 and adds an ordering check; replaces the conditional S7 slice.

## Revisit if

PayHub timestamps prove unreliable or clock skew is material — escalate to Option A
(per-aggregate version guards).
