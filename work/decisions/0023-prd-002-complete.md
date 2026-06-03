---
id: '0023'
title: prd/002 complete — durable ingest + ACK, gaps closed
status: accepted
created: 2026-06-03T03:18:18Z
loop: implementation
triggered_by: /story:complete (qa-lead + code-reviewer)
artifact: work/epics/prd/stories/002-durable-ingest-ack.md
checkpoint: declared — CLAUDE.md "Claiming the work is done" (per-story half)
---

# 0023 — prd/002 complete: durable ingest + ACK, gaps closed

## Context

`/story:complete prd/002` ran the verification gate: `qa-lead` mapped each AC to evidence,
`code-reviewer` did a pre-merge review. The reviewer found **no Critical/Major**; the
qa-lead confirmed 4/5 ACs well-covered and AC5 over-satisfied, but flagged real gaps. The
room chose **"send back — close gaps first"** rather than completing with the gaps open.

## Options considered

- **Send back — close gaps first (chosen):** add the missing end-to-end 503 test and the
  README env-var docs before declaring done; tightens AC3 evidence and meets the CLAUDE.md
  secrets-documentation rule.
- **Complete, accept gaps as v0:** mark done now, defer the 503 test + README docs.
- **Complete + fast-follow:** mark done, track the gaps as a follow-up.

## Decision

**Sent back, gaps closed, then completed.** Remediation (this build beat):
- **End-to-end 503 test** added — `WriteFailure503IT.writeFailureSurfacesAs503AndCommitsNothing`:
  drops the table on a live Níma+Postgres stack to inject a real `SQLException`, POSTs, asserts
  HTTP **503** and **zero rows**. Verified non-vacuous (flipping the expectation to 200 fails).
- **README env-var docs** added — `HOTPATH_README.md` documents `PAYHUB_DB_URL` /
  `PAYHUB_DB_USER` / `PAYHUB_DB_PASSWORD` (exact names from `IngressConfig`), run/build steps,
  no secret values committed; `README.md` points to it.

**Evidence (independently re-run):** `mvn test` → 11/0; `WriteFailure503IT` → 1/0 against real
Testcontainers Postgres; `grep org.springframework|jakarta.persistence|spring-security-crypto
src/main` → nothing; runtime dependency tree → no framework on any classpath; ArchUnit ban
test enforces it in CI. AC1–AC5 satisfied with evidence.

prd/002 → **`status: complete`**.

## Accepted residual gaps (recorded, not absorbed silently)

- **Event-ID extraction is an assumption pending the prd/001 spike.** Header `PayHub-Event-Id`
  with a top-level JSON `id` fallback, isolated in `EventIdExtractor` and documented in code.
  Accepted as a known, reversible risk until prd/001 (PayHub sandbox/docs) confirms the real
  envelope. If it differs, the extractor + `event_id` UNIQUE semantics change.
- **AC4 is a faithful pool-restart analogue, not a literal `kill -9` of a separate JVM.** It
  closes the entire HikariCP pool (discarding in-memory state) and reopens against the same
  Postgres — proving the property ADR 0011 guarantees (only committed data survives a fresh
  process). Accepted.
- **Doc drift:** ADR 0019 / band-check narrative said "Spring remains on the build classpath";
  the delivered pom has no Spring on any scope, so the ArchUnit rule currently passes
  *vacuously* (a guard against a future Spring dependency). Safe direction; noted for a later
  one-line doc correction.

## Implications

- Epic `progress` recomputed: 1 of 13 stories complete.
- prd/002's `hitl:` gains `0023` (now spanning decomposition + two implementation-time loops).
- prd/008 and prd/010 (depended only on prd/002) are now unblocked and ready to start.

## Revisit if

The prd/001 spike contradicts the event-ID extraction assumption (then reopen the extractor),
or a future change adds Spring under `ingress` (the ArchUnit gate then fires non-vacuously).
