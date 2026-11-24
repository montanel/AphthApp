# AphthApp — UI spec (v4, 2026-11-24)

What the app looks like and how it behaves. The design files in `design/` are the picture; this
file is the words. Where they disagree, ask (`DECISIONS.md`, Open questions). Decisions and their
reasons are in `DECISIONS.md`; this file says only what to build.

**Goal:** a quick evening log of what happened that day, plus whether an aphtha hurt, so patterns
can be found over months. Logging should take about 20 seconds.

**Screen size:** every design is drawn at **384 × 832 dp**, the size of Luca's Galaxy S26 Ultra at
its current display zoom (R-03).

---

## 1. Colours

| Token | Hex | Use |
|---|---|---|
| bg | `#000000` | every screen's background |
| card | `#1C1C1E` | cards, aphtha card (off), tab bar, back buttons |
| chipOff | `#111113` | unselected chip, sort indicator background, morning nudge card |
| chipOn | `#2F3846` | selected chip, selected tab, "Editing" banner, step numbers, badges on cards |
| hairline | `#2C2C2E` | row dividers, logged part of a year strip, unfilled progress |
| quietLine | `#3A3A3C` | dashed "not logged" outlines, "no tea" squares |
| ring | `#48484A` | the aphtha card's empty ring, inactive sort icon |
| text | `#F2F2F7` | main text (white `#FFFFFF` on selected chips) |
| muted | `#98989F` | secondary text, unselected chip text, unselected tabs, labels |
| quiet | `#636366` | the faintest text: chevrons, footnotes, the "don't know" outline |
| iconBg | `#1A1F29` | the app icon's background, and the round icon background on the launch screen (Q-008) |
| info | `#8FA3C0` | blue-grey accent: the app icon's ring, progress, "Can't tell" values |
| red | `#FF453A` | aphthas only: aphtha card on, episode strips in History, suspects in Insights, the app icon's dot; also "Delete all data" |
| redSoft | `#FF453A` at 28% opacity (on `card` it shows as about `#5C2826`) | aphtha days in the month calendar. It's the same red, toned down so a whole week of aphtha days isn't glaring |
| badge | `#F2F2F7` bg, `#000000` text, 2 dp black ring | count / stress badge on chips |

## 2. Type

Inter (bundle it, or use Google Fonts downloadable fonts). Sizes in sp:

| Size / weight | Use |
|---|---|
| 92 / 700 | lock-screen clock (notification mock only) |
| 30 / 800, tracking −3% | screen titles |
| 26 / 800 | big numbers (stats), verdict headline |
| 24 / 700 | "Aphtha" on the aphtha card |
| 18 / 700 | card titles |
| 15–16 / 500–600 | chips, rows, body text |
| 12–14 / 600 | labels, captions, small pills |
| 11 / 600 | tab labels, weekday letters, strip month names |
| 10.5 / 600 | the small line under a chip ("don't know") |

## 3. Shapes and spacing

- Screen padding: 14 dp left and right; 40 dp top in the drawings (use the real status-bar inset);
  content stops about 100 dp above the bottom so the floating tab bar never covers it.
- Gaps between blocks: 12 dp. Inside cards: 8 dp.
- Cards: 20 dp corners, padding 16–18 dp.
- Anything pill-shaped is fully round (corner = half its height): chips (44 dp tall), small pills
  (36 dp), tabs.
- Back buttons: 44 dp circles in `card`, a 20 dp chevron.
- Rows in a list card: 52 dp tall, 0.5 dp `hairline` divider, none under the last.
- Touch targets are never under 44 dp.

## 4. Shared components

- **Floating tab bar:** a short pill (`card`, 6 dp padding, 32 dp corners) centred 22 dp above the
  bottom inset. Each tab is ≥64 × 52 dp: a 20 dp line icon over an 11 sp label. The selected tab
  sits on a `chipOn` pill (26 dp corners); the others are `muted`. **v1 has three tabs: Today,
  History, Settings.** Insights joins between History and Settings when it is built (D-021).
- **Screen title row:** title left (30 sp), a small `muted` note right (the date, the year).
  Sub-screens put a back button before the title.
- **Stat tile:** a card with a 26 sp number and a 12 sp `muted` label under it (up to two lines).
- **Section label:** 12 sp `muted`, above a card, 4 dp in from the card's edge.

## 5. Today

Design: `design/Main.dc.html`. Fits on one screen. From top to bottom:

1. **Title row:** "Today", the date on the right ("Tue 10 Nov").
2. **Aphtha card:** full width, 72 dp tall, 20 dp corners. "Aphtha" on the left, a 46 dp ring on
   the right. Off: `card` with an empty `ring`. On: `red` fill, the ring becomes a white disc
   with a red check. Off every day by default. Tapping toggles it.
3. **List area:** the chips (section 6), in the current sort mode (section 7).
4. **Sort indicator:** two small icons (a grid for categories, a clock for most recent) in a
   `chipOff` capsule, centred just above the tab bar. The active one is on `hairline` with `text`
   colour; the other is `ring`. **One tap on it switches the mode** (D-026): light haptic, press
   scale 0.96. Its touch area is at least 44 dp tall, even though the capsule is drawn smaller.
5. **Tab bar**, Today selected.

**Which day "Today" is:** a log day runs from 04:00 to 04:00 on the phone's local clock (D-023,
D-025). Until 04:00, Today shows and saves to the previous date, and its title row shows that
date.

## 6. Chips

- A chip is a pill, 44 dp tall, 12 dp side padding, 15 sp / 600 text.
  - Unselected: `chipOff`, `muted` text.
  - Selected: `chipOn`, white text.
  - The weight never changes, so the width never changes.
- Laid out in a `FlowRow`, 6 dp apart side to side and between lines. Group name and its chips 5 dp
  apart, groups 8 dp apart.
- **Yes/no chips:** tap toggles.
- **Tea, Coffee (count):** each tap adds a cup, 0 → 1 → 2 → 3 → 4+ → 0. Selected when above 0. The
  count is a 20 dp badge on the chip's top-right corner (offset −5 dp, −4 dp), drawn over the
  edge, hidden at 0. Minimum width 72 dp.
- **Brushed (times):** 0 → 1 → 2 → 0, the number in the same badge, minimum width 72 dp (D-024).
- **Stress (level):** 0 → 1 → 2 → 0, with "•" or "••" in the same badge. Minimum width 72 dp.
- **SLS toothpaste (three states):** off (no SLS) → selected (SLS) → "don't know" → off.
  - "Don't know" is a transparent chip with a 1.5 dp dashed `quiet` outline drawn inside its
    edge, and a small "don't know" line (10.5 sp) under the name. Same width.
- Every tap is saved at once.

### Items (default order within each group)

| Group | Items |
|---|---|
| Drinks | Tea (count), Coffee (count), Alcohol |
| Food | Banana, Nuts, Chocolate, Raw apple/carrot, Spicy, Citrus/tomato |
| Mouth care | Brushed (0 to 2), Water floss, Floss, SLS toothpaste (three states) |
| Body | Bit mouth, Stress (level), Bad sleep, Feeling ill |
| Supplements | B complex |
| Hidden until switched on in Settings | Crunchy food (Food), Gum/mints (Food), Mouthwash (Mouth care), Dental visit (Body) |

## 7. Sort modes

- **By category:** the groups above in that order, each under a 12 sp `muted` group name. The list
  hugs the top of its area.
- **Most recent** (replaces "often used", D-027): one flat list, no group names, ordered by when
  each item was last tapped (any tap that changes its value, on any day). **The most recently
  tapped chip comes last, at the bottom right**, nearest the thumb; items never tapped keep the
  category order and come first. The list hugs the bottom of its area, just above the sort
  indicator.
  - **When chips move:** the order is worked out each time Today comes on screen (opening the app,
    coming back to it, switching to the Today tab) and is then fixed while Today stays on screen.
    Tapping a chip changes its time but does not move it, so tapping Tea twice never means chasing
    it. The new order shows the next time Today opens, drawn straight away with no animation.
  - Switching into this mode also works out the order at that moment.
- **Switching:** a double-tap anywhere between the aphtha card and the tab bar that isn't a chip
  (gaps, group names, empty space), or one tap on the sort indicator.
  - Compose: one container holding the list and the indicator, with
    `pointerInput { detectTapGestures(onDoubleTap = …) }`; the chips are `clickable` and keep
    their own taps.
  - Light haptic.
- The mode is remembered between launches.

## 8. Past day (editing from History)

Design: `design/PastDay.dc.html`. The Today screen for another date:
- **Title row:** a back button (to the month), and the date as the title ("Sun 8 Nov").
- **Banner:** under the title, a `chipOn` strip with a small pencil icon and "Editing Sunday 8
  November" (13 sp / 600, white, 12 dp corners, 6 × 12 dp padding). It sits 8 dp under the title
  row, so the past day still fits on one screen.
- **Contents:** the chips show what was logged that day, or nothing if it wasn't.
- **Tab bar:** History stays selected.

## 9. Notifications

Design: `design/Notification.dc.html` (a lock-screen mock).
- **Evening reminder:** at the time set in Settings (default 21:30). Title "How was today?", text
  "Takes 20 seconds. Tap to log." Opens Today.
- **Next-morning nudge:** at 09:00, only if the day before was not logged, and only if turned on in
  Settings. Title "Yesterday is still empty", text "Fill it in while you remember." Opens
  yesterday's log.
- The small icon is the app icon's ring and dot in white (Android shows it in one colour).

## 9b. Launch screen

Design: `design/Launch.dc.html`. Android 12+ `SplashScreen`: background `#000000`, the icon's
ring and dot centred, on a round icon background in the app icon's tint (`iconBg`,
`#1A1F29`). No text. Shown only as long as the system needs.

## 10. History

Design: `design/Calendar.dc.html`. Fits on one screen. From top to bottom:

1. **Title row:** "History", the year on the right.
2. **Three stat tiles in a row:**
   - Days aphtha-free since the last aphtha day. While an episode is on, show the episode's day
     count instead, labelled "days into this episode".
   - Episodes this year.
   - "days between episodes": the median gap between episode starts, shown as "~31".
3. **Year card** (`card`, 20 dp corners, 8 × 18 dp padding): one row per month, Jan to Dec. Each row
   is **one tap target**, 44 dp tall and the full width, that opens that month.
   - The row: the month's three letters (13 sp; `text` for the current month, `muted` for past
     months, `quiet` for future ones), then a strip.
   - The strip: 8 dp tall, 4 dp corners, on `chipOff`. The logged part of the month is
     `hairline`, and each episode is a `red` segment placed by day.
   - Pressing a row highlights it in `hairline` (12 dp corners).
4. **Tab bar**, History selected.

## 11. Month

Design: `design/Month.dc.html`.
1. **Title row:** a back button to the year, the month name as the title, the year on the right.
2. **Two stat tiles:** aphtha days this month, episodes this month.
3. **Calendar card:**
   - Weekday letters, Monday first.
   - Day cells 46 dp, round:
     - Logged: `text`.
     - Aphtha: `redSoft` with white text.
     - Not logged: 1.5 dp dashed `quietLine` outline, `muted` text.
     - Today: a 2 dp white ring.
     - Future: `quietLine` text, can't be tapped.
   - A legend under the grid: Aphtha, Not logged.
4. **Behaviour:** swiping left or right changes the month. Tapping a past day opens it (section 8).

## 12. Insights (later, not in v1)

**Not in v1 (D-021).** This section is here so the design isn't lost; don't build it yet. Designs:
`design/Insights.dc.html`, `design/Factor.dc.html`, `design/HowItWorks.dc.html`, drawn with the
four-tab bar they will have. These three screens may scroll.

**Insights**
- Title row with a small "How it works" pill on the right.
- A summary card: the number of episodes so far, a three-part progress bar (big, medium and small
  effects: the part that can already be seen is `info`) and one line of text.
- Four groups, each a list card. Each row has the item name, a short value and a chevron:
  - **Suspects:** value in `red`, e.g. "~2.6× risk".
  - **Can't tell from logs alone:** value in `info`, e.g. "Every day", "Never skipped", "Acts slowly".
  - **Too little data:** value in `muted`.
  - **Probably fine:** value in `muted`, e.g. "No sign".
- Tapping a row opens the item screen.

**Item screen (Tea)**
- Back button and the item name.
- A verdict card: a small `chipOn` pill, a 26 sp headline and one sentence.
- A grid of every logged day:
  - Tea days: `chipOn`.
  - No-tea days: outlined.
  - Episode starts: `red`.
- A card on what would settle it.

**How it works**
- Back button and title.
- Four numbered steps.
- Three short cards: when to say yes, why sore days are skipped, honest limits.
- A footnote saying it is not a diagnosis.

## 13. Settings

Design: `design/Settings.dc.html`. May scroll a little.

- **Reminders card:**
  - "Evening reminder" with the time on the right. A tap opens a time picker.
  - "Nudge next morning if I skip" with an On/Off chip.
- **What to track card:**
  - The same chips as Today, smaller (36 dp tall, 14 sp), grouped. A tap shows or hides the item
    on Today; hidden items keep their history.
  - The four hidden items (section 6) start off.
  - A dashed "+ Add your own" chip adds a yes/no item to a chosen group.
- **Data card:**
  - "Export my data" (CSV).
  - "Delete all data" in `red`, which asks for confirmation.

## 14. Motion (quick and subtle)

Easing everywhere: `FastOutSlowInEasing`. No bounce. Honour the system "Remove animations"
setting.

| What | How |
|---|---|
| Chip press | Scale to 0.96 while pressed (100 ms), back over 150 ms (`collectIsPressedAsState` → `animateFloatAsState` → `graphicsLayer`). |
| Chip on/off | Background and text colour, `animateColorAsState(tween(150))`. |
| Count change | The badge's number changes with `AnimatedContent` (fade + 4 dp up, 150 ms); the badge appears and leaves with scale 0.6→1 + fade (150 ms). |
| Aphtha card | Colour 200 ms, press scale 0.98, a confirm haptic when it turns on (`HapticFeedbackConstants.CONFIRM`, API 30+). |
| Tab bar | The selected pill slides to the new tab (`animateDpAsState`, 250 ms); icon and label colours fade over 200 ms; screens cross-fade over 200 ms. |
| Sort switch | The list cross-fades (out 100 ms, in 150 ms); the indicator's highlight slides between the two icons (200 ms); light haptic. |
| Year row press | Row highlight in 120 ms. |
| Month change | Slide left or right with a fade (250 ms). Day press scale 0.92. |

## 15. Data

- **One record per day:** `date`, `aphtha: Boolean`, and `values: Map<itemKey, Int>`.
  - Yes/no: 0/1.
  - Count: cups.
  - Stress: 0–2.
  - SLS: 0 no, 1 yes, 2 don't know.
- **Raw values only.** Summaries are computed, never stored.
- **No record = "not logged".** That is different from a logged day with nothing ticked.
- **Items:** `key`, `label`, `group`, `type`, `visible`, `order`, and `testable` (remove only, add or
  remove, watch only; unused for now, for later tests). Raw apple/carrot is watch only.
- **Brushed:** 0, 1 or 2 times (D-024).
- **Log day:** runs from 04:00 to 04:00 local time (D-023).
- **Time zone:** each day saves `zone`, the phone's time zone when its record is created (e.g.
  `Asia/Seoul`) (D-025).
- **Most recent order:** needs, for each item, the time it was last tapped. Keep it with the items
  (it is not daily data).
- **Episode:** a run of aphtha days. Runs separated by one or two no days are the same episode.
  The first day is the episode's start.
- **Export:** CSV, one row per day, one column per item.
- **Storage:** local, plus Android Auto Backup.
