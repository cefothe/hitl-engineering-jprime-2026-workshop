---
id: '0010'
title: Replay/skip scope — any stored event
status: accepted
created: 2026-06-02T20:06:22Z
loop: decomposition
triggered_by: /prd:to-epic (product framing; solution-architect CP-I)
artifact: work/epics/prd/epic.md
checkpoint: raised — resolves PRD contradiction (non-goal line 37 vs user story line 57)
---

# 0010 — Replay/skip scope: any stored event

## Context

PRD contradiction: line 37 makes historical replay a non-goal (a one-off cleanup), while
line 57 wants on-call to "replay or skip individual events without writing a one-off
script." Resolves analysis **Q9**.

## Options considered

- **Option A — DLQ replay + explicit skip (no arbitrary replay):** covers line 57 without
  opening historical replay; less flexible.
- **Option B — DLQ-only replay/skip:** only post-launch failed events; honours the non-goal
  most strictly; bounded.
- **Option C — Any-stored-event replay:** replay any event by ID regardless of state;
  maximal on-call power; re-applying leans entirely on idempotency; blurs the non-goal line.

## Decision

**Option C — any-stored-event replay** (by event ID, any state).

## Implications

- On-call can re-apply any *stored* event; safety rests entirely on the idempotency design
  (ADR 0003/0004) — re-applying an already-applied event must be a no-op.
- **Tension with PRD non-goal (line 37):** this widens replay beyond DLQ. The non-goal about
  *pre-launch historical* events still holds (those were never stored); document the line
  clearly so "any stored event" is not read as "replay history."
- Expands S5 (cold-path tooling) scope.

## Revisit if

Arbitrary replay causes incorrect re-application (idempotency gap found) — narrow to
Option A.
