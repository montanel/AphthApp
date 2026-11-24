package ch.luca.aphthapp

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.roundToInt

/** "1 episode", "3 episodes": the noun is singular only for exactly one. */
private fun counted(n: Int, one: String, many: String) = if (n == 1) one else many

/** The History tab's main screen: three numbers and one strip per month of this year. */
@Composable
fun YearScreen(onOpenMonth: (YearMonth) -> Unit) {
    val today = Store.today
    val days = Store.days
    val eps = episodes(days)
    val streak = currentStreak(days, eps, today)
    val thisYear = eps.count { it.start.year == today.year }
    val gap = medianGap(eps)
    Column(Modifier.padding(horizontal = 14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TitleRow("History", today.year.toString())
        Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val tile = Modifier.weight(1f).fillMaxHeight()
            // A non-breaking hyphen keeps "aphtha-free" on one line.
            StatTile(
                streak?.first?.toString() ?: "–",
                when {
                    streak?.second == true -> counted(streak.first, "day into this episode", "days into this episode")
                    else -> counted(streak?.first ?: 0, "day aphtha‑free", "days aphtha‑free")
                },
                tile,
            )
            StatTile(thisYear.toString(), counted(thisYear, "episode this year", "episodes this year"), tile)
            StatTile(gap?.let { "~" + it.roundToInt() } ?: "–", "days between episodes", tile)
        }
        Column(
            Modifier.fillMaxWidth().background(C.card, RoundedCornerShape(20.dp)).padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            val current = YearMonth.from(today)
            for (m in 1..12) {
                val month = YearMonth.of(today.year, m)
                val (logged, aphtha) = monthStrip(month, days, eps)
                MonthRow(month, current, logged, aphtha) { onOpenMonth(month) }
            }
        }
    }
}

/**
 * One month of the year card: the name and a strip with the logged days in `hairline` and the
 * episodes in `red`. The whole row opens the month; future months can't be opened.
 */
@Composable
private fun MonthRow(month: YearMonth, current: YearMonth, logged: List<IntRange>, aphtha: List<IntRange>, onTap: () -> Unit) {
    val future = month.isAfter(current)
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val highlight by animateColorAsState(if (pressed) C.hairline else C.card.copy(alpha = 0f), ease(120), label = "rowPress")
    val name = month.month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
    Row(
        Modifier.fillMaxWidth().defaultMinSize(minHeight = 44.dp)
            .background(highlight, RoundedCornerShape(12.dp))
            .clickable(source, indication = null, enabled = !future, role = Role.Button,
                onClickLabel = month.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH), onClick = onTap)
            .padding(horizontal = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val color = when {
            month == current -> C.text
            future -> C.quiet
            else -> C.muted
        }
        BasicText(name, Modifier.width(30.dp), style = type(13.sp, FontWeight.SemiBold, color))
        val len = month.lengthOfMonth().toFloat()
        Canvas(Modifier.weight(1f).height(8.dp).clip(RoundedCornerShape(4.dp))) {
            val r = CornerRadius(4.dp.toPx())
            drawRoundRect(C.chipOff, cornerRadius = r)
            fun run(range: IntRange, color: Color) = drawRoundRect(
                color,
                topLeft = Offset(size.width * (range.first - 1) / len, 0f),
                size = Size(size.width * (range.last - range.first + 1) / len, size.height),
                cornerRadius = r,
            )
            logged.forEach { run(it, C.hairline) }
            aphtha.forEach { run(it, C.red) }
        }
    }
}

private val monthTitle = DateTimeFormatter.ofPattern("MMMM", Locale.ENGLISH)

/**
 * One month as a calendar; swiping left or right moves between months, from two years back (or
 * the first logged month, if earlier) to this month. `onMonthChange` reports the month on screen.
 */
@Composable
fun MonthScreen(
    month: YearMonth,
    onBack: () -> Unit,
    onMonthChange: (YearMonth) -> Unit,
    onOpenDay: (LocalDate) -> Unit,
    onToday: () -> Unit,
) {
    val today = Store.today
    val current = YearMonth.from(today)
    val firstLogged = Store.days.keys.minOrNull()?.let { YearMonth.from(it) } ?: current
    val first = minOf(firstLogged, current.minusMonths(24), month)
    val count = (current.year - first.year) * 12 + current.monthValue - first.monthValue + 1
    fun monthAt(page: Int): YearMonth = first.plusMonths(page.toLong())
    val pager = rememberPagerState(
        initialPage = ((month.year - first.year) * 12 + month.monthValue - first.monthValue).coerceIn(0, count - 1),
        pageCount = { count },
    )
    val shown = monthAt(pager.currentPage)
    LaunchedEffect(shown) { onMonthChange(shown) }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(Modifier.padding(horizontal = 14.dp)) {
            BackTitleRow(shown.format(monthTitle), shown.year.toString(), "Back to the year", onBack)
        }
        HorizontalPager(pager, Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top, beyondViewportPageCount = 1) { page ->
            MonthPage(monthAt(page), today, onOpenDay, onToday)
        }
    }
}

@Composable
private fun MonthPage(month: YearMonth, today: LocalDate, onOpenDay: (LocalDate) -> Unit, onToday: () -> Unit) {
    val days = Store.days
    val eps = episodesIn(episodes(days), month)
    val aphthaDays = (1..month.lengthOfMonth()).count { days[month.atDay(it)]?.aphtha == true }
    Column(Modifier.fillMaxSize().padding(horizontal = 14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatTile(aphthaDays.toString(), counted(aphthaDays, "aphtha day", "aphtha days"), Modifier.weight(1f), 13.sp, 16.dp)
            StatTile(eps.size.toString(), counted(eps.size, "episode", "episodes"), Modifier.weight(1f), 13.sp, 16.dp)
        }
        Column(
            Modifier.fillMaxWidth().background(C.card, RoundedCornerShape(20.dp)).padding(horizontal = 12.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                for (letter in listOf("M", "T", "W", "T", "F", "S", "S")) BasicText(
                    letter, Modifier.weight(1f),
                    style = type(12.sp, FontWeight.SemiBold, C.muted).copy(textAlign = TextAlign.Center),
                )
            }
            // Monday first: blank cells before the 1st.
            val lead = month.atDay(1).dayOfWeek.value - 1
            val cells = List(lead) { null } + (1..month.lengthOfMonth()).map { month.atDay(it) }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                for (week in cells.chunked(7)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        for (i in 0 until 7) {
                            val date = week.getOrNull(i)
                            if (date == null) Spacer(Modifier.weight(1f)) else DayCell(
                                date, days[date], today, Modifier.weight(1f),
                                onTap = { if (date == today) onToday() else onOpenDay(date) },
                            )
                        }
                    }
                }
            }
            Row(Modifier.padding(start = 6.dp, top = 6.dp, end = 6.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Legend("Aphtha") { Box(Modifier.size(12.dp).background(C.redSoft, CircleShape)) }
                Legend("Not logged") { Box(Modifier.size(12.dp).dashedOutline(C.quietLine)) }
            }
        }
    }
}

/** One day of the calendar: 46 dp tall and round, coloured by what was logged (SPEC section 11). */
@Composable
private fun DayCell(date: LocalDate, record: DayRecord?, today: LocalDate, modifier: Modifier, onTap: () -> Unit) {
    val future = date.isAfter(today)
    val shape = RoundedCornerShape(23.dp)
    val textColor = when {
        future -> C.quietLine
        record?.aphtha == true -> C.white
        record == null && date != today -> C.muted
        else -> C.text
    }
    Box(
        modifier.height(46.dp)
            .then(if (future) Modifier else Modifier.tap(0.92f, Role.Button, date.toString(), onTap))
            .then(if (record?.aphtha == true) Modifier.background(C.redSoft, shape) else Modifier)
            .then(
                when {
                    date == today -> Modifier.border(2.dp, C.text, shape)
                    record == null && !future -> Modifier.dashedOutline(C.quietLine)
                    else -> Modifier
                }
            ),
        contentAlignment = Alignment.Center,
    ) {
        BasicText(date.dayOfMonth.toString(), style = type(15.sp, FontWeight.SemiBold, textColor))
    }
}

@Composable
private fun Legend(label: String, swatch: @Composable () -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        swatch()
        BasicText(label, style = type(12.sp, FontWeight.Normal, C.muted))
    }
}
