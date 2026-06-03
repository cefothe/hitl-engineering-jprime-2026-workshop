---
id: '0005'
title: Identity mapping — lazy just-in-time
status: accepted
created: 2026-06-02T20:06:22Z
loop: decomposition
triggered_by: /prd:to-epic (product framing; solution-architect CP-D)
artifact: work/epics/prd/epic.md
checkpoint: declared — "PayHub customer_id → internal user_id mapping" (CLAUDE.md)
---

# 0005 — Identity mapping: lazy just-in-time

## Context

Events must update "the right thing in our system" (PRD line 45), assuming a
`customer_id → user_id` mapping the PRD never describes. `CLAUDE.md` reserves this as a
checkpoint. Resolves analysis **Q5**.

## Options considered

- **Option A — Mapping exists; orphans → DLQ:** simple, fails loud; real customers could
  land in DLQ if mapping lags.
- **Option B — Lazy just-in-time:** resolve or create mapping on first event. Tolerates new
  customers; risks fabricating users from spoofed-but-valid-signature events.
- **Option C — Buffer-and-park orphans:** holds unmatched events pending with retry + SLA
  alert; doesn't lose or fabricate; adds a new state to manage.

## Decision

**Option B — lazy just-in-time mapping.**

## Implications

- The warm path resolves-or-creates the internal user on first event for a customer.
- **Mitigation required:** because signature is verified on the hot path (ADR 0013), a
  forged event cannot reach mapping — but the team must ensure JIT creation can't be abused
  by a replay of a genuine event for a non-existent customer. Document the guard.
- Affects S2 (warm processing core).

## Revisit if

JIT creation produces bogus/duplicate users in practice, or product decides orphan events
must be parked rather than auto-provisioned.
