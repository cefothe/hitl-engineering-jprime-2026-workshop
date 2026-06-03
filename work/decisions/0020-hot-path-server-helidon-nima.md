---
id: '0020'
title: Hot-path HTTP server — Helidon Níma
status: accepted
created: 2026-06-02T20:24:12Z
loop: implementation
triggered_by: /story:start (backend-developer, prd/002)
artifact: work/epics/prd/stories/002-durable-ingest-ack.md
checkpoint: raised — hot-path server choice (sub-choice under CLAUDE.md hot-path rule)
---

# 0020 — Hot-path HTTP server: Helidon Níma

## Context

The hot path may use "JDK `HttpServer` or Helidon Níma" (CLAUDE.md). Opening prd/002 forced
the concrete pick, which affects burst ergonomics (ADR 0012) and the framework-ban audit.

## Options considered

- **Option A — JDK `HttpServer`:** zero dependencies, nothing to vet for Spring leakage;
  primitive (manual executor, no built-in virtual-thread integration).
- **Option B — Helidon Níma:** Loom/virtual-thread-native serving, better burst ergonomics;
  a dependency whose transitive graph must be proven Spring-free.

## Decision

**Option B — Helidon Níma**, for virtual-thread-native serving aligned with the <5s ACK +
burst targets.

## Implications

- **New task:** vet Helidon Níma's transitive dependency graph to prove it pulls no
  `spring-*` / DI / `jakarta.persistence` artifacts onto the ingress classpath — and add it
  to the ArchUnit allowlist (ADR 0019).
- Still pure-Java, no Spring/DI — Níma is a serving library, not a DI framework; the ban
  holds.
- **Effort/scope impact:** dependency-vetting task feeds the re-forecast flag (ADR 0019).

## Revisit if

Níma's transitive graph proves to carry framework artifacts that can't be excluded, or its
virtual-thread benefit doesn't materialise under the validated burst (ADR 0012) — fall back
to JDK `HttpServer` (Option A).
