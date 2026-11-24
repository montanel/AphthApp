# DECISIONS — AphthApp

Revision 7, 2026-11-24, code

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
- **R-03** Target phone: Galaxy S26 Ultra at Luca's display zoom, **384 × 832 dp**, edge-to-edge
  with system-bar insets. **Today, the past day and History fit on one screen with no scrolling.**
  Settings, Insights and How it works may scroll. *Changed 2026-11-24, Luca:* was 412 × 891 dp
  (the default zoom); every design is now drawn at 384 × 832 (D-028).
- **R-04** Dark only, true black background, no dynamic colour. Stack: Kotlin + Jetpack Compose
  (D-022).
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

*(none right now)*

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
  apple/carrot, Spicy, Citrus/tomato. Mouth care: Brushed (0 to 2, D-024), Water floss, Floss,
  SLS toothpaste. Body: Bit mouth, Stress, Bad sleep, Feeling ill. Supplements: B complex. Hidden
  until switched on in Settings: Crunchy food, Gum/mints, Mouthwash, Dental visit. *Decided
  2026-11-24.* → Spec: Items.
- **D-008** *Two sort modes, switched by double-tap.* By category, or ~~often used~~ most recent
  (D-027). The second mode puts its item last, bottom right, nearest the thumb. A double-tap
  anywhere between the aphtha card and the tab bar that is not a chip switches the mode. Two small
  icons above the tab bar show which mode is on; ~~they cannot be tapped~~ a single tap on them
  also switches it (D-026). The mode is remembered. *Decided 2026-11-24.* → Spec: Sort.
- **D-009** *What counts as an aphtha day, and an episode.* Yes only on days an aphtha hurts;
  painless healing days are a no. An episode is a run of yes days; runs separated by one or two
  no days are the same episode. Analysis keys on the first day of each episode. *Decided
  2026-11-10, Luca.* → Spec: Data.
- **D-010** *History opens on the year.* Three numbers on top (days aphtha-free, episodes this
  year, days between episodes), then one strip per month with red where episodes were. The whole
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
  *Decided 2026-11-24; red dot asked for by Luca 2026-11-24 (was all blue-grey).* At notification
  size it may read like a "refresh" arrow; Luca keeps it for now and will judge it on his phone
  (was Q-003). *Changed 2026-11-24, Luca:* the background gets a slightly lighter tint, since
  near-black vanished on his black wallpaper. *Tint picked 2026-11-24, Luca:* dark blue-grey
  `#1A1F29` (was Q-008). → `APP_ICON.md`.
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
- **D-022** *Storage.* Kotlin + Compose (without the Material library). The log is one JSON file in
  the app's storage, included in Auto Backup, holding the raw values as in Spec: Data. Luca's
  only requirement: the app must be able to read it back for the analysis. *Decided by Luca
  2026-11-24 (was Q-001).*
- **D-023** *A log day starts at 04:00*, like AnkiDroid. Until then, Today shows (and saves to) the
  previous day, so a log made just after midnight goes to the evening it belongs to. *Decided by
  Luca 2026-11-24 (was Q-006).* **Design side:** add this to Spec: Data and Today.
  *Spec updated 2026-11-24, design chat:* Today says which day it shows, Data has the log day.
- **D-024** *Brushing counts up to 2.* The Mouth care item is now "Brushed" with a corner badge
  like Tea: 0 → 1 → 2 → 0, minimum width 78 dp. It replaces the yes/no "Brushed 2×" in D-007.
  *Asked by Luca 2026-11-24.* **Design side:** update Spec: Chips and Items, and
  `design/Main.dc.html`.
  *Spec updated 2026-11-24, design chat:* Chips, Items and Data say so, and `Main.dc.html` shows
  Brushed with a 2. With the narrower screen, the minimum width of every badge chip (Tea, Coffee,
  Brushed, Stress) is now 72 dp (D-028). Settings and Insights say "Brushed" too.
- **D-025** *Travel and time zones.* The log day follows the phone's local clock (with D-023), so
  abroad, days follow local time. Flying east, a date can be very short or skipped; it stays
  "not logged" unless filled in from History. Flying back, a date that happens twice is one day.
  Each day also saves the phone's time zone at the moment its record is created (e.g.
  `Asia/Seoul`), so later analysis can spot travel days and jet lag. Reminders follow local time.
  *Decided by Luca 2026-11-24 (was Q-007).* **Design side:** add `zone` to Spec: Data.
  *Spec updated 2026-11-24, design chat:* Data has `zone`.
- **D-026** *The sort indicator can be tapped.* One tap on it switches the sort mode, with the same
  light haptic and a press scale of 0.96. The double-tap on empty space still works too. This
  replaces "they cannot be tapped" in D-008. *Asked by Luca 2026-11-24.* **Design side:** update
  Spec: Today (item 4) and Sort modes.
  *Spec updated 2026-11-24, design chat:* both say so, and `Main.dc.html` switches on a tap. The
  touch area should be at least 44 dp tall although the capsule is drawn smaller.
- **D-027** *"Most recent" replaces "often used".* The second sort mode orders the chips by when
  each was last tapped (any tap that changes its value, on any day): the most recent last, bottom
  right; never-tapped items first, in category order. *When chips move:* the order is worked out
  each time Today comes on screen and then stays fixed while it is on screen, so a tapped chip
  stays where it is (Tea can be tapped twice in place). The new order shows the next time Today
  opens, with no animation. *Decided by Luca 2026-11-24.* → Spec: Sort modes, Data.
- **D-028** *Everything redrawn at 384 × 832 dp* (R-03). To keep Today and the past day on one
  screen at that width, Today is tighter, with the same look:
  - Chips have 12 dp side padding (was 14), so Food fits on two lines.
  - Gaps are 6 dp between lines of chips and 8 dp between groups.
  - The aphtha card is 72 dp tall (was 80).
  - The past day's banner is slimmer (13 sp, 6 × 12 dp padding) and sits 8 dp under its title.
  History, Month and Settings needed no change beyond the width. Checked by measuring the drawings
  at 384 × 832: Today and the past day fit in both sort modes. *Decided 2026-11-24.*
  → Spec: Shapes, Today, Chips, Past day.
- **D-029** *Launch screen.* Black, with the icon's ring and dot on a round background in the
  icon's tint, no text: the Android 12+ splash screen as built (Code's Design side note). Drawn in
  `design/Launch.dc.html`. *Added 2026-11-24, design chat.* → Spec: Launch screen.

---

*Code, 2026-11-24, build 4+2026-11-24.1506 (Revision 7):* everything decided for Today is built
and installed on Luca's phone. It was checked on an emulator at 384 × 832 dp, with screenshots
compared against `design/Main.dc.html` and `design/Launch.dc.html`. The items stay here until Luca
confirms them on the phone.
- D-027: most recent. `Data.kt` `byMostRecent`; `Item.lastTapped` (epoch ms, saved with the
  items); `Store.tapItem`; the order is kept in `Today.kt` `DayScreen` and worked out again on a tab
  switch, on coming back to the app, on switching into the mode and when the log day changes.
  Checked: a tapped chip stays in place, and the new order shows after History and back.
  Switching out of the mode keeps the order, so the list that is fading out doesn't reshuffle
  (Luca saw it jump); the order is worked out again only on switching back in.
- D-028: the tighter Today (12 dp chip padding, 6 dp between lines, 5 dp under group names, 8 dp
  between groups, 72 dp aphtha card with 12 × 14 dp padding, 72 dp badge chips). The top padding is
  the real status-bar inset, which is 40 dp on Luca's phone, as drawn. The past day banner comes
  with the past day screen. `Today.kt`, `MainActivity.kt`.
- D-017: icon tint `#1A1F29` (background and the gap around the dot). `res/drawable/`.
- D-029: launch screen. *How it differs:* it uses the platform's own Android 12+ attributes in
  `res/values-v31/themes.xml` instead of the AndroidX `Theme.SplashScreen` in `APP_ICON.md`, so no
  extra library is needed. It looks the same (black, ring and dot on a round `#1A1F29`
  background). Phones older than Android 12 show a plain black screen.
- D-026: the indicator's touch area is Compose's 48 dp minimum touch target around the capsule,
  so it is more than 44 dp tall although the capsule is drawn 24 dp tall.
- Built earlier, also waiting for Luca's check: D-001 to D-008 (Today and tab bar), D-016 (motion
  on Today), D-018 (data model; a saved day holds a value for every item shown that day, 0 when not
  ticked, so a missing key means hidden or not yet existing; `testable` set only for Raw
  apple/carrot), D-022 to D-025. History and Settings still show only their title row.

## Built
- *(none yet)*

---

## Superseded
- **D-030** Slightly larger text on Today (chips 16 sp, group names 13 sp). *Withdrawn 2026-11-24,
  Luca:* Today only looked roomy because his phone's font size was set to 0.9; he set it back
  to 1.0, and the drawn sizes stand. Checked at 1.0: category mode fits with about 30 dp to spare.
- **Q-004** *Often used ranking.* Code chose: taps over the 30 days before today, each day weighted
  `1 − (age − 1) / 60`, today left out, ties in reversed category order (`Data.kt`
  `byOftenUsed`). *Superseded 2026-11-24, Luca:* "often used" is replaced by "most recent"
  (D-027), so the weighting is no longer needed.
- **Q-001** Stack and storage. *Answered 2026-11-24, Luca:* any storage is fine as long as the app
  can read it back for the analysis. See D-022.
- **Q-005** How does a day with nothing to tick count as logged? *Answered 2026-11-24, Luca:* not
  a problem; he will always at least brush his teeth or drink tea. The record is created on
  the first tap, as built (D-018).
- **Q-006** Logging after midnight. *Answered 2026-11-24, Luca:* a 04:00 cut-off, like AnkiDroid.
  See D-023.
- **Q-007** Travel and time zones. *Answered 2026-11-24, Luca:* agreed with the suggestion; save
  the time zone with each day. See D-025.
- **Q-002** Is Insights part of v1? *Answered 2026-11-24, Luca:* no, later. Hide the tab in v1.
  See D-021.
- **Q-008** The icon's background tint: card grey `#1C1C1E`, lighter grey `#2C2C2E` or dark
  blue-grey `#1A1F29`. *Answered 2026-11-24, Luca:* dark blue-grey `#1A1F29`. See D-017.
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
- **S-008** "Often used" as the second sort mode (ranked by taps over 30 days). *Replaced
  2026-11-24 by "most recent" (D-027).*
