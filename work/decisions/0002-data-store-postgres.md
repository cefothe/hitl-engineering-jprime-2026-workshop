---
id: '0002'
title: Data store — PostgreSQL
status: accepted
created: 2026-06-02T20:06:22Z
loop: decomposition
triggered_by: /prd:to-epic (solution-architect CP-A)
artifact: work/epics/prd/epic.md
checkpoint: declared — "Choosing the data store" (CLAUDE.md)
---

# 0002 — Data store: PostgreSQL

## Context

The PRD is silent on persistence, yet Goals 1 & 2 (no loss, no double-apply) depend on a
durable store shared across the hot/warm framework boundary. `CLAUDE.md` reserves the
store choice for the human. Resolves analysis **Q7**.

## Options considered

- **Option A — PostgreSQL:** single store for durable ingest, idempotency ledger, DLQ,
  reconciliation views; sustains burst writes; `SELECT … FOR UPDATE SKIP LOCKED` gives a
  clean warm-path claim; real hot/warm concurrency. Cost: a server to operate + back up.
- **Option B — SQLite:** zero-ops, trivial local dev; but single-writer contention between
  hot and warm paths, likely fails the burst target. A timeline-optimising stakeholder
  could prefer it if burst scope were cut.
- **Option C — Embedded H2:** easy on the Spring side; weaker cross-restart durability and
  unproven burst concurrency; hot path needs plain JDBC regardless.

## Decision

**PostgreSQL.** Chosen by the room for durability, concurrency, and a single store across
all three paths.

## Implications

- The hot path writes via **plain JDBC only** — never a Spring `Repository` or JPA
  `@Entity` (would breach the hot-path framework ban; see ADR 0013 / architect tension T1).
- The store schema is a cross-framework contract owned by neither Spring nor the ingress.
- Local dev needs docker-compose for Postgres; document the env vars.
- Couples to ADR 0012 (burst): Postgres is expected to sustain the validated burst rate.

## Revisit if

Operational cost proves prohibitive, or a validated burst profile (ADR 0012) shows a
managed queue is warranted in front of the store.
