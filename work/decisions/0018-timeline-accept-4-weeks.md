---
id: '0018'
title: Timeline — accept ~4 weeks, full scope
status: accepted
created: 2026-06-02T20:20:10Z
loop: decomposition
triggered_by: /epic:estimate (project-manager)
artifact: work/epics/prd/estimate.md
checkpoint: raised — resolves blocking analysis question Q13
---

# 0018 — Timeline: accept ~4 weeks, full scope

## Context

The PRD targets production within 3 weeks, with the next monthly close in 4 (PRD line 74).
The project-manager forecast **~4 weeks likely (80% CI 3–5)** for the full 13-story scope,
driven by a serial ~15-engineer-day critical path (`001 → 002 → 005 → 007 → 009`), two
spikes (001, 010) feeding decisions that could reopen ADRs, and the S013 compliance gate on
go-live. Resolves analysis **Q13** (deferred here from `/prd:to-epic`).

## Options considered

- **Option A — Cut to core (~3 weeks):** defer S009, S011, S012 to v1.1; ship the no-loss /
  no-double-apply / lookup core. Hits ~3–3.5 weeks code-complete; reconciliation pain (the
  PM's motivating driver) persists one more close.
- **Option B — Accept ~4 weeks, full scope:** keep all 13 stories; code-complete lands on
  the monthly close with no buffer. Fragile — any top-3 risk pushes past the close.
- **Option C — Move the date** past the next close to absorb spike + compliance risk.

## Decision

The room chose **Option B — accept ~4 weeks for the full scope.** No stories are cut; all
13 remain in plan.

## Implications

- Forecast: code-complete ~2026-06-30 (CI ~2026-06-23 to ~2026-07-07). **No buffer** before
  the monthly close — explicitly accepted.
- The three PM risks are now live without a scope-cut shock absorber: a spike invalidating
  an ADR (Risk 1), burst re-architecture (Risk 2), or the compliance gate (Risk 3) each
  threaten the close. **Mitigations are mandatory, not optional:** run both spikes day 1,
  escalate the retention number to compliance today (PO-owned).
- Go-live remains gated by S013 (ADR 0007) regardless of code-complete.
- `estimate.md` carries this forecast; `/pm:status` forecasts against it; a loop-2 decision
  that changes scope flags it `needs_reforecast: true`.

## Revisit if

A spike invalidates an ADR, the burst spike (010) shows Postgres insufficient, or compliance
cannot supply the retention number in time — any of which should reopen this as a cut-scope
(Option A) or move-date (Option C) decision.
