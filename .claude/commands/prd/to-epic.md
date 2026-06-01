---
allowed-tools: Bash, Read, Write, LS, Task, AskUserQuestion
---

# PRD → Epic

Turn a reviewed PRD into a technical implementation epic — honouring the architecture
constraints in `CLAUDE.md` absolutely, and **stopping at every decision the room owns**.
The first generative beat, and the first of the two HITL loops (decomposition).

## Usage
```
/prd:to-epic <name>             # name of the PRD, e.g. /prd:to-epic webhook
```

## Required Rules
**Before executing, read and follow:**
- `.claude/rules/hitl-checkpoint.md` — the STOP protocol; **this command (not the agent)
  owns the `AskUserQuestion` BLOCK, the ADR allocation, and the epic write**.
- `.claude/rules/datetime.md` — real timestamps

## Preflight
1. If `<name>` was not provided, tell the user how to call it and stop.
2. Read `CLAUDE.md` (or `CLAUDE.md.template`): load `## Architecture constraints` and
   `## HITL checkpoints`. These are **binding** — the epic may not contradict them. Warn
   if either heading is missing (declared detection degrades to agent judgement only).
3. Read `work/prd/<name>.analysis.md` if it exists. Any open question still `[ ]` and
   `class: blocking` must be resolved at this command's STOP before the epic is written.
4. If `work/epics/<name>/epic.md` already exists, ask to overwrite; proceed only on "yes".

## Instructions

### 1. Delegate the design to the Solution Architect
Use the `Task` tool to launch the `solution-architect` agent. Give it the PRD, the
analysis, and the architecture constraints. Ask it to **return as text** (it writes
nothing) an epic blueprint covering:
- Overview (technical summary of the *what* and the chosen shape)
- Architecture decisions — **respecting the hot/warm/cold boundary verbatim**; the agent
  must flag, never override, any tension with `CLAUDE.md`.
- Technical approach per path
- Story breakdown preview (high-level slices — *not* the stories themselves yet)
- Dependencies and sequencing risks
- Technical success criteria, traced to the PRD's success metrics
- A list of the checkpoints it hit (declared + raised), each with 2–3 options.

### 2. STOP — batch the architecture checkpoints
Collect every checkpoint the architect reached that matches a declared `CLAUDE.md` item,
resolves a blocking analysis question, or is *raised* as architecturally significant and
PRD-silent. Present the **independent** ones as a **single `AskUserQuestion` with multiple
questions**; reserve a serial STOP only for decisions that depend on a prior answer.
Consult the `product-owner` (via `Task`) to frame any checkpoint that is a *product*
decision (retention, user-visible behaviour) rather than purely technical.

For each resolved decision, allocate and write one ADR sequentially per the rule
(re-read `work/decisions/` before each write; fail loud on collision; ids quoted). Where
a decision answers a blocking analysis question, set that question's `resolved_by` to the
ADR id and flip it to `[x]`.

### 3. Write the epic, then verify it against the bands
Write `work/epics/<name>/epic.md` with frontmatter: `name`, `status: backlog`, `created`,
`updated`, `prd: <prd-path>`, `progress: 0%`, `hitl: ['<ADR ids>']`. Reflect each recorded
decision in the body.

**Post-generation band check (do not skip):** re-read the written epic against
`## Architecture constraints`. If anything assigned to the **hot path** references a
forbidden framework token (`@Component`, `@Autowired`, `@RestController`, `@Service`,
`@Bean`, Spring DI), **STOP** and surface it — a hard-rule violation is a bug, not a
style nit.

### 4. Hand off
Summarise the epic, the decisions recorded, and any still-deferred questions. Suggest:
"Next: `/epic:decompose <name>`".
