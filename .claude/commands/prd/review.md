---
allowed-tools: Bash, Read, Write, LS, Task, AskUserQuestion
---

# PRD Review

Walk a PRD with the Product Owner. Surface silent assumptions, internal contradictions,
vague success criteria, and the open questions that belong to humans — **without
answering any of them**. The first read-only beat of the workshop.

## Usage
```
/prd:review <prd-path>          # e.g. /prd:review PRD.md
```

## Required Rules
**Before executing, read and follow:**
- `.claude/rules/hitl-checkpoint.md` — the STOP protocol; **this command owns the
  `AskUserQuestion` BLOCK and all writes**; the agent only advises.
- `.claude/rules/datetime.md` — real timestamps

## Preflight
1. If `<prd-path>` was not provided, tell the user: "❌ Provide a PRD path: `/prd:review PRD.md`" and stop.
2. If the file does not exist, say so and stop.
3. **Derive `<name>`** — the slug used by every downstream command — from the PRD's
   frontmatter `name:` if present, else the file basename lowercased and de-extensioned
   (`PRD.md` → `prd`; `webhook-prd.md` → `webhook-prd`). **Echo the derived `<name>` to
   the user** so the next command is unambiguous.
4. Read `CLAUDE.md` (or `CLAUDE.md.template` if `CLAUDE.md` is absent) to load the
   declared HITL checkpoints and architecture constraints. Warn if either heading is missing.

## Instructions

This command **does not produce a plan or answer anything**. It produces a faithful
inventory of what the PRD decides, leaves open, and silently assumes.

### 1. Delegate the read to the Product Owner
Use the `Task` tool to launch the `product-owner` agent. Ask it to read the PRD end to
end and **return as text** (it writes nothing) a structured review with these sections:
- **Silent assumptions** — things the PRD treats as settled that aren't.
- **Internal contradictions** — X stated here, not-X implied there. Cite both lines.
- **Vague success criteria** — weasel words ("a while", "fast", "reliable") that need a measurable test.
- **Open questions** — both the PRD's explicit ones and any the agent raises.

The agent must **surface, not resolve**. If it proposes an answer, that is a bug.

### 2. Write the analysis
Write the agent's review to `work/prd/<name>.analysis.md` (create `work/prd/` if needed)
with frontmatter: `name`, `source: <prd-path>`, `created`, `status: reviewed`, `hitl: []`.
Render each open question as a **state-bearing list item** so downstream commands and
`/pm:status` read one representation:

```markdown
- [ ] **Q1** <question> — class: blocking | deferrable — owner: <role> — resolved_by: <ADR id or "—">
```

(`[ ]` open, `[x]` resolved; `class`/`owner` filled at step 3; `resolved_by` filled when an
ADR answers it — e.g. by `/prd:to-epic`.)

### 3. STOP — classify with the room
This is the checkpoint. Per `hitl-checkpoint.md`, call **`AskUserQuestion`** (one call,
batching all open questions as separate questions) to let the room mark each
**blocking** or **deferrable**. Do not classify them yourself.

Record the classification as one ADR (`work/decisions/NNNN-prd-review.md`, allocated per
the rule), set each question's `class` and the ADR id, and append the ADR id to the
analysis file's `hitl:` list.

### 4. Hand off
Report the derived `<name>`, the blocking vs deferrable split, and suggest:
"Next: `/prd:to-epic <name>`". Note that blocking questions remain `[ ]` until an ADR
resolves them, and will resurface as STOPs in `/prd:to-epic`.
