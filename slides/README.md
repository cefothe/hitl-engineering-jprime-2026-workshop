# Slides

Marp deck for the **HITL Engineering — jPrime 2026** workshop.

## Preview / build

No install needed — uses `npx`:

```bash
# Live preview (auto-reloads on save)
npx @marp-team/marp-cli@latest -w slides.md

# Export to HTML
npx @marp-team/marp-cli@latest slides.md -o slides.html

# Export to PDF
npx @marp-team/marp-cli@latest --pdf slides.md -o slides.pdf

# Export to PPTX
npx @marp-team/marp-cli@latest --pptx slides.md -o slides.pptx
```

## VS Code

Install the **Marp for VS Code** extension — gives you a live side-by-side preview while editing `slides.md`.

## Files

- `slides.md` — the deck (Marp markdown with a custom inline theme — warm palette, no Gaia)
- `slides.pdf` — pre-built PDF for offline viewing
- `README.md` — this file
