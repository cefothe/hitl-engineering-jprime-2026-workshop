---
id: 'prd/002'
title: Durably ingest a webhook POST and ACK (hot path)
status: complete
created: 2026-06-02T20:14:19Z
updated: 2026-06-03T03:18:18Z
epic: prd
path: hot
depends_on: []
parallel: true
metrics: ['goal-1', 'metric-99.9-5min']
hitl: ['0002', '0011', '0019', '0020', '0021', '0023']
---

# 002 — Durably ingest a webhook POST and ACK (hot path)

## User story

**As a** PayHub integration
**I want** my `POST` durably stored and acknowledged within the retry window
**So that** no event is lost even if our service restarts the instant after I send it.

## Acceptance criteria

1. `derived` (line 42) — the pure-Java handler (JDK `HttpServer`/Níma, **no Spring**) accepts the request body.
2. `derived` (Goal 1, line 27; ADR 0011) — the raw payload + PayHub event ID + received-at are written to Postgres via **plain JDBC** and the transaction commits before any ACK.
3. `derived` (line 44, Goal 1) — on committed write the handler returns 2xx; on write failure it returns non-2xx so PayHub retries.
4. `derived` (Goal 1, line 27) — an event committed then service-killed-and-restarted is still present (durability survives restart).
5. `assumed` (CLAUDE.md hot-path ban) — the hot-path module links no Spring/DI/AOP dependency; `spring-security-crypto` absent. Enforced by code-reviewer per story.

## Non-goals for this story

No signature verification (S003), no dedup (S004), no processing (S005).

## Open questions

None blocking. Framework-ban enforcement confirmed as a per-story code-reviewer gate.

## Dependencies

S001 informs (event-ID/timestamp) but does not block raw ingest. Postgres available (ADR 0002).

## Tasks (discovered at start)

Surfaced by `backend-developer` at `/story:start`. Loop-2 decisions: build structure =
single module + ArchUnit (ADR 0019); server = Helidon Níma (ADR 0020); pooling = HikariCP
standalone (ADR 0021).

1. Scaffold a single Maven module; add an **ArchUnit rule** banning `org.springframework.*`
   and `jakarta.persistence.*` imports under the `ingress` package, wired into CI (ADR 0019,
   realises AC5).
2. Stand up **Helidon Níma** with one `POST` route; read the request body as **raw bytes**
   (preserve verbatim for S003 HMAC-over-raw-body). Vet Níma's transitive graph is
   Spring-free; add to the ArchUnit allowlist (ADR 0020).
3. Hand-wire a **HikariCP** `DataSource` in `main` (no `@Bean`/Spring lifecycle); open at
   startup, close on shutdown; verify HikariCP pulls nothing Spring transitively (ADR 0021).
4. Define the durable-write table (raw payload, PayHub event ID, received-at, a
   `processed`/claim column for the warm path) as a **`.sql` source of truth** — the
   cross-framework contract (ADR 0002 / T1); no shared `@Entity`/repository code. Include a
   unique constraint on event ID (column needed now though dedup is prd/004).
5. Implement commit-before-ACK: `setAutoCommit(false)` → INSERT → `commit()` → **then** 2xx;
   any `SQLException` at/before commit → non-2xx so PayHub retries (ADR 0011, AC2/AC3).
6. Restart-durability integration test (AC4): POST → 2xx → hard-kill JVM → restart → assert
   row present. Use Testcontainers Postgres at **test scope only** — must not leak onto the
   ingress runtime classpath.
7. Config/secrets via `System.getenv()` (DB URL/user/password; future HMAC secret) — env
   vars only, documented in README (CLAUDE.md secrets rule).

**Open (carried / deferred):** exact success status (200/201/202) and write-failure status
(500/503); request body size cap + 413; how the event ID is extracted for the column
(header vs minimal parse) pending the prd/001 spike; schema migration ownership (warm-path
Flyway vs neutral step).

### Band check (path: hot) — PASS with enforcement note

No discovered task introduces a forbidden framework token (`@Component`/`@Autowired`/
`@RestController`/`@Service`/`@Bean`/Spring DI/`spring-security-crypto`). Helidon Níma and
HikariCP are plain libraries, not DI frameworks. **Caveat:** ADR 0019 chose single-module,
so Spring remains on the *build* classpath — the ban is enforced by the ArchUnit rule (task 1)
+ code-reviewer, not by physical module isolation. The ArchUnit gate is therefore
load-bearing and must be in CI before merge.
