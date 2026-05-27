---
marp: true
paginate: true
size: 16:9
footer: 'HITL Engineering · jPrime 2026 · Sofia · 3 June 2026'
style: |
  /* ============================================================
     jPrime 2026 — Human-in-the-Loop Engineering
     Marp inline theme (no @theme, no @import, no Gaia)
     Page size: 1280x720 (Marp default)
     ============================================================ */

  section {
    width: 1280px;
    height: 720px;
    padding: 70px 90px 90px 90px;
    background: #FAF8F5;
    color: #1C1917;
    font-family: "Inter", system-ui, -apple-system, "Segoe UI", Roboto, sans-serif;
    font-size: 26px;
    font-weight: 400;
    line-height: 1.5;
    letter-spacing: -0.005em;
    position: relative;
    box-sizing: border-box;
    display: flex;
    flex-direction: column;
    justify-content: flex-start;
  }

  h1, h2, h3, h4, h5, h6 {
    font-family: "Inter", system-ui, -apple-system, "Segoe UI", Roboto, sans-serif;
    color: #1C1917;
    font-weight: 700;
    letter-spacing: -0.02em;
    line-height: 1.15;
    margin: 0 0 0.5em 0;
  }

  h1 { font-size: 56px; font-weight: 800; }
  h2 {
    font-size: 40px;
    margin-bottom: 0.6em;
    padding-bottom: 0.35em;
    border-bottom: 1px solid #E5DDD0;
  }
  h3 { font-size: 28px; font-weight: 700; color: #1C1917; margin-top: 0.2em; }
  h4 { font-size: 22px; font-weight: 600; color: #5C544A; }

  p, ul, ol { margin: 0 0 0.7em 0; }
  ul, ol { padding-left: 1.3em; }
  li { margin-bottom: 0.35em; }
  li::marker { color: #B8593A; }

  strong, b { color: #1C1917; font-weight: 700; }
  em, i { color: #1C1917; }

  a { color: #B8593A; text-decoration: none; border-bottom: 1px solid #B8593A40; }

  hr { border: 0; border-top: 1px solid #E5DDD0; margin: 1em 0; }

  /* Inline code & code blocks */
  code {
    font-family: "JetBrains Mono", "Menlo", "Monaco", "Consolas", monospace;
    font-size: 0.85em;
    color: #1C1917;
    background: #EDE8E0;
    padding: 0.1em 0.4em;
    border-radius: 4px;
    border: 1px solid #DDD4C3;
    font-weight: 500;
  }

  pre {
    background: #2A2724;
    color: #F5F0E8;
    padding: 22px 26px;
    border-radius: 8px;
    border-left: 4px solid #B8593A;
    font-size: 20px;
    line-height: 1.5;
    overflow: auto;
    margin: 0.6em 0;
  }

  pre code {
    background: transparent;
    color: #F5F0E8;
    border: none;
    padding: 0;
    font-size: inherit;
    font-weight: 400;
  }

  /* Blockquote — no floating quote glyph */
  blockquote {
    margin: 0.8em 0;
    padding: 0.3em 0 0.3em 1.1em;
    border-left: 4px solid #B8593A;
    color: #3C3530;
    font-style: italic;
    font-size: 1.05em;
    background: transparent;
  }
  blockquote::before,
  blockquote::after,
  blockquote p::before,
  blockquote p::after {
    content: none !important;
    display: none !important;
  }
  blockquote p { margin: 0.2em 0; }

  /* Tables */
  table {
    border-collapse: collapse;
    width: 100%;
    margin: 0.6em 0;
    font-size: 0.92em;
  }
  table th {
    background: #EDE8E0;
    color: #1C1917;
    font-weight: 600;
    text-align: left;
    padding: 10px 14px;
    border-bottom: 2px solid #C9BFAC;
  }
  table td {
    padding: 10px 14px;
    border-bottom: 1px solid #E5DDD0;
    color: #1C1917;
    background: transparent;
  }
  table tr:last-child td { border-bottom: none; }

  /* Footer pinned */
  footer {
    position: absolute;
    bottom: 28px;
    left: 90px;
    right: 90px;
    font-size: 0.6em;
    color: #8A8074;
    font-weight: 400;
    letter-spacing: 0.02em;
    border-top: 1px solid #E5DDD0;
    padding-top: 10px;
    font-style: normal;
  }

  header { display: none; }

  /* Pagination */
  section::after {
    font-family: "Inter", system-ui, sans-serif;
    font-size: 14px;
    color: #8A8074;
    bottom: 30px;
    right: 90px;
    font-weight: 400;
  }

  /* .title — opening slide / chapter dividers */
  section.title {
    justify-content: center;
    align-items: center;
    text-align: center;
    padding: 90px 120px;
    background: #FAF8F5;
  }
  section.title h1 {
    font-size: 64px;
    font-weight: 800;
    letter-spacing: -0.03em;
    margin-bottom: 0.3em;
    max-width: 900px;
    line-height: 1.1;
  }
  section.title h2 {
    font-size: 28px;
    font-weight: 500;
    color: #5C544A;
    border: none;
    padding: 0;
    margin: 0.2em 0 1.2em 0;
    letter-spacing: -0.01em;
  }
  section.title h3 {
    font-size: 22px;
    font-weight: 500;
    color: #B8593A;
    margin-top: 0.8em;
  }
  section.title p {
    font-size: 22px;
    color: #5C544A;
    max-width: 800px;
  }
  section.title hr {
    width: 80px;
    border-top: 3px solid #B8593A;
    margin: 1em auto;
  }
  section.title footer,
  section.title::after,
  section.title header { display: none; }

  /* .split — two columns, H2 spans both */
  section.split {
    display: grid;
    grid-template-columns: 1fr 1fr;
    grid-auto-rows: min-content;
    column-gap: 60px;
    row-gap: 0;
    align-content: start;
    padding: 70px 90px 90px 90px;
  }
  section.split > h2 {
    grid-column: 1 / -1;
    margin-bottom: 0.4em;
  }
  section.split > .col { min-width: 0; }
  section.split > .col > h3 {
    font-size: 24px;
    color: #B8593A;
    margin: 0 0 0.5em 0;
    font-weight: 700;
    letter-spacing: -0.01em;
  }
  section.split > .col > h3:not(:first-child) { margin-top: 0.8em; }
  section.split > .col > ul,
  section.split > .col > ol { padding-left: 1.2em; }
  section.split > .col > p,
  section.split > .col > ul,
  section.split > .col > ol {
    font-size: 22px;
    line-height: 1.5;
  }
  section.split > .col small {
    display: block;
    margin-top: 0.8em;
    font-size: 14px;
    color: #8A8074;
    font-family: "JetBrains Mono", "Menlo", monospace;
  }
  section.split > p,
  section.split > ul,
  section.split > ol { font-size: 22px; }

  /* .roster — 2-col grid of role cards */
  section.roster {
    display: grid;
    grid-template-columns: 1fr 1fr;
    grid-auto-rows: min-content;
    column-gap: 28px;
    row-gap: 18px;
    align-content: start;
    padding: 60px 90px 90px 90px;
  }
  section.roster > h2 {
    grid-column: 1 / -1;
    margin-bottom: 0.2em;
  }
  section.roster > .role {
    background: #F2ECE0;
    border-left: 4px solid #B8593A;
    padding: 14px 20px;
    border-radius: 0 6px 6px 0;
    font-size: 19px;
    line-height: 1.4;
    color: #1C1917;
  }
  section.roster > .role b,
  section.roster > .role strong {
    display: block;
    font-size: 20px;
    font-weight: 700;
    color: #1C1917;
    margin-bottom: 2px;
    letter-spacing: -0.01em;
    font-family: "JetBrains Mono", "Menlo", monospace;
  }
  section.roster > .role p {
    margin: 0;
    color: #3C3530;
    font-size: 18px;
  }

  /* .callout — single big statement */
  section.callout {
    justify-content: center;
    align-items: center;
    text-align: center;
    padding: 100px 140px;
  }
  section.callout blockquote,
  section.callout p {
    font-size: 42px;
    font-weight: 600;
    line-height: 1.25;
    letter-spacing: -0.02em;
    color: #1C1917;
    font-style: normal;
    border: none;
    padding: 0;
    margin: 0;
    max-width: 980px;
  }
  section.callout blockquote::before,
  section.callout blockquote::after { content: none !important; }
  section.callout > hr {
    width: 60px;
    border-top: 3px solid #B8593A;
    margin: 0.6em auto 0.8em auto;
  }
  section.callout cite,
  section.callout em {
    display: block;
    margin-top: 0.8em;
    font-size: 22px;
    font-weight: 500;
    font-style: normal;
    color: #5C544A;
    letter-spacing: 0.02em;
  }

  img { max-width: 100%; border-radius: 6px; }

  /* small footnotes inside default sections */
  small {
    font-size: 14px;
    color: #8A8074;
    font-family: "JetBrains Mono", "Menlo", monospace;
  }
---

<!-- _class: title -->

# Human-in-the-Loop Engineering
## Designing Systems Where Agents Propose, Humans Approve
### 3 June 2026 · Sofia · 2-hour workshop
Stefan Angelov · Lyubomir Bozhinov

---

<!-- _class: split -->

## Your guides today

<div class="col">

### Stefan Angelov
Architect Lead · Engineering Manager
*"The Java guy from pLoVEdiv"*
@cefothe

</div>

<div class="col">

### Lyubomir Bozhinov
Technology Leader
*Bridging Strategy, Innovation & Scalable Execution*
@lyubomir-bozhinov

</div>

---

## What this workshop is

This is not a talk you sit and watch. You sit **with us** while an AI coding agent builds a real service from a real PRD — and we stop the agent at every moment that matters.

Every stop is a decision we make as a room. Where should events go? What do we do about this contradiction? Should we accept this estimate? We don't move on until we've decided together.

The PRD is deliberately flawed — the same flaws you see every week. The agent is genuinely capable. The question is: **capable of what, decided by whom?**

---

## What you walk away with

- A filled-in `CLAUDE.md` for a Java webhook-receiver project — yours to keep and adapt
- A partially-built, running receiver that demonstrates the architecture in action
- A working mental model for where HITL checkpoints belong in your own projects

> Not a finished product. The point is the *process*.

---

## The scenario

Maria K. is a Payments PM at a fictional company. She wrote a PRD asking engineering to fix a broken webhook integration with payment provider **PayHub**.

The PRD has every real flaw: silent assumptions, internal contradictions, deferred decisions, an optimistic timeline. It is a normal PRD.

We hand it to Claude Code and step in when human judgment is required — not to fix bad writing, but because some decisions belong to the room, not the agent.

---

## Architecture — pre-decided

| Path | Stack | Reason |
|---|---|---|
| **Hot path** | Pure Java 21, no framework | Sub-5s ACK, sustained throughput |
| **Warm path** | Spring Boot 3.x | Processing, retries, persistence — dev velocity dominates |
| **Cold path** | Spring Boot 3.x | Admin, reconciliation, DLQ |

Left alone, Claude Code would default to Spring Boot everywhere. The first thing `CLAUDE.md` does is forbid that default on the hot path.

---

## Agenda — 2 hours

| Time | Block |
|---|---|
| 0:00 – 0:05 | Welcome |
| 0:05 – 0:20 | PRD walkthrough |
| 0:20 – 0:55 | Build `CLAUDE.md` |
| 0:55 – 1:40 | Drive Claude Code |
| 1:40 – 1:55 | Wrap-up |
| 1:55 – 2:00 | Buffer |

We will **NOT** finish the implementation. We **WILL** finish the `CLAUDE.md`. That is the priority.

---

## HITL beats — what we decide together

- Where to **store events**
- How to **map identities** between PayHub and the local system
- What to do about an **internal contradiction** in the PRD
- A **major architectural decision** the PRD doesn't mention
- Whether to accept the agent's **effort estimate**

> Not staged. They emerge from the PRD.

---

<!-- _class: title -->

# The Cast
## 8 specialized agents
### Each refusing to silently decide

---

## Why a cast, not a single agent?

A general-purpose agent silently makes decisions that should never be made silently: which database, what SLA, whether the feature is ready to ship, what counts as tested. Those decisions belong to humans — or at minimum, in front of humans before anything is committed.

Splitting work across role-shaped agents doesn't make the system safer by making it slower. It makes it more honest by giving each agent explicit boundaries, escalation rules, and a documented refusal to override the humans who own the decision.

> The agent doesn't get less capable. The **system** gets more honest.

---

<!-- _class: roster -->

## The roster

<div class="role"><b>product-owner</b><p>Stewards the PRD. Surfaces silent assumptions. Won't let engineering quietly resolve open product questions.</p></div>

<div class="role"><b>project-manager</b><p>Owns scope, schedule, risk. Pushes back on unrealistic timelines. Runs HITL checkpoints.</p></div>

<div class="role"><b>solution-architect</b><p>Architecture, ADRs, trade-offs. Refuses to make architecturally significant decisions silently.</p></div>

<div class="role"><b>backend-developer</b><p>JVM-first implementation. Java 21, Spring Boot 3.x, virtual threads, persistence, observability.</p></div>

<div class="role"><b>qa-lead</b><p>Test strategy, quality gates, release criteria. Won't declare quality "verified" without evidence.</p></div>

<div class="role"><b>qa-engineer</b><p>Hands-on test design and automation across functional, integration, perf, security, accessibility.</p></div>

<div class="role"><b>code-reviewer</b><p>Pre-merge correctness, security, OWASP, rule enforcement. Last reasonable check before merge.</p></div>

<div class="role"><b>devops-engineer</b><p>CI/CD, GitOps, Kubernetes, IaC, observability. Owns the production runtime story.</p></div>

---

## What they all share

- **Documented refusal points** — things each agent won't decide alone
- **Explicit escalation paths** — to the architect, the PO, the room
- **Project rules are binding** — `CLAUDE.md` wins; agents flag conflicts, never override
- **Severity discipline** — Critical · Major · Minor · Suggestion, never conflated
- **Evidence over taste** — every finding cites file, line, rationale

> How HITL discipline survives contact with velocity pressure.

---

<!-- _class: title -->

# The Toolbelt
## Making the agent context-efficient, deterministic, and persistent

---

<!-- _class: split -->

## Deterministic repo context

<div class="col">

### `cxpak`
*Spends CPU cycles so you don't spend tokens.* The LLM gets a briefing packet instead of a flashlight in a dark room.

→ Deterministic, reviewable repo context.

<small>github.com/Barnett-Studios/cxpak</small>

</div>

<div class="col">

### `codeindex`
Local-first codebase index engine. Builds a semantic index on your machine — no code leaves the box.

→ Retrieval layer for grounded answers on large repos.

<small>github.com/askcodebase/codeindex</small>

</div>

---

## Token optimization — `rtk`

**Rust Token Killer.** A single Rust binary installed as a Claude Code `PreToolUse` hook.

Transparently compresses noisy Bash output **before** it reaches the model:
`cargo test` · `git status` · `find` · `grep` · `npm` · `docker` · `kubectl`

> `cargo test` with 262 passing tests: **4,823 → 11 tokens.**
> Average savings across 2,900+ commands: **~89%.**

**Why HITL cares:** longer sessions, cheaper iterations, less context pollution between human review cycles.

<small>github.com/rtk-ai/rtk</small>

---

<!-- _class: split -->

## Session memory — two takes

<div class="col">

### `claude-mem` — auto-capture
Records the session, compresses with Agent SDK, auto-injects context on restart. Works across Claude Code, Codex, Gemini CLI, Windsurf, OpenCode, Copilot.

→ *"I don't want to re-prime every morning."*

<small>github.com/thedotmack/claude-mem</small>

</div>

<div class="col">

### `memory-palace` — deliberate, MCP-based
Explicit `remember` · `recall` · `forget` tools over a knowledge graph with semantic search.

→ *"The human chooses what the agent knows long-term."*

<small>github.com/jeffpierce/memory-palace</small>

</div>

---

## Choosing memory strategy is itself HITL

| You want… | Pick |
|---|---|
| Frictionless continuity | `claude-mem` — auto |
| Auditable, curated knowledge | `memory-palace` — manual + MCP |
| Neither — every session clean | The `CLAUDE.md` itself |

> The discipline is not picking the fanciest tool. It is being explicit about what the agent assumes between sessions.

---

## What you need to follow along

- Java 21+ installed (`java --version` shows 21+)
- Maven 3.9+ or Gradle
- **Claude Code** (Anthropic's CLI) installed and signed in
- An Anthropic API key — we distribute one in the room
- This repo cloned locally

Or just watch. Repo is public — replay everything after.

<small>github.com/cefothe/hitl-engineering-jprime-2026-workshop</small>

---

## After the workshop

The `CLAUDE.md` you walk away with is **yours**. The same shape works for your projects:

1. Decide what the agent should **assume** — architecture, conventions, forbidden patterns
2. Decide what it should **ask** — the HITL checkpoints
3. Decide what it should **refuse** — the boundaries

> The artifact is small. The discipline is the point.

---

<!-- _class: title -->

# Let's begin.
## Open the PRD. Meet Maria.
### Questions during, not just at the end. Interrupt us.

#jprime2026 · @cefothe · @lyubomir-bozhinov
