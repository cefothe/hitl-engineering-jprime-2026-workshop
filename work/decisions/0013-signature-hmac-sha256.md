---
id: '0013'
title: Signature verification — HMAC-SHA256, JDK crypto only
status: accepted
created: 2026-06-02T20:06:22Z
loop: decomposition
triggered_by: /prd:to-epic (solution-architect CP-L, raised)
artifact: work/epics/prd/epic.md
checkpoint: raised — resolves silent assumption (verification mechanism, line 43)
---

# 0013 — Signature verification: HMAC-SHA256, JDK crypto only

## Context

PRD line 43 defers authenticity verification ("engineering will know what to do"). The
mechanism gates the hot-path ingest (S1) and must be settled. Resolves the architect's
raised checkpoint and PRD silent assumption 4.

## Options considered

- **Option A — HMAC-SHA256 over raw body** (shared secret), `javax.crypto` only. Standard
  PayHub-style scheme; must confirm PayHub uses it.
- **Option B — Asymmetric signature** (PayHub public key); no shared secret to leak; adds
  key-rotation handling.
- **Option C — Spike PayHub docs first**, then choose; avoids guessing the scheme.

## Decision

**Option A — HMAC-SHA256 over the raw request body**, shared secret, implemented with JDK
`javax.crypto` only.

## Implications

- **Hard-rule (CLAUDE.md hot-path framework ban):** verification uses `javax.crypto` /
  `java.security` ONLY. `spring-security-crypto` and any Spring/DI helper are **BANNED** on
  the hot path (architect tension T2). The code-reviewer must guard this on every S1 PR.
- The shared secret is supplied via env var, never committed (CLAUDE.md secrets rule).
- Still needs confirmation that PayHub actually uses HMAC-SHA256 (fold into the ADR-0004
  spike); reject with 4xx on signature mismatch before any durable write.

## Revisit if

PayHub's docs reveal a different scheme (e.g. asymmetric) — then supersede with Option B,
still JDK-crypto-only on the hot path.
