---
id: '<epic>/NNN'         # epic-scoped, globally unique — quoted; e.g. 'webhook/001'
title: <short imperative, e.g. "Durable receipt before ACK">
status: backlog          # backlog | in-progress | complete
created: <ISO-8601 UTC>
updated: <ISO-8601 UTC>
epic: <epic-name>
path: warm               # hot | warm | cold | n/a  (n/a = no architecture band applies)
depends_on: []           # epic-scoped story ids that must land first, e.g. ['webhook/001']
parallel: true           # can this run alongside its siblings without file conflict?
metrics: []              # PRD success metric(s) this story serves, or 'none: <justification>'
hitl: []                 # quoted ADR ids, e.g. ['0001']; grows at decomposition AND /story:start
---

# NNN — <title>

A story is the **leaf deliverable**: a small, independently-shippable vertical slice that
can be demoed to a real user without explanation. Not a task list — the tasks are
discovered when the story starts.

## User story

**As a** <persona>
**I want** <capability>
**So that** <outcome>

> The *so that* must be real value, not invented to satisfy the template.

## Acceptance criteria

Given/When/Then or a numbered checklist. Each criterion independently verifiable by QA
without a follow-up question. Trace each one back to a PRD success metric where possible.

1. Given <context>, when <action>, then <observable result>
2. …

## Non-goals for this story

Explicit list — what this slice deliberately does *not* cover.

## Open questions

Anything still undecided, **with an owner**. These become checkpoints at `/story:start`
if they touch architecture or business — and get recorded as ADRs.

## Dependencies

Other stories, external decisions, or ADRs this story waits on.
