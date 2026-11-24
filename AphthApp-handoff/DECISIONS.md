# DECISIONS — AphthApp

Revision 3, 2026-11-24, design

This file is the single source of truth for what's been decided about the app and whether it's
built. It travels between two places: the code repo (worked on with Claude Code) and the design
chat on claude.ai. If something isn't written here, it isn't decided, whatever was said in a chat.

## How to use this file

**For everyone**
- Every item has a short ID (`D-001`, `Q-001`, `R-01`) so it can be referred to across tools.
- Items move down the page: Open question → Decided, not built → Built.
  Don't delete items; move them. Superseded items go to the Superseded section at the bottom
  with a note saying what replaced them.
- Each item says *what* was decided and, briefly, *why*. Details belong in the spec (`SPEC.md`,
  `APP_ICON.md`) and the design files (`design/*.dc.html`); point to the section instead of
  copying it.
- Add the date when an item moves.
- The *Revision* line under the title says which copy this is: a number, the date and the side
  that made it (`code` or `design`), written exactly as `Revision N, YYYY-MM-DD, side`.
  - **Raise the number by one each round you change the file**, with today's date and your side.
    For Claude Code a round is one session; for the design chat it is one hand-back. Raise it
    once per round, not once per edit.
  - **Before changing anything, compare the number with the last copy you saw.** A lower
    number means you were handed an old copy. The same number from the other side means both
    sides edited the same copy separately. In either case stop and ask for the latest copy, or
    merge the two, before editing.
  - Hand the file over whole, with its Revision line.

**For Claude Code (Cece)**
- Read this file before starting work, and follow it over the design files where they disagree.
  If they disagree, also add an Open question about it.
- When you hit something the spec doesn't settle, or that would mean going against the spec,
  don't guess. Add it under Open questions (what you found, the options you see, what each means
  for the result) and carry on with other work.
- When you finish building a decided item, move it to Built, with the date and the file(s) where
  it lives.
- If something you built works differently from the spec, add a note to the item saying how.

**For the design chat on claude.ai**
- Start by reading this file, especially Open questions.
- When a question is settled, move it to Decided, not built, and update the spec and design files
  to match. Hand back this file and any changed spec or design files.
- Never mark anything Built. Only the code side knows that.

---

## Standing rules
These apply to everything and don't move.

- **R-01** Only the current design files count. They live on the **App** page of the design canvas
  on claude.ai and are copied into this package as `design/*.dc.html`, with `SPEC.md` and
  `APP_ICON.md`. The copies can lag; the design chat says when it has changed one. The canvas's
  other pages (Today options, History options, Logo ideas) are history; "Later: Insights and tests" holds designs for after v1.
- **R-02** Every number in the designs is made-up sample data, there to show the layout. The app
  computes everything from the log. The sample numbers need not agree with each other.
- **R-03** Target phone: Galaxy S26 Ultra, 412 × 891 dp at default display settings, edge-to-edge
  with system-bar insets. **Today and History fit on one screen with no scrolling.** Settings,
  Insights and How it works may scroll.
- **R-04** Dark only, true black background, no dynamic colour. Stack assumed: Kotlin + Jetpack
  Compose (see Q-001).
- **R-05** No dots as separators (·) anywhere in the app's text. Use commas, line breaks or space.
  *Set by Luca 2026-11-24.*
- **R-06** The design files and the spec describe the whole UI: someone with only them should be
  able to rebuild it, layout and motion. Anything built that they do not draw or describe (a
  state, a message, a transition) gets a **Design side** note on its item so the design chat
  adds it, even when Luca is happy with it as built.
- **R-07** Keep it simple. Luca wants the fewest taps and screens possible; anything that adds
  friction to the evening log needs a good reason and an Open question first.
- **R-08** Store raw daily values, never only summaries, so that later analysis and tests (D-019)
  can use the full history.

---

## Open questions

- **Q-001** *Stack.* The spec assumes Kotlin + Jetpack Compose, local storage (Room or DataStore)
  and Android Auto Backup. Code: confirm, or say what you'd use instead and why.
- **Q-004** *Often used ranking.* The spec says "taps in the last 30 days, recent days counting a
  bit more". Code may pick the exact weighting; say what you chose.

---

## Decided, not built
Status not yet confirmed by Claude Code. Code: check each one against the current build and move
any that are already in to Built.

- **D-001** *One visual language.* True black, grey cards, one blue-grey for "selected", red only
  for aphthas, Inter, no gradients or glows. Cards have 20 dp corners; chips, pills and tabs are
  fully round; back buttons are 44 dp circles. *Decided 2026-11-24, Luca (after four rounds of
  options).* There is one red; the month calendar uses it at 28% strength, which reads as a deep
  maroon on the card. *Asked by Luca 2026-11-24:* the spec now names that shade `redSoft` with its
  colour code. → Spec: Colours, Type, Shapes.
- **D-002** *Navigation.* A short floating tab bar, icon over label. In v1 it has three tabs: Today,
  History, Settings. Insights comes back between History and Settings when it is built (D-021).
  The selected pill slides between tabs. *Decided 2026-11-24; three tabs in v1 decided by Luca
  2026-11-24.*
  → Spec: Tab bar; Motion.
- **D-003** *The aphtha card sits at the top of Today.* It is the point of the app, but the least
  often tapped thing, so it is prominent and deliberately out of the thumb's easy reach. Off by
  default every day; one tap turns it red with a check. No hint text on it. *Decided 2026-11-24.*
  → Spec: Today.
- **D-004** *Chips, not switches.* Every item is a rounded chip you tap. Chips never change width
  (the text weight stays the same when selected). All chips start empty every day; Luca ticks
  what happened. Every tap saves; there is no save button. *Decided 2026-11-24.* → Spec: Chips.
- **D-005** *Counts and levels in a corner badge.* Tea and coffee count cups (each tap adds one,
  4+ then back to 0); stress has two levels (one dot, two dots). The number or dots sit in a small
  badge on the chip's top-right corner. *Decided 2026-11-24 (over a number under the word).*
  → Spec: Chips.
- **D-006** *SLS toothpaste has three states.* Untouched = no SLS, one tap = SLS, two taps =
  "don't know" (dashed outline, a small "don't know" line under the name), three taps = back to
  no. There is no per-toothpaste setup. SLS and SDS are the same chemical. *Decided 2026-11-24.*
  → Spec: Chips.
- **D-007** *The item list.* Drinks: Tea, Coffee, Alcohol. Food: Banana, Nuts, Chocolate, Raw
  apple/carrot, Spicy, Citrus/tomato. Mouth care: Brushed 2×, Water floss, Floss, SLS toothpaste.
  Body: Bit mouth, Stress, Bad sleep, Feeling ill. Supplements: B complex. Hidden until switched
  on in Settings: Crunchy food, Gum/mints, Mouthwash, Dental visit. *Decided 2026-11-24.*
  → Spec: Items.
- **D-008** *Two sort modes, switched by double-tap.* By category, or often used. Often used puts
  the most-used item last, bottom right, nearest the thumb; the order is frozen while the screen
  is open and recalculated the next day. A double-tap anywhere between the aphtha card and the tab
  bar that is not a chip switches the mode. Two small icons above the tab bar show which mode is
  on; they cannot be tapped. The mode is remembered. *Decided 2026-11-24.* → Spec: Sort.
- **D-009** *What counts as an aphtha day, and an episode.* Yes only on days an aphtha hurts;
  painless healing days are a no. An episode is a run of yes days; runs separated by one or two
  no days are the same episode. Analysis keys on the first day of each episode. *Decided
  2026-11-10, Luca.* → Spec: Data.
- **D-010** *History opens on the year.* Three numbers on top (days aphtha-free, episodes this
  year, days usually between), then one strip per month with red where episodes were. The whole
  row (name and strip, 44 dp tall) is one tap target that opens that month. *Decided 2026-11-24
  (strips over mini-month calendars).* → Spec: History.
- **D-011** *The month view.* Back arrow to the year, the month's aphtha days and episodes, a
  calendar of 46 dp days, swipe for other months, tap a past day to edit it. *Decided 2026-11-24.*
  → Spec: Month.
- **D-012** *Editing a past day.* The Today screen for that date, with a back arrow, the date as
  the title and a blue-grey banner reading "Editing Sunday 8 November". The History tab stays
  selected. *Decided 2026-11-24.* → Spec: Past day.
- **D-013** *Settings is the fourth tab.* Reminders (evening time, next-morning nudge on or off),
  What to track (chips to show or hide items, plus "Add your own"), Data (export CSV, delete all).
  It may scroll a little. *Decided 2026-11-24.* → Spec: Settings.
- **D-014** *Reminders.* An evening notification at the chosen time (default 21:30), "How was
  today?". If a day was not logged, a nudge at 09:00 next morning, "Yesterday is still empty",
  which can be turned off. *Decided 2026-11-24.* → Spec: Notifications.
- **D-015** *Insights, the item screen and How it works are designed* (layout and wording, sample
  data) for after v1 (D-021). What they compute is still to be specified. *Decided 2026-11-24.*
  → Spec: Insights.
- **D-016** *Motion is quick and subtle.* Short, smooth, no bounce; presses shrink slightly;
  colours fade; the tab pill slides; a confirm haptic when the aphtha card turns on. *Decided
  2026-11-24.* → Spec: Motion.
- **D-017** *The app icon is Orbit: a blue-grey ring with a red dot*, as an adaptive icon kept
  inside the safe area. The red dot is the app's aphtha red: an aphtha, and a logged day.
  *Decided 2026-11-24; red dot asked for by Luca 2026-11-24 (was all blue-grey).* At notification size it may read like a "refresh" arrow; Luca keeps it for
  now and will judge it on his phone (was Q-003). → `APP_ICON.md`.
- **D-018** *Data model.* One record per day with the aphtha flag and raw values per item; a day
  with no record is "not logged", unlike a logged day with nothing ticked. Items carry key, label,
  group, type, visible, order, and an unused `testable` field for later. *Decided 2026-11-24.*
  → Spec: Data.
- **D-019** *Tests come later.* Deliberate experiments (a few weeks with and without something)
  need a few months of data first, so they are not in v1. Some items can never be tested by
  adding them (Luca is mildly allergic to raw apple and carrot); the `testable` field is for
  that. The old test screens are on the canvas's "Later" page, not revised. *Decided 2026-11-11.*
- **D-020** *No setup screens.* No first launch, no blood-test list, no separate "What I track"
  screen; how often Luca gets aphthas is learned from the log. *Decided 2026-11-24.*
- **D-021** *Insights is not in v1.* Luca wants it later, not now. v1 is Today (with past days),
  History, Settings and the two reminders. The tab bar has no Insights tab until it is built; its
  three screens are on the canvas's "Later: Insights and tests" page and in `design/` as
  reference. Logging stays as specified, so Insights can later use the full history (R-08).
  *Decided by Luca 2026-11-24 (was Q-002).* → Spec: Tab bar, Insights.

---

## Built
- *(none yet)*

---

## Superseded
- **Q-002** Is Insights part of v1? *Answered 2026-11-24, Luca:* no, later. Hide the tab in v1.
  See D-021.
- **Q-003** Does the icon read like a "refresh" arrow at small sizes? *Answered 2026-11-24, Luca:*
  keep it for now; he will check it on his phone. See D-017.
- **S-001** Switch toggles in a single list for the daily log. *Replaced 2026-11-11 by chips
  (D-004).*
- **S-002** A separate "new ulcer" screen with location, size and pain. *Replaced 2026-11-10 by a
  single yes for the day (D-003, D-009):* aphthas come in bunches, positions are hard to tell and
  unlikely to matter.
- **S-003** Cup count as a small number under the word. *Replaced 2026-11-24 by the corner badge
  (D-005).*
- **S-004** History as mini-month calendars, or month tiles. *Replaced 2026-11-24 by strips
  (D-010).*
- **S-005** A past day with only the date and back arrow. *Replaced 2026-11-24 by the version with
  the banner (D-012).*
- **S-006** A text-only tab bar, and the Tests tab. *Replaced 2026-11-24 by the four-tab icon bar
  (D-002); tests moved to later (D-019).*
- **S-007** First launch, Blood tests and What I track screens. *Removed 2026-11-24 (D-020); What
  I track lives in Settings (D-013).*
