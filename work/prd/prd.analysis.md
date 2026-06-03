---
name: prd
source: PRD.md
created: 2026-06-02T19:47:01Z
status: reviewed
hitl: ['0001']
---

# PRD Review — Reliable Webhook Processing for Payment Events

Surfaced by the `product-owner` agent via `/prd:review`. **Nothing here is resolved.**
Every open question is owned by a human and classified at the `/prd:review` STOP gate
(below). Resolution happens later, via ADRs, in `/prd:to-epic` and beyond.

## Silent assumptions

1. **"Exactly once" is achievable as stated** (PRD lines 23, 27–28). End-to-end
   exactly-once across a network boundary is generally not achievable; the usual build is
   at-least-once + idempotent application. The PRD does not acknowledge the distinction.
2. **A PayHub `customer_id` → internal `user_id` mapping already exists** (line 45). The
   PRD never states how PayHub identifies a customer, whether a mapping exists, or what
   happens to unmatched events.
3. **PayHub provides a stable, unique event ID** (lines 29, 47, 53) that survives retries
   unchanged — assumed but never confirmed.
4. **Signature/authenticity verification is known and sufficient** (line 43, "engineering
   will know what to do") — deferred without confirming the mechanism exists / suffices.
5. **The ~5,000-events-in-5-minutes burst is the worst case** (line 30) — an unvalidated
   estimate with no stated source or headroom.
6. **The existing data mess is separable from v1** (line 37) — assumed it won't interact
   with the new idempotency/state model.
7. **The €2,400/month discrepancy is caused by webhook loss/duplication** (line 15) — and
   therefore fixing webhooks fixes reconciliation. Causation implied, never established.
8. **A "set aside for review" (DLQ) location is acceptable to its eventual users**
   (line 31, 46) — owner, monitoring, and drain SLA all unstated.

## Internal contradictions

1. **Replay scope.** Line 37 (non-goal): historical/pre-launch replay is out, a one-off
   script. Line 57 (user story): on-call engineer should "replay or skip individual
   events without writing a one-off script." The in/out boundary is undefined.
2. **"No event ever lost" vs the 99.9% / DLQ model.** Line 27 states an absolute; line 62
   ("99.9% within 5 min") and line 31 (DLQ) describe a system where some events are
   delayed/diverted. Whether a DLQ'd event counts as "not lost" is left implicit.
3. **"Acknowledge quickly" vs processing latency.** Line 44 (5s ACK) and line 62 (5min
   full processing) imply an async architecture and durability between ACK and processing
   that the functional requirements never state.

## Vague success criteria

- "within a few minutes" (line 51) — undefined; differs from line 62's 5 minutes.
- "quickly" (line 44) — no target ACK time stated beyond the 5s retry threshold.
- "a while" (line 68, retention) — explicit PM placeholder; no measurable period.
- "eventually" (line 45) — no bound; distinct from the 5-minute metric.
- "survive a backlog … without taking down the rest of the app" (line 30) — "survive" and
  the during-burst behaviour are not measured.
- Current-state baseline ("some", "occasionally", "completely") is anecdotal, weakening
  the before/after metrics.

## Open questions

> `[ ]` open · `[x]` resolved. `class` and `resolved_by` are filled by the STOP gate and
> by downstream ADRs respectively.

- [x] **Q1** How long must raw event payloads be retained? (line 68; PM defers to compliance) — class: blocking — owner: product-owner (+compliance) — resolved_by: 0007
- [x] **Q2** On-call paging on repeated failure, or is a daily digest enough for v1? (line 69) — class: blocking — owner: product-owner (+project-manager) — resolved_by: 0008
- [x] **Q3** Do out-of-order events need handling, and if so how? (line 70) — class: blocking — owner: product-owner (+solution-architect) — resolved_by: 0006
- [x] **Q4** True exactly-once, or at-least-once + idempotent application? (assumption 1) — class: blocking — owner: solution-architect (+product-owner) — resolved_by: 0003
- [x] **Q5** Does a `customer_id`→`user_id` mapping exist, how is it populated, and what happens to orphans? (assumption 2) — class: blocking — owner: product-owner (+solution-architect) — resolved_by: 0005
- [x] **Q6** Does PayHub send a stable, unique event ID surviving retries? (assumption 3) — class: blocking — owner: solution-architect — resolved_by: 0004
- [x] **Q7** What is the data store? (not in PRD; durability depends on it) — class: blocking — owner: solution-architect — resolved_by: 0002
- [x] **Q8** How is the DLQ monitored, by whom, with what drain SLA? (Goal 5) — class: blocking — owner: product-owner (+project-manager) — resolved_by: 0009
- [x] **Q9** What is in scope for "replay or skip individual events", vs the historical-replay non-goal? (contradiction 1) — class: blocking — owner: product-owner — resolved_by: 0010
- [x] **Q10** Does "no event ever lost" treat a DLQ'd event as not-lost, and does the 0.1% count against Goal 1? (contradiction 2) — class: blocking — owner: product-owner — resolved_by: 0011
- [ ] **Q11** Is the €2,400/month discrepancy fully attributable to webhooks, making €50/month achievable by this work alone? (assumption 7) — class: deferrable — owner: product-owner (+finance) — resolved_by: —
- [x] **Q12** What is the required behaviour and acceptable processing delay *during* the burst? (vague criterion) — class: blocking — owner: product-owner (+solution-architect) — resolved_by: 0012
- [x] **Q13** Is the 3-week timeline compatible with Q1/Q5/Q7 still unresolved? (line 74) — class: blocking — owner: project-manager — resolved_by: 0018 (room accepted ~4 weeks, full scope)
