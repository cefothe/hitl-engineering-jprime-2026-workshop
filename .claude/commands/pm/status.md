---
allowed-tools: Bash, Read, LS
---

# PM Status

Read-only snapshot of where every artifact sits in the pipeline. No generation, no STOPs.

## Usage
```
/pm:status
```

## Instructions

Scan `work/` and report concisely. Do not modify anything.

1. **PRDs / analyses** — for each `work/prd/*.analysis.md`: name, status, and the count of
   open (`[ ]`) blocking vs deferrable questions, reading the per-question state items.
2. **Epics** — for each `work/epics/*/epic.md`: name, status, `progress`, story count.
3. **Stories** — per epic, counts by status (`backlog` / `in-progress` / `complete`) and
   the parallel-vs-sequential split.
4. **Forecast** — if `work/epics/*/estimate.md` exists, surface: `forecast_date` vs the
   PRD timeline (on-track / slipping-by-N), the top open risk, and which pending gate (if
   any) sits on the `critical_path`. If `needs_reforecast: true`, flag prominently:
   "⚠️ estimate stale — N implementation-time decisions since last forecast; re-run
   `/epic:estimate <name>`." If no estimate exists, say "forecast unavailable — no estimate yet."
5. **WIP & aging** — count `in-progress` stories (gently flag if above ~3 — too much
   started, nothing finishing). Age each open checkpoint and `in-progress` story by its
   `updated` timestamp and name the **oldest stalled** one.
6. **Decisions** — total ADRs in `work/decisions/`, split by `loop:` (decomposition vs
   implementation). The implementation count is the second-loop signal.
7. **Open checkpoints** — any `[ ]` blocking question with no `resolved_by` ADR. These are
   the next STOPs waiting to happen.

Keep it a scannable summary, not a file dump. End with the single most useful next action.
