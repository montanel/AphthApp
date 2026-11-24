# AphthApp — handoff package

Everything Cece needs to build the app, without access to the design canvas on claude.ai.

| File | What it is |
|---|---|
| `DECISIONS.md` | What's decided and whether it's built. Read it first. Both sides edit it, one round at a time (rules at its top). |
| `SPEC.md` | The UI in words: colours, sizes, every screen, behaviour, motion, data. |
| `APP_ICON.md` | The app icon as Android resources. |
| `design/` | The screens themselves: `index.html` shows them all side by side; each `*.dc.html` is one screen. |
| `screenshots/` | A picture of each screen, drawn with the stand-in viewer, for a quick look. |

**Opening the designs:** open `design/index.html` in a browser. The screens are clickable: chips,
the aphtha card, the tabs, the year rows, a double-tap on the list background. `design/support.js`
is a small stand-in for the canvas's own runtime, written for this package so the files open
anywhere. It's only for viewing. Inter loads from Google Fonts, so offline the text falls back to
the system font.

**Which design counts:** the App page of the design canvas on claude.ai. The files in `design/` are
copies of it as of the Revision line in `DECISIONS.md`. The numbers on the screens are made-up
sample data.

**Phone:** Galaxy S26 Ultra at Luca's display zoom, 384 × 832 dp. The screens are drawn at exactly that size.
