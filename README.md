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

## The dev team (pre-built agents)

The repo ships with a small dev team of Claude Code subagents under [`.claude/agents/`](./.claude/agents). They are deliberately generic — not tied to webhooks, PayHub, or this PRD — so you can lift them into your own projects after the workshop. Each one has an explicit *Human-in-the-Loop discipline* section that says when it must stop, what it must escalate, and what it must never decide alone.

| Agent | Role |
|---|---|
| [`product-owner`](./.claude/agents/product-owner.md) | Owns the *what* and *why*. PRD stewardship, user stories, acceptance criteria, prioritization. Refuses to silently resolve open product questions. |
| [`project-manager`](./.claude/agents/project-manager.md) | Owns *when*, *how much*, *who*. Scope, schedule, dependencies, risk. Pushes back on unrealistic timelines instead of absorbing them. |
| [`solution-architect`](./.claude/agents/solution-architect.md) | Owns system shape. Design proposals, ADRs, trade-off analysis. Honours documented architectural constraints absolutely. |
| [`backend-developer`](./.claude/agents/backend-developer.md) | Implements services and APIs. Modern Java 21 + Spring Boot 3 leaning, polyglot-aware. Reads docs before assuming the build system. |
| [`devops-engineer`](./.claude/agents/devops-engineer.md) | CI/CD, containers, GitOps, Kubernetes, IaC, observability, on-call. Treats reversibility as a first-class property. |
| [`qa-lead`](./.claude/agents/qa-lead.md) | Test strategy, risk-based planning, quality gates, exit criteria. Refuses to be a rubber stamp. |
| [`qa-engineer`](./.claude/agents/qa-engineer.md) | Hands-on test design and automation (UI, API, contract, performance, accessibility, resilience). Reproduces before reporting. |
| [`code-reviewer`](./.claude/agents/code-reviewer.md) | Pre-merge review for correctness, security, performance, observability, and documented-rule compliance. Severity-classified, evidence-based feedback. |

These agents do not magically build the system for you. They are scaffolding that makes the HITL beats visible — each one will *propose*, then *pause*, at the moments that matter. The workshop's `CLAUDE.md` is what gives them their marching orders for this specific project.

---

## The pipeline (PRD → epic → stories → implementation)

The repo ships a set of slash commands under [`.claude/commands/`](./.claude/commands) that
drive the work from PRD to running code — but with a **human-in-the-loop gate at every
transition**. They are the inverse of a "fire and forget" project-management bot: each
command delegates the thinking to a role-shaped agent, then **stops and asks the room**
whenever it reaches a decision the humans own, and records that decision as an ADR.

| Command | Owner | What it does | Where it stops |
|---|---|---|---|
| `/prd:review <prd>` | product-owner | Inventories silent assumptions, contradictions, open questions — answers none | Classify each: blocking vs deferrable |
| `/prd:to-epic <name>` | solution-architect | Architecture-bound epic, honouring the hot/warm/cold boundary | Every architecture checkpoint in `CLAUDE.md` |
| `/epic:decompose <name>` | product-owner | Small, independently-deliverable stories (no task tier); maps each to a PRD metric | Product/architecture questions + uncovered requirements |
| `/epic:estimate <name>` | project-manager | Estimate + honest pushback on the 3-week timeline; writes the critical path | Accept estimate, cut scope, or move date |
| `/story:start <id…>` | backend-developer | Opens one or more ready stories; surfaces questions only visible in code | Kickoff architecture/business questions |
| `/story:complete <id>` | qa-lead + code-reviewer | Closes a story: ACs met *with evidence*, review passed | Declare done, send back, or accept a gap |
| `/epic:done <name>` | qa-lead + code-reviewer | Verifies the PRD's **success metrics** are met before victory | Each metric: satisfied or out-of-scope-for-v0 |
| `/pm:status` · `/pm:next` | — | Read-only view of where everything sits, incl. forecast vs deadline | — |

### How a run flows

The commands run in order, each consuming the previous one's output. The flow is a
straight line with a **gate** (a human decision) on every arrow:

```
  PRD.md
    │
    ▼  /prd:review PRD.md
  work/prd/webhook.analysis.md ──┐ GATE: which open questions are blocking?
    │                            │       (product-owner surfaces, room classifies → ADR)
    ▼  /prd:to-epic webhook      │
  work/epics/webhook/epic.md ────┤ GATE: data store? out-of-order handling?     ◀─┐
    │                            │       (architect proposes options, room picks → ADR)│ LOOP 1
    ▼  /epic:decompose webhook   │                                                     │ decomposition
  work/epics/webhook/stories/ ───┤ GATE: identity mapping? business rules?       ◀────┘
    │  001-…, 002-…              │       (product-owner surfaces while splitting → ADR)
    ▼  /epic:estimate webhook    │
  work/epics/webhook/estimate.md ┘ GATE: 3 weeks is unrealistic — accept / cut / move?
    │                                    (project-manager pushes back, room decides → ADR)
    ▼  /story:start webhook/001 webhook/003   (multiple ready, parallel-safe stories at once)
  story → in-progress ─────────────┐ GATE: "this slice assumes X — true?"        ◀─┐ LOOP 2
    │  tasks discovered,           │        (developer surfaces in-code questions → ADR)│ implementation
    ▼  (hand off to backend-developer to build against the tasks + ADRs)            ───┘
    │
    ▼  /story:complete webhook/001   then   /epic:done webhook
  story/epic → complete             GATE: ACs met with evidence? PRD success metrics satisfied?
                                          (qa-lead + code-reviewer verify; room declares done → ADR)
```

A typical end-to-end run, step by step:

1. **`/prd:review PRD.md`** — the `product-owner` reads Maria's PRD and returns an
   inventory of silent assumptions, the internal contradiction, vague metrics, and open
   questions — **answering none of them**. The command writes
   `work/prd/webhook.analysis.md`, then **stops** and asks the room to mark each open
   question *blocking* or *deferrable*. The classification is recorded as an ADR.
2. **`/prd:to-epic webhook`** — the `solution-architect` drafts the epic, honouring the
   hot/warm/cold boundary in `CLAUDE.md` verbatim. Each time it reaches a decision the
   `CLAUDE.md` reserves for humans (the data store, out-of-order handling), the command
   **stops**, presents 2–3 options, waits for the room, and records an ADR before writing
   `epic.md`.
3. **`/epic:decompose webhook`** — the `product-owner` splits the epic into small,
   independently-deliverable stories (`work/epics/webhook/stories/001-…`). Questions that
   only surface while slicing (the PayHub→internal identity mapping, an ambiguous business
   rule) trigger the same **stop → options → decide → ADR** gate.
4. **`/epic:estimate webhook`** *(recommended)* — the `project-manager` estimates the
   stories and tests them against the PRD's 3-week timeline. If the gap is real it **stops**
   and asks the room to accept the estimate, cut scope, or move the date. It writes the
   critical path that `/pm:next` uses to sequence work and that `/pm:status` forecasts against.
5. **`/story:start webhook/001 …`** — the `backend-developer` reads each story *with the
   implementer's eye* (advisory only — it writes nothing), lists the tasks it now sees, and
   surfaces any architecture or business question invisible at decomposition. Those get the
   same gate (batched across the stories started), each story flips to `in-progress`, and
   the command **opens the gate but does not write code**. If an in-code decision changes
   scope, it flags the estimate stale so the forecast doesn't rot.
6. **`/story:complete webhook/001`** then **`/epic:done webhook`** — the `qa-lead` maps
   acceptance criteria (and the PRD's success metrics) to *evidence*, the `code-reviewer`
   confirms the hard rules held (hot path framework-free, no PII logged), and the room
   **declares done** — or sends it back. No agent self-certifies victory.

At any point, **`/pm:status`** shows where every artifact sits, the forecast vs the
deadline, stalled gates, and any stale estimate; **`/pm:next`** names the next command(s),
opening the whole ready-and-parallel set rather than one story at a time.

### Two loops, one protocol

There are **two HITL loops**, not one:

- **Decomposition-time** (`/prd:to-epic`, `/epic:decompose`) — questions that block *planning*.
- **Implementation-time** (`/story:start`) — questions that only become visible once an
  engineer opens the story. A decision recorded here is not a planning failure; some
  decisions genuinely cannot be made until the build begins.

Both loops obey one shared protocol — [`.claude/rules/hitl-checkpoint.md`](./.claude/rules/hitl-checkpoint.md):

> **Detect** the boundary → **Propose** 2–3 options → **ask the room and block** →
> **record an ADR** in [`work/decisions/`](./work).

The checkpoints are **not hardcoded**. Each command reads `CLAUDE.md`'s `HITL checkpoints`
and `Architecture constraints` and stops at *those* — so the same commands enforce
whatever boundaries your own `CLAUDE.md` declares. Drop these commands into another repo
with a different `CLAUDE.md` and they gate *that* project's decisions instead. All
artifacts land in [`work/`](./work) as plain markdown — no database, no GitHub, diffable
in a PR, take-home after the workshop.

> A story whose decision trail (`hitl:`) grows *after* `/story:start` is the visible proof
> of the second loop — a decision that did not exist at decomposition time.

### How the gate actually holds

A human gate is only real if it is *executed*, not narrated. So the pipeline is built so
the discipline survives a model under velocity pressure:

- **The command — not the agent — runs the gate.** Only the orchestrating command can call
  `AskUserQuestion` and write files. The role agents are **advisory**: they read, reason,
  and return their analysis as text. An agent cannot put a question to the room, and it
  cannot quietly write the artifact and skip the stop. Tool grants enforce this (e.g. the
  `solution-architect` is read-only).
- **A missing answer is a stop, not a default.** If the human doesn't answer, the command
  halts and writes nothing. There is no "I'll assume X for now".
- **Done is verified, never self-declared.** `/story:complete` and `/epic:done` require the
  `qa-lead` to map acceptance criteria and the PRD's success metrics to *evidence*, and the
  `code-reviewer` to confirm the hard rules held, before a human marks anything complete.

### Efficiency without removing the human

The gates protect *decisions*, not keystrokes — so the pipeline keeps the human in control
while cutting wasted cycles:

- **Batching** — when one step surfaces several independent checkpoints, they're presented
  as a *single* `AskUserQuestion`, not one blocking round-trip each.
- **Parallel kickoff** — `/story:start` opens the whole ready, parallel-safe set at once
  instead of serializing stories the decomposition already proved independent.
- **Forecast-aware** — `/pm:status` reads the estimate to show forecast-vs-deadline and the
  critical path; a loop-2 decision that changes scope flags the estimate stale so the plan
  never silently rots.

---

## Agenda (2 hours)

| Time | Block | What happens |
|---|---|---|
| 0:00–0:05 | Welcome | API keys handed out, setup check |
| 0:05–0:20 | PRD walkthrough | Read the PRD together. Surface 2 real issues live. |
| 0:20–0:55 | Build `CLAUDE.md` | Section by section, with the room. ~35 min. |
| 0:55–1:40 | Run the pipeline | `/prd:review` → `/prd:to-epic` → `/epic:decompose` → `/epic:estimate` → `/story:start`. Each command pauses at the gate it owns. See the flow diagram above. |
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

The `CLAUDE.md` you walk away with is yours — and so is the rest of `.claude/`: the
pipeline commands, the dev team, the HITL protocol, the templates. Drop the repo into
another project, point the pipeline at *its* `CLAUDE.md`, and the same gates fire on
*that* project's decisions. **The heading contract is the API.**

The same three-step recipe makes any `CLAUDE.md` work with the pipeline:

1. Decide what you want the agent to *assume* — under `## Architecture constraints`.
2. Decide what you want it to *ask* — under `## HITL checkpoints`.
3. Decide what you want it to *refuse* — out-of-scope and forbidden patterns.

The pipeline reads those headings literally. Change them and the gates change with them.

The artifact is small. The discipline is the point.

---

## Files in this repo

- [`PRD.md`](./PRD.md) — Maria's PRD. The brief.
- [`HOTPATH_README.md`](./HOTPATH_README.md) — the pure-Java hot-path receiver (prd/002): required env vars (`PAYHUB_DB_URL`, `PAYHUB_DB_USER`, `PAYHUB_DB_PASSWORD`), how to start Postgres, and the `mvn` build/run commands.
- [`CLAUDE.md.template`](./CLAUDE.md.template) — the scaffold we will fill in together.
- `CLAUDE.md` — your filled-in version, generated during the workshop.
- [`.claude/agents/`](./.claude/agents) — the pre-built dev team (Product Owner, Project Manager, Solution Architect, Backend Developer, DevOps Engineer, QA Lead, QA Engineer, Code Reviewer). Generic, reusable, HITL-disciplined.
- [`.claude/commands/`](./.claude/commands) — the pipeline commands (`/prd:*`, `/epic:*`, `/story:*`, `/pm:*`) that drive PRD → epic → stories with a human gate at each step.
- [`.claude/rules/hitl-checkpoint.md`](./.claude/rules/hitl-checkpoint.md) — the shared STOP protocol every command obeys.
- [`.claude/templates/`](./.claude/templates) — the ADR and story templates the commands write from.
- [`work/`](./work) — where the pipeline writes its output (analyses, epics, stories, decisions). Starts empty.
