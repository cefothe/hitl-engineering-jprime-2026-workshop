---
id: '0016'
title: Purged-event lookup — keep non-PII tombstone metadata
status: accepted
created: 2026-06-02T20:14:19Z
loop: decomposition
triggered_by: /epic:decompose (product-owner; S013 assumed AC)
artifact: work/epics/prd/stories/013-retention-purge.md
checkpoint: raised while splitting — retention/lookup interaction
---

# 0016 — Purged-event lookup: keep non-PII tombstone metadata

## Context

When the retention purge job (S013, ADR 0007) deletes a raw payload, support lookup (S008,
Goal 3) must still behave sensibly. The decomposition surfaced what lookup shows for a
purged event.

## Options considered

- **Option A — Tombstone metadata:** purge only the raw PII-bearing payload; retain non-PII
  metadata (event ID, received-at, final state). Lookup shows "purged" + that metadata —
  preserves Goal 3 lookup after purge while honouring no-PII-at-rest.
- **Option B — Not found:** delete the whole record; lookup returns "not received". Simpler;
  loses the audit trail for old events (weakens Goal 3 for aged events).
- **Option C — Defer to /story:start.**

## Decision

**Option A — retain a non-PII tombstone.** The purge removes the raw payload (PII) but keeps
event ID, received-at, and final state so lookup still answers "did we receive it, and what
happened."

## Implications

- S013's purge must split PII (raw payload) from non-PII metadata in the schema — affects
  the store contract (ADR 0002) and S008's lookup response (a "purged" state).
- Honours CLAUDE.md no-PII-at-rest while preserving Goal 3 for aged events.

## Revisit if

Compliance requires full deletion (incl. metadata) of aged records — then Option B.
