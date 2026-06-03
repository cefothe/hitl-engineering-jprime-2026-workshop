---
id: 'prd/007'
title: Retry transient failures, divert poison events to a DLQ, page on-call (warm)
status: backlog
created: 2026-06-02T20:14:19Z
updated: 2026-06-02T20:14:19Z
epic: prd
path: warm
depends_on: ['prd/005']
parallel: false
metrics: ['goal-5', 'tickets-zero', 'metric-99.9-5min']
hitl: ['0008', '0009', '0011']
---

# 007 — Retry transient failures, divert poison events to a DLQ, page on-call (warm)

## User story

**As an** on-call engineer
**I want** events that keep failing set aside in a DLQ and to be paged when it happens
**So that** a failing event is never silently dropped and I can act in real time.

## Acceptance criteria

1. `derived` (line 46, Goal 5) — an event whose apply throws a transient error is retried with backoff up to N attempts.
2. `derived` (Goal 5, line 31; ADR 0011) — an event exhausting N retries is moved to the DLQ (durably stored = not lost), not dropped.
3. `derived` (line 69 → ADR 0008; ADR 0009) — a DLQ entry raises a real-time page to on-call engineering.

## Non-goals for this story

No DLQ inspection UI/CLI (S009).

## Open questions (resurface as loop-2 checkpoints at /story:start — ADR 0017)

- Value of N (max retries) and the backoff schedule for v1. — owner: backend-developer + project-manager.
- Rule for classifying transient (retry) vs permanent (straight-to-DLQ) failures. — owner: backend-developer + solution-architect.

## Dependencies

S005 (the apply path).
