---
id: '0017'
title: Implementation-detail assumed ACs — carried to /story:start (loop 2)
status: accepted
created: 2026-06-02T20:14:19Z
loop: decomposition
triggered_by: /epic:decompose (product-owner; batched assumed-AC routing)
artifact: work/epics/prd/epic.md
checkpoint: raised while splitting — routing of implementation-time decisions
---

# 0017 — Implementation-detail assumed ACs carried to /story:start (loop 2)

## Context

Decomposition surfaced 15 `assumed` ACs. Several are genuinely implementation-time
questions visible only once a story is opened: constant-time signature compare (S003),
concurrent-duplicate race (S004), JIT user-creation guard (S005), timestamp tie-break +
unreliable-timestamp escalation (S006), retry count N + backoff schedule (S007), and the
transient-vs-poison failure classification (S007). The HITL protocol forbids these sailing
through silently — but the two-loop design routes implementation-time decisions to loop 2.

## Options considered

- **Option A — Carry to /story:start (loop 2):** keep each as a *flagged* open question on
  its story; resolve as a `loop: implementation` ADR when the story is opened. Honours both
  the no-silent-assumption rule and the two-loop design.
- **Option B — Resolve all now:** a second batched gate at decompose. Front-loads certainty;
  heavy interaction; pre-empts loop 2 with decisions lacking implementation context.
- **Option C — Accept agent defaults silently:** fastest, but violates the HITL discipline
  (assumed ACs sailing through). Rejected.

## Decision

**Option A.** Each listed assumed AC is written into its story's "Open questions" section
with an owner, explicitly flagged to resurface as a loop-2 checkpoint at `/story:start`.
They are *recorded as open*, not resolved by omission.

## Implications

- Stories S003, S004, S005, S006, S007 carry flagged open questions; their `hitl:` lists are
  expected to grow at `/story:start` — the visible proof of the second loop.
- The decompose gate does not silently accept any assumed AC; it records the *decision to
  defer* each, which is itself auditable here.

## Revisit if

An assumed AC turns out to change a story's scope materially before it is opened — pull it
forward to a decompose-time decision.
