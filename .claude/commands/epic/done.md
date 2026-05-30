---
allowed-tools: Bash, Read, Write, LS, Task, AskUserQuestion
---

# Epic Done

The declared **"Claiming the work is done"** checkpoint, made real. An epic is not done
because its stories are closed — it is done when the **PRD's success metrics** are each
satisfied or explicitly flagged out-of-scope-for-v0, and a human agrees. No agent declares
victory alone.

## Usage
```
/epic:done <name>               # epic name, e.g. /epic:done webhook
```

## Required Rules
**Before executing, read and follow:**
- `.claude/rules/hitl-checkpoint.md` — the STOP protocol; **this command owns the
  `AskUserQuestion` BLOCK, the ADR, and the status write**.
- `.claude/rules/datetime.md` — real timestamps

## Preflight
1. If `<name>` was not provided, tell the user how to call it and stop.
2. If any story is not `complete`, list them and ask whether to proceed (normally: no —
   finish or explicitly drop them first via `/story:complete`).
3. Read the PRD's **Success metrics** section — these are the acceptance criteria for the
   whole epic, per `CLAUDE.md`.

## Instructions

### 1. Map every success metric to evidence (qa-lead, advisory)
Use the `Task` tool to launch the `qa-lead` agent to **return as text** a metric-by-metric
verdict: for each PRD success metric (e.g. webhook tickets → 0; 99.9% processed within
5 min; reconciliation discrepancy < €50/month; zero "did we get event X" tickets), state
satisfied / not-satisfied / not-measurable-yet, with the evidence. No metric may be marked
satisfied by assertion alone.

### 2. Compliance sweep (code-reviewer, advisory)
Use the `Task` tool to launch the `code-reviewer` agent to confirm the documented hard
rules held across the epic: hot-path stays framework-free, no PII at INFO, no secrets in
the repo, no commits to `main` without review (`CLAUDE.md`). Critical/Major findings block.

### 3. STOP — declare the epic done (or not)
Call `AskUserQuestion`: mark the epic `complete`; or keep it open with named gaps; or
accept specific metrics as out-of-scope-for-v0 with rationale. Do not self-declare. Record
one ADR (sequential allocation, quoted id) listing each metric's disposition and any
accepted out-of-scope item.

### 4. Write
On "complete": set `epic.md` `status: complete`, `progress: 100%`, bump `updated`, append
the ADR id to `hitl:`. Otherwise leave it open and report the unmet metrics.

### 5. Hand off
Report the metric scorecard, the compliance verdict, and the decision. This is the only
command that can move an epic to `complete`.
