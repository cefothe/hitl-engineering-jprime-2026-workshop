---
allowed-tools: Bash, Read, Write, LS, Task, AskUserQuestion
---

# Epic Estimate

Have the Project Manager estimate the decomposed epic and **test it against the stated
timeline** — pushing back honestly instead of absorbing pressure. The judgment beat:
"do we accept this estimate, or change scope/date?" is a human decision.

## Usage
```
/epic:estimate <name>           # epic name, e.g. /epic:estimate webhook
```

## Required Rules
**Before executing, read and follow:**
- `.claude/rules/hitl-checkpoint.md` — the STOP protocol; **this command owns the
  `AskUserQuestion` BLOCK, the ADR, and the estimate write**.
- `.claude/rules/datetime.md` — real timestamps

## Preflight
1. If `<name>` was not provided, tell the user how to call it and stop.
2. If `work/epics/<name>/stories/` has no stories, say: "❌ Decompose first: `/epic:decompose <name>`" and stop.
3. Read the PRD's "Timeline" section (if any) — the date the estimate must be measured against.
4. If `work/epics/<name>/estimate.md` exists with `needs_reforecast: true`, note **why**
   (the implementation-time ADRs since the last forecast) and re-estimate from current state.

## Instructions

### 1. Delegate to the Project Manager
Use the `Task` tool to launch the `project-manager` agent. Ask it to **return as text**:
- A per-story estimate with a confidence range (not a single point).
- The **critical path** through `depends_on`, and what can run in parallel.
- The top 3 risks, each with a trigger and a mitigation.
- A forecast date with a confidence interval, **compared to the PRD's stated timeline**.
- If the gap is real: what would have to be true to hit the date, and concrete scope-cut options.

The PM must not quietly make the numbers fit the deadline. An unrealistic timeline is
surfaced, not absorbed.

### 2. STOP — accept or push back
Call `AskUserQuestion`: accept the estimate as-is, cut scope to hit the date, or move the
date. If scope is cut, route the *what-to-cut* decision to the `product-owner` (value is
theirs to protect). Record the outcome as one ADR (sequential allocation, quoted id).

### 3. Write the estimate
Write `work/epics/<name>/estimate.md` with frontmatter:
`name`, `created`, `updated`, `forecast_date`, `confidence`, `critical_path: ['<story ids>']`,
`needs_reforecast: false`, `reforecast_reason: ''`, `hitl: ['<ADR ids>']`, and the PM's
analysis in the body. (`critical_path` is what `/pm:next` and `/pm:status` consume — keep it
current.) If the timeline decision changed scope, note which stories were dropped/deferred
and update their `status`.

### 4. Hand off
Report the accepted forecast and any scope change. Suggest: "Next: `/story:start <id>`".
