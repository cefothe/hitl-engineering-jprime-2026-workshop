# HITL Checkpoint Protocol

The shared discipline every `/prd:*`, `/epic:*`, and `/story:*` command obeys. This
file is the one piece of real engineering in the workflow — everything else is
choreography around it.

> **The thesis:** agents *propose*, humans *approve*. A command must never silently
> resolve a question that belongs to the room. When it reaches a boundary, it stops,
> presents options, waits for a human decision, and records that decision.

---

## Who owns the BLOCK and the writes (read this first)

The human gate is only real if it is *executed*, not narrated. So:

- **The orchestrating command — running in the main session — owns every file write and
  every `AskUserQuestion` call.** It must have `AskUserQuestion` in its `allowed-tools`.
- **Delegated role agents are advisory.** When a command launches a `Task` sub-agent
  (product-owner, solution-architect, backend-developer, …), that agent **returns its
  analysis as text**. It does not write the artifact and it does not run the gate. The
  command persists the result and runs the `AskUserQuestion` itself.
- **Never delegate the BLOCK or the write to a sub-agent.** A sub-agent cannot put a
  question to the room; if you let it "decide", the gate has been skipped.
- **If `AskUserQuestion` returns no human answer, halt.** Write nothing further. A missing
  answer is not permission to proceed — it is a stop.

This is what makes "agents propose, humans approve" structural rather than aspirational.

---

## When to STOP (hybrid detection)

A command must trigger a checkpoint whenever the artifact it is about to write would
**resolve a question the humans own**. Detection is *hybrid* — two sources, combined:

1. **Declared (seeded from `CLAUDE.md`).** Read the project's `CLAUDE.md` before
   generating anything. Treat as a binding checkpoint list:
   - every item under the heading **`## HITL checkpoints`**
   - every constraint under **`## Architecture constraints`** (e.g. a framework boundary)
   - every item under the PRD's own "Open questions" section

   **Heading contract.** Detection of the declared half depends on these exact headings.
   If `CLAUDE.md` is missing **either** heading, the command must **emit a visible
   warning** ("⚠️ no `## HITL checkpoints` heading in CLAUDE.md — declared detection
   disabled, relying on agent judgement only") and continue on source (2) alone. It must
   never fail silently — silent under-firing is the worst outcome, because it looks
   compliant. When you reuse this pipeline in another repo, give that `CLAUDE.md` these
   two headings or the declared gates will not fire.

2. **Raised (agent judgement).** The owning role agent may surface a checkpoint the
   list did not anticipate — a silent assumption, an internal contradiction, an
   architecturally-significant fork the PRD is silent on. A *raised* checkpoint is as
   binding as a *declared* one.

**Precedence when a topic appears in more than one seed-list.** Some questions appear in
several places with different scope (e.g. out-of-order events: the PRD asks *whether it
matters*, `CLAUDE.md` gates *adding logic for it*). The **broadest framing wins** — gate
the **decision** ("do we need to handle this at all, and if so how?"), not merely the
*act* of writing code for it. Deciding "no" by omission is still a decision and still
STOPs.

---

## The four-step macro

When a checkpoint fires, run these four steps in order. Do not skip step 3.

### 1. Detect
Name the checkpoint out loud: *which* declared item or *what* raised concern. State
what the artifact would commit to if you proceeded without asking.

### 2. Propose
The owning role agent presents **2–3 concrete options with trade-offs** — never a
single recommendation dressed as a question, never an open-ended "what do you want?".
A recommendation is allowed, but it must be auditable, not a fait accompli.

**Options must be genuine.** Each option must be one a reasonable stakeholder could
actually choose — no strawmen padding a single real choice. In the ADR, for each
*rejected* option, state why a human might legitimately have preferred it.

### 3. Decide — BLOCK HERE (orchestrating command only)
Call `AskUserQuestion` to put the options to the room. **Wait for the answer.** No
default-and-proceed, no "I'll assume X for now". The command does not continue past this
point until a human has chosen. See "Who owns the BLOCK" above.

**Batch independent decisions.** If a single generation pass surfaces several *independent*
checkpoints (e.g. `/prd:to-epic` reaching data store, retention, and out-of-order at once),
present them as **one `AskUserQuestion` with multiple questions** — not N blocking
round-trips. Collapse the *interaction*, not the decisions: you still record one ADR per
decision. Reserve a serial, single-question STOP for genuinely **dependent** decisions,
where one answer changes which later questions even apply.

### 4. Record
Write an ADR to `work/decisions/NNNN-<slug>.md` using `.claude/templates/adr.md`.
Then add the ADR id to the `hitl:` frontmatter list of the artifact that triggered it.
The ADR is the audit trail — it is why the decision was made, not just what.

---

## ADR allocation (do this carefully — the audit trail depends on it)

- IDs are four-digit, zero-padded, monotonic strings: `0001`, `0002`, …
- **The orchestrating command allocates ADR numbers sequentially, one at a time, and
  writes the file before moving to the next checkpoint.** Re-read `work/decisions/`
  immediately before each write to get the next number (highest + 1, starting at `0001`).
- **Never let parallel `Task` sub-agents allocate ADR numbers** — two forks each computing
  "highest + 1" will collide and silently overwrite a decision record. Sub-agents return
  the *content* of a decision; the command assigns the number and writes the file.
- **Fail loud on collision.** If the chosen `NNNN-*.md` already exists, stop — do not
  overwrite. Re-read and re-number.
- Slug: lowercase, hyphenated, ≤ 5 words (`0001-event-store-postgres.md`).

## The `hitl:` frontmatter field

Every generated artifact (epic, story, estimate) carries:

```yaml
hitl: ['0001', '0004']   # ADR ids — quoted strings, never bare 0001 (YAML reads that as int 1)
```

**Quote the ids.** Bare `hitl: [0001]` is parsed by YAML as the integer `1`, which no
longer matches the file `0001-*.md` — the traceability join breaks. Always quote:
`['0001']`. The same applies to `id:` in the ADR and story templates.

This makes the decision trail auditable **backward** from any artifact. A story whose
`hitl:` list grows *after* `/story:start` is the visible proof of the second HITL loop —
a decision that did not exist at decomposition time.

---

## Two loops, one protocol

The same four-step macro runs at two different times:

| Loop | Fires during | Typical questions |
|---|---|---|
| **Decomposition** | `/prd:to-epic`, `/epic:decompose` | data store, identity mapping, retention, ordering — things that block *planning* |
| **Implementation** | `/story:start` | "this story assumes X — true?" — things visible only once you are *in the code* |

A checkpoint raised in loop 2 is not a failure of loop 1. Some decisions genuinely
cannot be made until implementation begins. Recording them is the point — and when a
loop-2 decision changes scope or effort, the estimate must be flagged stale (see
`/story:start` and `/epic:estimate`) so the forecast does not silently rot.

---

## What this protocol forbids

- Answering a PRD "Open question" yourself.
- Picking one side of a contradiction silently.
- Choosing a data store / framework / boundary the `CLAUDE.md` reserves for the human.
- Resolving a gated decision *by omission* (e.g. writing no ordering logic to dodge the gate).
- Declaring work "done" without checking against the PRD's success metrics.
- Letting a sub-agent run the gate or write the artifact.
- Proceeding past step 3 without a recorded human decision.
