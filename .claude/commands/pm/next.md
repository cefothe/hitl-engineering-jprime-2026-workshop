---
allowed-tools: Bash, Read, LS
---

# PM Next

Read-only. Recommend the next action(s) in the pipeline, and why.

## Usage
```
/pm:next
```

## Instructions

Determine where the pipeline is and name the next step. Priority order:

1. **Unanswered blocking question** (`[ ]`, `class: blocking`, no `resolved_by`) in a PRD
   analysis → next is to resolve it (a STOP in `/prd:to-epic`). Name it.
2. **PRD reviewed, no epic** → `/prd:to-epic <name>`.
3. **Epic exists, no stories** → `/epic:decompose <name>`.
4. **Stories exist, no estimate** → `/epic:estimate <name>`. Strongly recommended, not
   optional: without it, critical-path sequencing below is unavailable.
5. **Estimate stale** (`needs_reforecast: true`) → `/epic:estimate <name>` to re-forecast.
6. **Ready stories** (all `depends_on` `complete`, status `backlog`) → recommend
   `/story:start` for the **whole ready, `parallel: true` set at once** (epic-scoped ids),
   not just one. If `estimate.md` exists, order by `critical_path`; if it does not, say so
   explicitly ("no estimate — cannot rank by critical path; starting by dependency order").
7. **Everything started** → point at the highest-value `in-progress` story to drive to
   completion, then `/story:complete <id>` / `/epic:done <name>`.

Output: the recommended command(s), the artifacts they act on, and a one-line reason.
Do not modify anything.
