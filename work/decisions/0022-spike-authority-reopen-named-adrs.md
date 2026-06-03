---
id: '0022'
title: Spike authority — may reopen only the named ADRs (0004/0006/0013)
status: accepted
created: 2026-06-02T20:24:12Z
loop: implementation
triggered_by: /story:start (backend-developer, prd/001)
artifact: work/epics/prd/stories/001-payhub-spike.md
checkpoint: raised — prd/001 AC4 (assumed): spike's authority to reopen a decided ADR
---

# 0022 — Spike authority: may reopen only the named ADRs (0004/0006/0013)

## Context

prd/001 is the PayHub spike. Its only `assumed` AC (AC4) asks whether the spike may STOP and
reopen an already-*accepted* ADR if PayHub behaviour contradicts it. The three target ADRs
each carry a "Revisit if" clause this spike is the designated trigger for (0004 IDs mutate →
Option C; 0006 timestamps unreliable → Option A; 0013 non-HMAC scheme → Option B). Needed
before the spike runs.

## Options considered

- **Option A — Authorise reopen (any ADR):** on any contradiction the spike halts and raises
  a gate. Nothing buried; but a spike can block dependent stories mid-run.
- **Option B — Report-only:** spike records the contradiction + flags the ADR stale;
  reopening is a separate deliberate step. Clean separation; risk a contradiction sits
  unactioned while S003/S004/S006 build on a stale ADR.
- **Option C — Authorise reopen for the named ADRs only** (0004/0006/0013), report-only
  otherwise. Bounded blast radius; matches exactly the ADRs the spike is scoped to test.

## Decision

**Option C.** On a finding that contradicts 0004, 0006, or 0013, the spike STOPs and raises
an `AskUserQuestion` to reopen *that* ADR (escalating to its "Revisit if" option). Any
contradiction outside those three is recorded as a finding and flagged, not auto-reopened.

## Implications

- The spike is the controlled mechanism that can falsify three dependency-bearing decisions
  before downstream stories build on them — exactly its de-risking purpose.
- A reopened ADR produces a new `loop: implementation` decision and flags the estimate stale.
- Captured payloads carry PII: the spike must never commit raw payloads and must redact PII
  in findings (CLAUDE.md no-PII rule) — record field names/structure, never customer values.

## Revisit if

A surprise contradiction outside the three named ADRs proves material enough that report-only
handling is too weak — widen to Option A.
