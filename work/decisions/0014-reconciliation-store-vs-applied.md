---
id: '0014'
title: Reconciliation scope — store vs applied only (v1)
status: accepted
created: 2026-06-02T20:14:19Z
loop: decomposition
triggered_by: /epic:decompose (product-owner; S012 assumed AC / Q11)
artifact: work/epics/prd/stories/012-reconciliation-read-views.md
checkpoint: raised while splitting — resolves deferrable Q11 scope
---

# 0014 — Reconciliation scope: store vs applied only (v1)

## Context

S012 must serve finance reconciliation (PRD line 55) toward the <€50/month metric (line
63). The decomposition surfaced whether reconciliation must ingest PayHub's own dashboard
totals (a three-way diff) or only compare our received-events store against our applied
aggregate state. Relates to deferrable analysis **Q11**.

## Options considered

- **Option A — Store-vs-applied only:** reconcile received events vs applied state. Smaller
  scope; the €50/month metric may not be fully achievable by this work alone.
- **Option B — Ingest PayHub totals:** pull PayHub's API/dashboard totals and three-way
  reconcile; new integration; best shot at the €50 metric.
- **Option C — Defer to finance.**

## Decision

**Option A — store-vs-applied only for v1.** The €50/month metric carries an explicit
caveat (Q11): if the residual discrepancy is not webhook-attributable, this work alone may
not hit it, and PayHub-total ingestion (Option B) becomes a follow-up.

## Implications

- S012 reconciles our store vs our applied state; it does **not** call PayHub for totals.
- `/epic:done` must record the €50/month metric as "served, with Q11 caveat" rather than
  unconditionally met.
- Q11 remains a deferrable, finance-owned question for a possible v1.1.

## Revisit if

Finance shows the residual discrepancy is webhook-side and store-vs-applied can't surface
it — then reopen Option B (ingest PayHub totals).
