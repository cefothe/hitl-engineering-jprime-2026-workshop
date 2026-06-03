---
id: 'prd/008'
title: Look up an event by PayHub event ID in under 30 seconds (cold path)
status: backlog
created: 2026-06-02T20:14:19Z
updated: 2026-06-02T20:14:19Z
epic: prd
path: cold
depends_on: ['prd/002']
parallel: true
metrics: ['goal-3', 'tickets-zero', 'tickets-did-we-get']
hitl: ['0004', '0015', '0016']
---

# 008 — Look up an event by PayHub event ID in under 30 seconds (cold path)

## User story

**As a** support agent
**I want** to look up an event by PayHub's event ID and see whether we received it, when, and its current state
**So that** I can answer "my payment isn't showing" without pinging engineering.

## Acceptance criteria

1. `derived` (line 47, Goal 3, line 64) — given a PayHub event ID, the admin endpoint/CLI returns: received (yes/no), received-at, and current state (processed / retrying / DLQ / superseded / purged).
2. `derived` (Goal 3, line 29) — the answer returns in under 30 seconds, no engineer involved.
3. `derived` (line 47) — an unknown event ID returns a clear "not received" result (distinguishable from an error).
4. `derived` (ADR 0016) — a purged event returns its non-PII tombstone (event ID, received-at, final state) marked "purged", not "not found".

## Non-goals for this story

No replay/skip (S009). No reconciliation (S012).

## Open questions (resurface as loop-2 checkpoints at /story:start — ADR 0015)

- Access control (authn/authz) for this PII-bearing lookup — decided at /story:start. — owner: product-owner + security.

## Dependencies

S002 (events must be stored). State richness improves with S005/S007 but received/received-at works once S002 lands.
