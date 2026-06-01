---
allowed-tools: Bash, Read, Write, LS, Task, AskUserQuestion
---

# Story Start

Kick off work on one or more ready stories. This is the **second HITL loop —
implementation-time**. Opening a story routinely exposes architecture or business
questions invisible at decomposition. This command surfaces them, records the decisions,
and **stops at the gate** — it does not write production code.

> Default: this command *opens the gate*, it does not implement. The STOP is the
> teachable moment. Actual building is the next, separate beat — driving the
> `backend-developer` to implement against the discovered tasks and ADRs.

## Usage
```
/story:start <id> [<id> ...]    # epic-scoped ids, e.g. /story:start webhook/001 webhook/003
/story:start <epic>/ready       # opens every ready, parallel-safe story in the epic at once
```

## Required Rules
**Before executing, read and follow:**
- `.claude/rules/hitl-checkpoint.md` — the STOP protocol; **this command owns the
  `AskUserQuestion` BLOCK and all writes. The delegated agent advises only and writes
  nothing.**
- `.claude/rules/datetime.md` — real timestamps

## Preflight
1. If no `<id>` was provided, tell the user how to call it and stop.
2. Resolve each id. Ids are **epic-scoped** (`<epic>/NNN`); a bare `NNN` is accepted only
   if unambiguous across epics, otherwise ask which epic. `<epic>/ready` expands to every
   story in that epic whose `status: backlog`, all `depends_on` are `complete`, and
   `parallel: true`.
3. For each story, check `depends_on`: if any prerequisite is not `complete`, warn and ask
   whether to proceed anyway.
4. Read `CLAUDE.md` architecture constraints and HITL checkpoints — the boundaries each
   story must respect (especially the `path: hot` framework rule).

## Instructions

### 1. Read each story with the implementer's eye (advisory only)
For each selected story, use the `Task` tool to launch the `backend-developer` agent with
an explicit instruction: **do not write any files or code — return analysis as text only.**
Ask it for:
- The concrete tasks it now sees (discovered, not pre-planned).
- Any **architecture question** visible only in implementation
  (e.g. "this slice assumes the warm path owns idempotency — confirm the boundary").
- Any **business question** the ACs leave ambiguous.
- Any place a task would touch a `CLAUDE.md` boundary (hot-path framework, PII logging),
  and whether any `assumed` AC needs confirming.

Route architecture questions through the `solution-architect` and business questions
through the `product-owner` to frame options.

### 2. STOP — decide the kickoff questions (batched)
Collect the surfaced questions across **all** the stories being started and present the
independent ones as a **single `AskUserQuestion`** (batch; serialize only dependent ones).
Record one ADR per resolved decision, tagged `loop: implementation` (sequential allocation,
quoted ids) — these are the proof of the second loop.

### 3. Open the gate (do not code)
For each started story:
- Set `status: in-progress`, bump `updated`, and **append the new ADR ids to its `hitl:`
  list** (now holding both decomposition- and implementation-time decisions).
- **Verify against the bands:** if `path: hot`, confirm no discovered task introduces a
  forbidden framework token; if it would, STOP. (`path: n/a` skips this guard.)
- Write the discovered tasks into the story under "## Tasks (discovered at start)".

### 4. Re-forecast hook
If any recorded `loop: implementation` ADR's **Implications** affect effort or scope, and
`work/epics/<epic>/estimate.md` exists, set its `needs_reforecast: true` and append the
reason to `reforecast_reason`. Do not silently let the forecast rot — `/pm:status` will
surface it and `/epic:estimate` will re-run.

### 5. Hand off
Summarise per story: tasks discovered, decisions recorded, boundaries to respect, and
whether the estimate was flagged stale. State plainly: implementation is the next,
separate beat — drive the `backend-developer` to build against these tasks and ADRs.
