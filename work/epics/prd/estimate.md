---
name: prd
created: 2026-06-02T20:20:10Z
updated: 2026-06-02T20:20:10Z
forecast_date: 2026-06-30
confidence: '80% CI ~2026-06-23 to ~2026-07-07 (3–5 weeks); likely ~4 weeks'
critical_path: ['prd/001', 'prd/002', 'prd/005', 'prd/007', 'prd/009']
needs_reforecast: true
reforecast_reason: '/story:start (prd/002) recorded loop-2 ADRs 0019/0020/0021 adding tasks not in the original estimate — ArchUnit CI gate (single-module enforcement) plus transitive-dependency vetting of Helidon Níma and HikariCP for Spring-freeness. Re-run /epic:estimate to re-size prd/002.'
hitl: ['0018']
---

# Estimate — Reliable Webhook Processing for Payment Events

Produced by the `project-manager` via `/epic:estimate`. The room **accepted ~4 weeks, full
scope** (ADR 0018) — the 3-week target is not met; no stories were cut.

**Forecasting assumption:** ~2 backend engineers full-time, ~65% focus factor. Postgres
assumed provisioned (else +1–2 days).

## Per-story estimate (engineer-days: optimistic / likely / pessimistic)

| Story | Type | Opt | Likely | Pess |
|---|---|---|---|---|
| prd/001 PayHub spike | spike | 1 | 2 | 4 |
| prd/002 Durable ingest + ACK | build | 2 | 3 | 5 |
| prd/003 Signature verification | build | 1 | 1.5 | 3 |
| prd/004 Idempotent duplicate ingest | build | 1 | 1.5 | 3 |
| prd/005 Warm processing core | build | 3 | 5 | 8 |
| prd/006 Timestamp LWW | build | 1.5 | 2.5 | 5 |
| prd/007 Retry + DLQ + paging | build | 2 | 3 | 5 |
| prd/008 Event lookup | build | 1.5 | 2.5 | 4 |
| prd/009 DLQ replay/skip | build | 2 | 3 | 4 |
| prd/010 Burst spike | spike | 1 | 2 | 4 |
| prd/011 Burst behaviour | build (blocked on 010) | 2 | 4 | 8+ |
| prd/012 Reconciliation views | build | 1.5 | 2.5 | 4 |
| prd/013 Retention purge | blocked (compliance) | 1 | 2 | 3 |
| **Total** | | **20.5** | **34.5** | **60+** |

## Critical path

`prd/001 → prd/002 → prd/005 → prd/007 → prd/009` — ~15 serialised engineer-days that two
engineers cannot meaningfully split (each link needs the prior output). Near-critical
co-path: `prd/010 → prd/011` (burst). Parallel fill for the second engineer: 001, 010, 008,
012, 003, 004, 006. Adding a third engineer buys little — the chain is serial by
dependency, not capacity.

## Top 3 risks

1. **Spike invalidates an ADR (0004/0006/0013).** Trigger: 001 finds event IDs unstable /
   timestamps unreliable / non-HMAC scheme. Mitigation: run 001 day 1, time-box 2 days,
   treat a contradicting finding as a planned re-decision (story 001 AC4).
2. **Burst (011) unsized, may need re-architecture.** Trigger: 010 finds Postgres can't
   sustain the rate while ACKing <5s. Mitigation: run 010 early; pre-agree fallback "degrade
   gracefully + durably retain" for v1, defer queue to v1.1 (preserves Goal 1).
3. **Compliance retention number doesn't land → S013 go-live gate.** Trigger: monthly close
   arrives, no number (PRD line 68). Mitigation: PO escalates today with "blocks go-live, need
   a number by week-2"; purge built parameterised so only the value is missing.

Honourable mention: ADR 0003 "true exactly-once" is in known tension with network reality;
team builds exactly-once *effect*. Revisit 0003 → "exactly-once effect" and align PRD wording
with the PO before go-live.

## Forecast vs target

3 weeks ≈ 15 working days ≈ ~19–20 effective engineer-days (2 eng @65%). Demand ~34.5
likely. **3 weeks is not realistic for full scope** — likely **~4 weeks**, landing on the
monthly close with **no buffer**. Room accepted this (ADR 0018). Go-live additionally gated
by S013 (compliance) and the 010/011 burst data, which the engineering estimate cannot bound.

## Mandatory actions (from the accepted plan)

- Run spikes **prd/001 and prd/010 on day 1** — they bound the two biggest unknowns.
- **Escalate the compliance retention number today** (PO-owned) — it gates go-live in every
  scenario.
