---
id: '0007'
title: Retention — config value, block go-live until compliance answers
status: accepted
created: 2026-06-02T20:06:22Z
loop: decomposition
triggered_by: /prd:to-epic (product framing; solution-architect CP-F)
artifact: work/epics/prd/epic.md
checkpoint: declared — "Event retention period" (CLAUDE.md)
---

# 0007 — Retention: config value, block go-live until compliance answers

## Context

The PM placeholder is "a while" (PRD line 68); compliance owns the real number and hasn't
given one. `CLAUDE.md` reserves retention as a checkpoint. Resolves analysis **Q1**.

## Options considered

- **Option A — Config value + block go-live:** ship retention as configuration with a
  placeholder, build the purge job, but block "done" until compliance answers. Unblocks the
  build without guessing; real number still owed before launch.
- **Option B — Fixed 90 days:** conservative default + purge job; guesses compliance's answer.
- **Option C — Long (7 years):** safe against legal retention; large storage + longer
  PII-at-rest exposure (CLAUDE.md no-PII concern).

## Decision

**Option A — retention is a config value; go-live is blocked until compliance supplies the
number.**

## Implications

- S9 (retention enforcement) builds a parameterised purge job but stays **open** until the
  compliance value lands — this is an explicit gate on `/epic:done`.
- Engineering does not invent the number (honours the PRD interpretation rule).
- Raw payloads carry PII — purge correctness matters for the no-PII-at-rest rule.

## Revisit if

Compliance supplies a firm retention requirement (then set the config and unblock S9), or
legal classifies the payloads such that a hard maximum applies.
