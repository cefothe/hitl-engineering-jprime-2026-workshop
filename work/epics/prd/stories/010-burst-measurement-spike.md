---
id: 'prd/010'
title: Measurement spike — validate the real burst profile before choosing behaviour
status: backlog
created: 2026-06-02T20:14:19Z
updated: 2026-06-02T20:14:19Z
epic: prd
path: warm
depends_on: ['prd/002']
parallel: true
metrics: ['goal-4']
hitl: ['0012', '0002']
---

# 010 — Measurement spike: validate the real burst profile before choosing behaviour

## User story

**As a** solution architect
**I want** the real PayHub burst profile confirmed and Postgres throughput measured against it
**So that** we pick a during-burst behaviour from data, not from an unvalidated 5k/5min estimate.

## Acceptance criteria

1. `derived` (Goal 4, line 30; ADR 0012) — a written finding states the measured/observed burst profile vs the 5k/5min estimate.
2. `derived` (ADR 0012; ADR 0002) — a load test of the durable write path (S002) reports whether Postgres sustains ingest at the validated rate while ACKing under 5s.
3. `assumed` (ADR 0012) — the spike raises a checkpoint proposing the during-burst behaviour (degrade gracefully vs back-pressure) — it cannot be a fixed AC until data exists.

## Non-goals for this story

No production burst-handling code (S011).

## Open questions (dependent — serial with S011, not batched)

- During-burst behaviour (degrade vs back-pressure, ADR 0012 Options A/C) — decided after this spike reports. — owner: solution-architect + product-owner.

## Dependencies

S002 (something to load-test). Couples to ADR 0002 (Postgres).
