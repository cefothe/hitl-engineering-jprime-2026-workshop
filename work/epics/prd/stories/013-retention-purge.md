---
id: 'prd/013'
title: Parameterised retention purge job — BLOCKED on compliance (cold path)
status: backlog
created: 2026-06-02T20:14:19Z
updated: 2026-06-02T20:14:19Z
epic: prd
path: cold
depends_on: ['prd/002', 'prd/008']
parallel: true
metrics: 'none: compliance/no-PII-at-rest control; hard go-live gate per ADR 0007'
hitl: ['0007', '0016']
---

# 013 — Parameterised retention purge job — BLOCKED on compliance (cold path)

## User story

**As a** compliance/data owner
**I want** raw event payloads purged after a configured retention period
**So that** we don't hold payment PII at rest longer than legally required.

## Acceptance criteria

1. `derived` (ADR 0007) — given a retention period supplied as configuration, the purge job deletes raw payloads older than that period.
2. `derived` (ADR 0007, ADR 0016; CLAUDE.md no-PII-at-rest) — the purge removes raw PII-bearing payloads while preserving the non-PII tombstone (event ID, received-at, final state) for lookup/reconciliation.
3. `derived` (ADR 0007 — go-live gate) — if compliance has **not** supplied the number, `/epic:done` keeps this story blocked and gates go-live.

## Non-goals for this story

Engineering does not invent the retention number (ADR 0007).

## Open questions (blocking — compliance-owned)

- The retention period value itself — owed by compliance before go-live (ADR 0007). — owner: product-owner + compliance.

## Dependencies

S002 (data to purge), S008 (purged-event lookup interaction, ADR 0016).
