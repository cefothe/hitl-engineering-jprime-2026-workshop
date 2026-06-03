---
id: '0015'
title: Cold-path access control — decided at /story:start (loop 2)
status: accepted
created: 2026-06-02T20:14:19Z
loop: decomposition
triggered_by: /epic:decompose (product-owner; S008/S009 assumed ACs)
artifact: work/epics/prd/stories/008-event-lookup.md
checkpoint: raised while splitting — routing of an implementation-time decision
---

# 0015 — Cold-path access control: decided at /story:start (loop 2)

## Context

S008 (PII-bearing event lookup) and S009 (destructive replay/skip) both have an `assumed`
AC for authn/authz — the PRD allows a CLI/admin endpoint (non-goal line 36) but is silent
on access control. The question is *when* to decide it, not just what.

## Options considered

- **Option A — Decide at /story:start (loop 2):** carry as flagged `assumed` open questions
  on S008/S009; resolve the concrete mechanism when those stories are opened. Keeps the
  two-loop design intact (implementation-time decisions belong to loop 2).
- **Option B — Require auth now (hard AC at decompose):** locks the bar early; pre-empts
  loop 2; risks deciding without implementation context.
- **Option C — Internal-only for v0** with an out-of-scope ADR.

## Decision

**Option A — defer the concrete access-control decision to `/story:start`.** Both stories
carry an explicit open question (owner: product-owner + security) that will resurface as a
loop-2 checkpoint and produce its own `loop: implementation` ADR.

## Implications

- S008 and S009 ship to decomposition with an unresolved, *flagged* access-control open
  question — not a silent assumption.
- The proof of loop 2: these stories' `hitl:` lists will grow when `/story:start` records
  the access-control ADR.

## Revisit if

A security/compliance requirement forces the auth bar to be fixed before any cold-path work
begins — then resolve at decompose instead.
