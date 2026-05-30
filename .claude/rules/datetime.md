# Datetime

Get the real current time in ISO-8601 UTC for every `created` / `updated` / ADR
timestamp. Never invent or guess a timestamp.

```bash
date -u +"%Y-%m-%dT%H:%M:%SZ"
```

Example output: `2026-06-03T09:14:05Z`
