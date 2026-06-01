---
allowed-tools: Bash, Read, Write, LS, Task, AskUserQuestion
---

# Story Complete

Close a single story honestly: its acceptance criteria are met **with evidence**, it has
passed review, and "done" is a human decision — not an agent's self-declaration. The
per-story half of the orphaned "claiming the work is done" checkpoint.

## Usage
```
/story:complete <id>            # epic-scoped, e.g. /story:complete webhook/001
```

## Required Rules
**Before executing, read and follow:**
- `.claude/rules/hitl-checkpoint.md` — the STOP protocol; **this command owns the
  `AskUserQuestion` BLOCK, the ADR, and the status write**.
- `.claude/rules/datetime.md` — real timestamps

## Preflight
1. If `<id>` was not provided, tell the user how to call it and stop.
2. Resolve the epic-scoped story; if not `in-progress`, warn and ask whether to proceed.

## Instructions

### 1. Verify acceptance criteria (qa-lead, advisory)
Use the `Task` tool to launch the `qa-lead` agent to **return as text** a coverage map:
each AC of the story → satisfied / not-satisfied / not-verifiable, with the evidence
(test, manual check, or "none"). Per `CLAUDE.md`, "verified" requires evidence, not
assertion. Flag any `assumed` AC that was never confirmed by an ADR.

### 2. Review gate (code-reviewer, advisory)
Use the `Task` tool to launch the `code-reviewer` agent for a pre-merge check of the
story's changes (correctness, security, documented-rule compliance — including the
hot-path framework rule and the no-PII-logging rule). It returns severity-classified
findings; **any Critical or Major blocks completion.**

### 3. STOP — declare done (or not)
Call `AskUserQuestion`: mark the story `complete`, send it back (ACs unmet / review
findings open), or accept a named gap as out-of-scope-for-v0. Do not self-declare. Record
one ADR (sequential allocation, quoted id) capturing the decision and any accepted gap.

### 4. Write
On a "complete" decision: set the story `status: complete`, bump `updated`, append the ADR
id to `hitl:`, and recompute the epic's `progress`. Otherwise leave it `in-progress` and
report what remains.

### 5. Hand off
Report AC coverage, review verdict, and the decision. If this was the last open story,
suggest: "Next: `/epic:done <name>`".
