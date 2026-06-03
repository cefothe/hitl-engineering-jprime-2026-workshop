---
id: '0009'
title: DLQ ownership — on-call engineering
status: accepted
created: 2026-06-02T20:06:22Z
loop: decomposition
triggered_by: /prd:to-epic (product framing; solution-architect CP-H)
artifact: work/epics/prd/epic.md
checkpoint: raised — DLQ ownership / drain SLA (assumption 8)
---

# 0009 — DLQ ownership: on-call engineering

## Context

Goal 5 / line 46 assume a "set aside for review" location but never say who owns it, how
it's monitored, or its drain SLA. Resolves analysis **Q8**.

## Options considered

- **Option A — On-call engineering owns + drains within X hours:** technical owner closest
  to remediation; pure engineering toil.
- **Option B — Support/finance triage, escalate to engineering:** business context; needs
  friendly cold-path tooling.
- **Option C — Shared (eng mechanics + support business decisions, defined SLA):** matches
  reality; needs a clear hand-off.

## Decision

**Option A — on-call engineering owns the DLQ** and its drain.

## Implications

- Pairs with ADR 0008 (real-time paging) — the pager targets the on-call engineer.
- The cold-path tooling (S5) is built for an engineering audience (CLI/admin endpoint is
  acceptable per PRD non-goals).
- A drain-SLA number is owed (currently "within X hours" placeholder) — flag for
  `/epic:estimate` / ops.

## Revisit if

Engineering toil from DLQ triage becomes excessive, or business-context decisions (e.g.
refund disputes) require support ownership — move toward Option C.
