---
id: 'prd/001'
title: Spike — confirm PayHub event-ID stability, signature scheme, timestamp semantics
status: in-progress
created: 2026-06-02T20:14:19Z
updated: 2026-06-02T20:24:12Z
epic: prd
path: n/a
depends_on: []
parallel: true
metrics: 'none: de-risks S002–S006; produces no measurable PRD outcome itself'
hitl: ['0004', '0006', '0013', '0022']
---

# 001 — Spike: confirm PayHub event-ID stability, signature scheme, timestamp semantics

## User story

**As a** backend engineer
**I want** to confirm from PayHub's docs and a captured sample of retried events whether the event ID is stable, what signature scheme PayHub uses, and whether event timestamps are trustworthy
**So that** S002/S003/S004/S006 build on confirmed behaviour instead of three stacked assumptions.

## Acceptance criteria

1. `derived` (line 47 / Goal 3; ADR 0004) — a written finding states whether the event ID is identical across original and retried delivery, and unique per logical event.
2. `derived` (line 43; ADR 0013) — a written finding states the exact signature scheme (confirm HMAC-SHA256 over raw body, or name the actual scheme) and the header that carries it.
3. `derived` (line 70; ADR 0006) — a written finding states whether events carry a usable per-event timestamp and PayHub's stated ordering guarantees.
4. `assumed` — if a finding contradicts ADR 0004/0006/0013, the spike raises a checkpoint to revisit that ADR rather than proceeding.

## Non-goals for this story

No production code. No endpoint. Findings only.

## Open questions

- Is the spike authorised to STOP and reopen a decided ADR if PayHub behaviour contradicts it? — owner: solution-architect (resurfaces at /story:start).

## Dependencies

None. Should start day 1; informs S002/S003/S004/S006.

## Tasks (discovered at start)

Surfaced by `backend-developer` at `/story:start`; AC4 authority settled by ADR 0022
(spike may reopen only 0004/0006/0013 on a contradiction).

1. Locate PayHub's authoritative webhook docs (event envelope, retry policy, signature
   header, timestamp field); record the URL + access date verbatim in the finding.
2. Confirm whether a PayHub sandbox/test account or API credentials exist — the single
   biggest unknown; the spike needs either a forced-retry capability or captured deliveries.
3. Obtain a captured retried delivery: prefer PayHub's dashboard delivery-log/replay; else
   an ingress/proxy log retaining raw bodies; else stand up a throwaway capture endpoint and
   force a retry (return non-2xx).
4. Per AC1 (ADR 0004): record event-ID field path, whether it is byte-identical across
   original and retry, and whether unique per logical event vs per attempt — mark *observed*
   vs *documented*.
5. Per AC2 (ADR 0013): record exact signature scheme, header name, encoding (hex/base64),
   and whether signed over raw bytes or canonicalised body. If docs are ambiguous, recompute
   HMAC over a captured raw body with the shared secret (env var) to confirm raw-vs-canonical.
6. Per AC3 (ADR 0006): record timestamp field path, format/precision/timezone, per-event vs
   per-delivery, and PayHub's stated ordering guarantee (or lack of one).
7. Write findings as a PII-redacted spike report — **field names/structure only, never
   customer values; never commit a raw captured payload** (CLAUDE.md no-PII / no-raw-payload).
   *(Open: report location convention — story is `path: n/a`.)*

**Boundaries to respect:** captured payloads are PII (redact in findings, store only in an
approved ephemeral location, never in the repo); HMAC secret via env var only. On a finding
that contradicts 0004/0006/0013, STOP and reopen that ADR (ADR 0022).
