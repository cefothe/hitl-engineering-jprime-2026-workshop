---
id: 'prd/011'
title: Implement validated burst behaviour (warm/hot)
status: backlog
created: 2026-06-02T20:14:19Z
updated: 2026-06-02T20:14:19Z
epic: prd
path: warm
depends_on: ['prd/010', 'prd/002', 'prd/005']
parallel: false
metrics: ['goal-4', 'metric-99.9-5min']
hitl: ['0012']
---

# 011 — Implement validated burst behaviour (warm/hot)

## User story

**As a** platform owner
**I want** the system to behave correctly during a PayHub backlog burst
**So that** we never drop events or take down the rest of the app.

## Acceptance criteria

1. `assumed` (Goal 4, line 30; ADR 0012 — **data-pending on S010**) — given the burst behaviour chosen from S010, a burst at the validated rate is handled by [degrade gracefully keeping ACK <5s, OR back-pressure 429/5xx]. *AC cannot be fixed until S010 reports.*
2. `derived` (Goal 4; Goal 1) — during a burst no event is dropped and the rest of the app stays responsive.
3. `derived` (Goal 1) — a burst exceeding the validated rate still durably retains events (ADR 0011), not lost.

## Non-goals for this story

No re-architecture to a managed queue unless S010 proves Postgres insufficient.

## Open questions (dependent on S010)

- Final behaviour + target — blocked until S010's measurement reports. — owner: solution-architect.

## Dependencies

S010 (behaviour + target undecided until the spike reports), S002, S005.
