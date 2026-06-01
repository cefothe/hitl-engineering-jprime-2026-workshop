---
allowed-tools: Bash, Read, Write, LS, Task, AskUserQuestion
---

# Epic Decompose

Break an epic into **small, independently-deliverable stories** — vertical slices, not a
task breakdown. The second decomposition-loop beat: product and architecture questions
that only become visible while splitting get surfaced and recorded.

## Usage
```
/epic:decompose <name>          # epic name, e.g. /epic:decompose webhook
```

## Required Rules
**Before executing, read and follow:**
- `.claude/rules/hitl-checkpoint.md` — the STOP protocol; **this command owns the
  `AskUserQuestion` BLOCK, ADR allocation, and the story writes**; the agent advises only.
- `.claude/rules/datetime.md` — real timestamps

## Preflight
1. If `<name>` was not provided, tell the user how to call it and stop.
2. If `work/epics/<name>/epic.md` does not exist, say: "❌ Epic not found. First run: `/prd:to-epic <name>`" and stop.
3. If `work/epics/<name>/stories/` already holds stories, list them and ask before recreating; proceed only on "yes". Prefer stable ids on re-run (append/keep, don't renumber from 001).
4. Read `CLAUDE.md` architecture bands so each story can be tagged `path: hot|warm|cold|n/a`
   (`n/a` = no band applies). Read the PRD's Goals, Functional requirements, and Success metrics.

## Instructions

### 1. Delegate the split to the Product Owner
Use the `Task` tool to launch the `product-owner` agent. Ask it to **return as text** a
decomposition into stories that each satisfy INVEST, with a hard bias toward **small**:
- Independently shippable and demoable to a real user without explanation.
- One acceptance-criteria cluster, roughly 1–3 days of work.
- ACs in Given/When/Then. **Tag each AC `derived` (cite the PRD line/metric it comes from)
  or `assumed` (PRD is silent).**
- Reject any story that bundles multiple users, multiple outcomes, or can't be demoed alone.

There is **no task tier**. Tasks are discovered at `/story:start`, not pre-planned here.

### 2. STOP on questions surfaced while splitting — including invented product truth
Decomposition exposes questions planning missed. **Every `assumed` AC is itself a product
decision and a checkpoint** — it must not sail through silently. Batch these with any
architecture/business forks (identity mapping, ordering, a vague business rule) into a
single `AskUserQuestion` where independent; route architecture forks through the
`solution-architect`, product forks through the `product-owner`. Record one ADR per
resolved decision (sequential allocation, quoted ids).

### 3. Write the stories
Write each story to `work/epics/<name>/stories/NNN-<slug>.md` using
`.claude/templates/story.md`. Set the epic-scoped `id: '<name>/NNN'`, `path`, `depends_on`
(epic-scoped ids), `parallel`, `metrics` (the PRD metric ids the story serves, or
`none: <justification>`), and seed `hitl:` with any ADR ids from the split. Number
sequentially (`001`, `002`, …).

### 4. Coverage assertion (do not skip) + update the epic
List every PRD **Goal** and **Functional requirement** and the story (or explicit
out-of-scope ADR) that covers it. **Any uncovered requirement is a STOP** — surface it via
`AskUserQuestion`: add a story, or record an out-of-scope ADR. This catches a stated
requirement (e.g. "look up an event by PayHub event ID") silently disappearing.

Add a "Stories" section to `epic.md` listing each story with its `path`, `metrics`, and
dependencies. Report the slice count and the parallel-vs-sequential split. Suggest:
"Next: `/epic:estimate <name>` for a timeline, or `/story:start <id>` to begin one."
