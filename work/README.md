# work/ — the pipeline's data layer

Everything the HITL pipeline generates lives here as plain markdown. No database, no
network, no GitHub — readable in the repo, diffable in a PR, take-home after the workshop.

```
work/
├── prd/<name>.analysis.md          # /prd:review   — surfaced flaws & open questions
├── epics/<name>/
│   ├── epic.md                     # /prd:to-epic  — architecture-bound blueprint;
│   │                               #   /epic:done sets status: complete after metric verification
│   ├── stories/NNN-<slug>.md       # /epic:decompose — small, deliverable vertical slices;
│   │                               #   /story:start → in-progress, /story:complete → complete
│   └── estimate.md                 # /epic:estimate — forecast, critical path, stale flag
└── decisions/NNNN-<slug>.md        # ADRs — one per resolved HITL checkpoint, both loops
                                     #   (ids are quoted strings: '0001', '0002', …)
```

This directory starts empty. The commands create what they need. The point of the
workshop is to watch it fill up — and to watch where the agent **stops and asks** before
it writes.

See [`../.claude/rules/hitl-checkpoint.md`](../.claude/rules/hitl-checkpoint.md) for the
STOP protocol every command obeys.
