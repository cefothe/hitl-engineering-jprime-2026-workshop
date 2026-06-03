---
id: 'prd/003'
title: Reject forged/unauthentic webhooks before any durable write (hot path)
status: backlog
created: 2026-06-02T20:14:19Z
updated: 2026-06-02T20:14:19Z
epic: prd
path: hot
depends_on: ['prd/002', 'prd/001']
parallel: false
metrics: 'none: security control protecting Goal 1 integrity'
hitl: ['0013']
---

# 003 — Reject forged/unauthentic webhooks before any durable write (hot path)

## User story

**As a** payments platform owner
**I want** requests without a valid PayHub signature rejected before we persist or act on them
**So that** an attacker cannot inject fake payment events into our pipeline.

## Acceptance criteria

1. `derived` (line 43; ADR 0013) — the handler computes HMAC-SHA256 over the **raw** body using the shared secret (env var, never committed) via `javax.crypto` only, and compares to the signature header.
2. `derived` (line 43; ADR 0013) — on mismatch or missing signature it returns 4xx and performs **no** durable write.
3. `derived` (Goal 1) — on a valid signature the request proceeds to the durable write (S002).

## Non-goals for this story

No dedup (S004). No processing.

## Open questions (resurface as loop-2 checkpoints at /story:start — ADR 0017)

- Constant-time signature comparison: required security bar for v1 or deferrable? — owner: solution-architect + security.

## Dependencies

S002 (shares the hot-path handler), S001 (confirms scheme/header; AC1 may be revised if S001 finds non-HMAC).
