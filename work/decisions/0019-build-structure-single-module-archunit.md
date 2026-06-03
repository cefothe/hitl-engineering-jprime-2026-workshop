---
id: '0019'
title: Build structure — single module + ArchUnit enforcement
status: accepted
created: 2026-06-02T20:24:12Z
loop: implementation
triggered_by: /story:start (backend-developer, prd/002)
artifact: work/epics/prd/stories/002-durable-ingest-ack.md
checkpoint: declared — CLAUDE.md "Build structure" TODO (was unresolved)
---

# 0019 — Build structure: single module + ArchUnit enforcement

## Context

`CLAUDE.md.template` line 38 left "multi-module Maven, or single-module with package
enforcement" as a TODO for the room — **no prior ADR resolved it.** Opening prd/002 forced
it: the first hot-path file *is* this decision (it determines whether the Spring ban is a
physical fact or a policed rule). A genuine loop-2 checkpoint — invisible until
implementation began.

## Options considered

- **Option A — Multi-module Maven:** `ingress` module declares only JDK + driver + pool;
  Spring is physically absent from its classpath. Strongest guarantee; more ceremony.
- **Option B — Single module + ArchUnit:** one module; an ArchUnit/Checkstyle rule bans
  `org.springframework.*` / `jakarta.persistence.*` imports under the ingress package. Less
  ceremony; Spring stays on the classpath, so the ban is a CI-enforced *rule*, not a *fact*.
- **Option C — Separate projects:** fully independent builds; max isolation, heaviest
  coordination for the shared schema contract.

## Decision

**Option B — single module with an ArchUnit enforcement rule.**

## Implications

- A new task: author an ArchUnit (or equivalent) rule banning Spring/DI/JPA imports under
  the ingress package, **wired into CI** so it fails the build on violation. This realises
  prd/002 AC5 — but as a *policed rule*, not physical isolation.
- Because Spring is on the classpath, the **code-reviewer's hot-path guard becomes more
  important**, not less — the ArchUnit rule is the safety net, reviewer is the backstop.
- **Effort/scope impact:** adds the ArchUnit-rule + CI-gate task to prd/002 not itemised in
  the original estimate → triggers a re-forecast flag (see estimate `needs_reforecast`).

## Revisit if

A Spring import slips past the rule into ingress in practice (enforcement proves too weak)
— escalate to Option A (multi-module physical isolation).
