---
id: '0012'
title: During-burst behaviour — validate the 5k/5min estimate first
status: accepted
created: 2026-06-02T20:06:22Z
loop: decomposition
triggered_by: /prd:to-epic (product framing; solution-architect CP-K)
artifact: work/epics/prd/epic.md
checkpoint: raised — resolves vague criterion (Goal 4 burst sizing)
---

# 0012 — During-burst behaviour: validate the 5k/5min estimate first

## Context

Goal 4 (line 30) sizes the system against "~5,000 events in 5 minutes" — labelled an
estimate with no source (silent assumption 5). The required *behaviour* during the burst
(beyond "survive") is also unstated. Resolves analysis **Q12**.

## Options considered

- **Option A — Degrade gracefully:** keep ACKing <5s, let processing latency stretch; never
  drop, never take down the app; subscribers wait longer during bursts.
- **Option B — Validate the 5k/5min estimate first:** get real data from PayHub/historical
  logs before picking a target; avoids over/under-engineering; needs data first.
- **Option C — Hard back-pressure:** return 429/5xx when the store can't keep up so PayHub
  retries; protects the system; risks PayHub's retry window.

## Decision

**Option B — validate the burst estimate before committing to a behaviour/target.**

## Implications

- S6 (burst resilience) starts with a **measurement spike**: confirm the real burst profile
  from PayHub docs / historical logs, then choose the behaviour (likely A or C).
- The during-burst target cannot be a fixed AC yet; the epic flags it as data-pending.
- Couples to ADR 0002 (Postgres) — the spike validates Postgres sustains the real rate.
- **Schedule risk for `/epic:estimate`:** S6 has an unsized investigation up front.

## Revisit if

The validated profile differs materially from 5k/5min, or Postgres cannot sustain it
(then a queue/back-pressure design per Option C is reopened).
