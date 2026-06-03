---
id: '0001'
title: PRD open-question triage (blocking vs deferrable)
status: accepted
created: 2026-06-02T19:47:01Z
loop: decomposition
triggered_by: /prd:review
artifact: work/prd/prd.analysis.md
checkpoint: raised (PRD "Open questions" classification)
---

# 0001 — PRD open-question triage (blocking vs deferrable)

## Context

`/prd:review` surfaced 13 open questions in the PayHub webhook PRD — the PRD's three
explicit ones (retention, paging-vs-digest, out-of-order) plus ten raised by the
`product-owner` (delivery semantics, identity mapping, data store, event-ID stability,
DLQ ownership, two PRD contradictions, burst behaviour, €2,400 attribution, timeline).

The PRD interpretation rules in `CLAUDE.md` forbid engineering from answering these
silently. The question of *which* must be answered before planning (the epic) and which
can be deferred is itself a decision the room owns — it sets the gate list for
`/prd:to-epic`.

## Options considered

Per open question, the room marked it **blocking** (must be resolved before the epic) or
**deferrable** (can be carried as a known unknown into decomposition/implementation).

- **Option A — treat most as blocking:** maximises planning certainty; front-loads
  stakeholder/compliance dependencies; risks stalling the epic on answers that aren't
  truly prerequisite (e.g. exact retention number).
- **Option B — defer aggressively:** keeps momentum; risks building the epic on
  assumptions that later force rework (e.g. data store, identity mapping).
- **Option C — per-question judgement:** classify each on whether the epic's *shape*
  depends on it. This is what the room did.

## Decision

The room (workshop facilitator) classified:

**Blocking (12)** — must be resolved at the `/prd:to-epic` STOP before the epic is written:
Q1 retention, Q2 paging-vs-digest, Q3 out-of-order, Q4 delivery semantics, Q5 identity
mapping, Q6 stable event ID, Q7 data store, Q8 DLQ ownership, Q9 replay scope, Q10
"not-lost" vs 99.9%, Q12 burst behaviour, Q13 timeline.

**Deferrable (1)** — carried forward, does not block the epic:
Q11 — whether the €2,400/month discrepancy is fully attributable to webhooks (a
finance-owned attribution question that does not change the system's shape).

## Implications

- `/prd:to-epic` must resolve all 12 blocking questions at its STOP gate (architect +
  product-owner framing), each producing its own ADR, before `epic.md` is written.
- The four `CLAUDE.md`-declared checkpoints (data store, identity mapping, retention,
  out-of-order) are all in the blocking set, as required.
- Q11 remains `[ ]` deferrable in the analysis; it should be revisited with finance but
  does not gate planning.

## Revisit if

A question marked deferrable turns out to change the epic's shape, or a blocking question
proves impossible to answer in time and the room chooses to convert it to an explicit
assumption instead.
