---
id: 'NNNN'                # quoted — bare 0001 is parsed as the integer 1, breaking the hitl: join
title: <short decision title>
status: accepted          # proposed | accepted | superseded
created: <ISO-8601 UTC>
loop: decomposition       # decomposition | implementation
triggered_by: <command or agent that raised it, e.g. /epic:decompose or solution-architect>
artifact: <relative path to the epic/story this decision belongs to>
checkpoint: <the declared CLAUDE.md checkpoint or "raised">
---

# NNNN — <decision title>

> One Architecture/Product Decision Record per resolved HITL checkpoint. Matches the
> decision-record template the `product-owner` and `solution-architect` agents already
> use, so the repo speaks one decision vocabulary.

## Context

What question came up, and *why it belongs to the room* rather than the agent. Quote the
PRD line or `CLAUDE.md` checkpoint that makes this a human decision.

## Options considered

- **Option A — <name>:** pros / cons
- **Option B — <name>:** pros / cons
- **Option C — <name>:** pros / cons

## Decision

What the room chose, and the reasoning. Name who decided if relevant.

## Implications

What changes downstream — which stories, which constraints, which other decisions this
now binds or unblocks.

## Revisit if

The conditions that would legitimately reopen this decision (new compliance answer,
load profile changes, a blocked assumption turns out false).
