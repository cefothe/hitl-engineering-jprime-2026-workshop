---
id: '0021'
title: Hot-path connection pooling — HikariCP (standalone)
status: accepted
created: 2026-06-02T20:24:12Z
loop: implementation
triggered_by: /story:start (backend-developer, prd/002)
artifact: work/epics/prd/stories/002-durable-ingest-ack.md
checkpoint: raised — does a pooling library breach the hot-path framework ban?
---

# 0021 — Hot-path connection pooling: HikariCP (standalone)

## Context

The hot path writes via plain JDBC (ADR 0002) and must sustain burst throughput while
ACKing <5s. Without Spring, *something* must pool connections. The framework ban targets
Spring/DI/AOP — but a pool "looks framework-y" and the developer rightly asked the room to
ratify rather than assume.

## Options considered

- **Option A — HikariCP (standalone library):** battle-tested pooling; no Spring dependency
  (commonly used *by* Spring, but usable plain). Must verify it pulls nothing Spring
  transitively.
- **Option B — JDK driver pool only** (`PGConnectionPoolDataSource` / hand-rolled): zero
  extra deps, purest reading of the ban; less proven under burst, more hand-rolling.

## Decision

**Option A — HikariCP as a standalone library**, hand-wired in `main` (no `@Bean`, no Spring
auto-config). The ban is about Spring/DI, not third-party libraries; HikariCP used plainly
does not breach it.

## Implications

- **New task:** verify HikariCP's transitive graph is Spring-free and add it to the ArchUnit
  allowlist (ADR 0019). Configure pool sizing for the burst target (ADR 0012 spike informs).
- Pool lifecycle is hand-managed: open at startup, close on shutdown — no Spring lifecycle.
- Reinforces that "no framework" ≠ "no libraries" — documents the boundary for future PRs.

## Revisit if

HikariCP is found to pull framework artifacts that can't be excluded, or the burst spike
(ADR 0012) shows a different pooling strategy is needed.
