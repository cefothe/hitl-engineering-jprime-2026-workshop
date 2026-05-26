# HITL Engineering — jPrime 2026 Workshop

Workshop materials for **Human-in-the-Loop Engineering: Designing Systems Where Agents Propose, Humans Approve** — jPrime 2026, Sofia, 3 June 2026.

Hosted by [Stefan Angelov](https://github.com/cefothe) and [Lyubomir Bozhinov](https://github.com/lyubomir-bozhinov).

---

## What this workshop is

You will not be lectured at for two hours. You will sit with us — or follow along on your laptop — while an AI coding agent builds a real, production-shaped service from a real, deliberately realistic PRD. Along the way we will stop the agent at every moment that matters and decide, as a room, what it should do next.

By the end you will have:

- **A filled-in `CLAUDE.md`** for a Java webhook-receiver project — your take-home artifact
- **A partially-built but running receiver** that demonstrates the architecture
- **A working mental model** for designing HITL checkpoints in your own AI-driven projects

What you will not have: a fully production-ready system. We are short of time. The point is the *process* and the *artifacts*, not the finished product.

---

## The scenario

Maria K., the Payments PM at a fictional company, has written [a PRD](./PRD.md) asking engineering to fix a broken webhook integration with their payment provider PayHub. The PRD is well-intentioned and mostly good — but it has the same flaws every real PRD has: silent assumptions, internal contradictions, deferred decisions, optimistic timelines.

We will hand the PRD to Claude Code, watch what it does, and intervene when human judgment is required.

---

## Architecture (pre-decided)

This is one of the workshop's central teaching moments. Read it now.

| Path | Stack | Why |
|---|---|---|
| **Hot** — webhook ingress | Pure Java 21, no framework (JDK `HttpServer` or Helidon Níma) | Sub-5s ACK, sustained throughput, no per-request framework cost |
| **Warm** — processing, retries, persistence | Spring Boot 3.x | Developer velocity dominates; latency tolerance is high |
| **Cold** — admin, reconciliation, DLQ review | Spring Boot 3.x | Same reasoning |

This split is deliberate. Left alone, Claude Code would default to Spring Boot for everything — it is the Java default. The first thing the `CLAUDE.md` does is forbid that default on the hot path. That moment is the workshop in miniature: *the human's domain knowledge overrides the agent's pattern-match*.

---

## Agenda (2 hours)

| Time | Block | What happens |
|---|---|---|
| 0:00–0:05 | Welcome | API keys handed out, setup check |
| 0:05–0:20 | PRD walkthrough | Read the PRD together. Surface 2 real issues live. |
| 0:20–0:55 | Build `CLAUDE.md` | Section by section, with the room. ~35 min. |
| 0:55–1:40 | Drive Claude Code | Build the receiver. HITL beats fire naturally. |
| 1:40–1:55 | Wrap-up | Take-home, principles, Q&A |
| 1:55–2:00 | Buffer | |

We will not finish the implementation. We will finish the `CLAUDE.md`. That is the priority.

---

## What you need (only if you want to follow along)

- **Java 21+** installed (`java --version` shows 21+)
- **Maven 3.9+** or Gradle
- **Claude Code** (Anthropic's CLI for Claude) installed and signed in
- **An Anthropic API key** — we will distribute one for the workshop in the room
- **This repo cloned** locally

You can also just watch us drive. Repo will stay public; you can replay everything after.

---

## What to expect (HITL beats, without spoilers)

During the workshop, the agent will hit moments where it must stop and ask. You will be involved in deciding:

- Where to store events
- How to map identities between PayHub and the local system
- What to do about an internal contradiction in the PRD
- How to handle a major architectural decision the PRD does not even mention
- Whether to accept the agent's effort estimate or push back

These are not staged. They emerge from the PRD itself.

---

## After the workshop

The `CLAUDE.md` you walk away with is yours. The same shape works for your projects:

1. Decide what you want the agent to *assume* (architecture, conventions, forbidden patterns)
2. Decide what you want it to *ask* (the HITL checkpoints)
3. Decide what you want it to *refuse* (the boundaries)

The artifact is small. The discipline is the point.

---

## Files in this repo

- [`PRD.md`](./PRD.md) — Maria's PRD. The brief.
- [`CLAUDE.md.template`](./CLAUDE.md.template) — the scaffold we will fill in together.
- `CLAUDE.md` — your filled-in version, generated during the workshop.
