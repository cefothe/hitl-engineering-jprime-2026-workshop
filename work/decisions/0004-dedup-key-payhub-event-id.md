---
id: '0004'
title: Dedup key — trust PayHub event ID
status: accepted
created: 2026-06-02T20:06:22Z
loop: decomposition
triggered_by: /prd:to-epic (solution-architect CP-C)
artifact: work/epics/prd/epic.md
checkpoint: raised — resolves silent assumption "stable unique event ID"
---

# 0004 — Dedup key: trust PayHub event ID

## Context

Both idempotency (Goals 1–2) and support lookup (Goal 3) assume PayHub sends a stable,
unique event ID that survives retries — assumed but unconfirmed. Resolves analysis **Q6**.

## Options considered

- **Option A — Trust PayHub event ID:** unique constraint at ingest. Simplest, cheapest;
  breaks silently if PayHub mutates/omits IDs on retry.
- **Option B — Content hash:** robust if IDs unreliable; hashing on the hot path; equivalent
  retries that differ byte-wise could slip.
- **Option C — Event ID + hash fallback:** most robust, most complex (two dedup paths).

## Decision

**Option A — trust PayHub's event ID** as the unique idempotency key.

## Implications

- A unique constraint on the event ID makes duplicate POSTs an idempotent no-op-but-ACK.
- **A spike must confirm PayHub's event-ID stability** before S1 is closed — this decision
  rests on an unverified assumption (architect CP-C dependency note).
- Support lookup (Goal 3) keys off the same ID.

## Revisit if

The spike shows PayHub mutates or omits event IDs on retry — then escalate to Option C.
