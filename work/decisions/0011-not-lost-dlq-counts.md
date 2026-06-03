---
id: '0011'
title: "Not lost" semantics — a DLQ'd event counts as not lost
status: accepted
created: 2026-06-02T20:06:22Z
loop: decomposition
triggered_by: /prd:to-epic (product framing; solution-architect CP-J)
artifact: work/epics/prd/epic.md
checkpoint: raised — resolves PRD contradiction (Goal 1 absolute vs 99.9% + DLQ)
---

# 0011 — "Not lost" semantics: a DLQ'd event counts as not lost

## Context

PRD contradiction: Goal 1 (line 27) says "no payment event is ever lost" (absolute), while
the 99.9% metric (line 62) and the DLQ model (line 31) allow some events to be delayed or
diverted. Resolves analysis **Q10**.

## Options considered

- **Option A — A DLQ'd event counts as "not lost":** Goal 1 is satisfied if the event is
  durably stored somewhere a human can act. Reconciles the absolute with the 99.9%+DLQ
  model; the customer is still unfulfilled until the DLQ is drained.
- **Option B — Only fully-applied events count as "not lost":** strict reading of line 27;
  any DLQ entry is a Goal-1 miss, making Goal 1 effectively unachievable.

## Decision

**Option A — durably stored (incl. DLQ) = not lost.** "Lost" means "not durably recorded
anywhere," not "not yet applied."

## Implications

- Goal 1 ("never lost") is satisfied the moment the hot path commits the durable write —
  this is the architecture's central guarantee.
- The 99.9%-within-5-min metric (line 62) measures *timely processing*, a separate property
  from not-lost. The 0.1% are not lost; they are late or DLQ'd.
- Sets the acceptance framing `/epic:done` verifies Goal 1 against.

## Revisit if

The business decides a DLQ'd (unfulfilled) event should count as a loss for SLA/commercial
purposes — then Goal 1 and the metric must be restated together.
