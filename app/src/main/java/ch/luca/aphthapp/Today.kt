package ch.luca.aphthapp

import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val shortDate = DateTimeFormatter.ofPattern("EEE d MMM", Locale.ENGLISH)

private val longDate = DateTimeFormatter.ofPattern("EEEE d MMMM", Locale.ENGLISH)

/**
 * The log for one day: title row, aphtha card, the chips and the sort indicator. It fills the
 * space above the tab bar, `bottomGap` being the height the tab bar takes. With `onBack` it is
 * a past day opened from History: a back button, the date as the title and an "Editing" banner.
 */
@Composable
fun DayScreen(date: LocalDate, bottomGap: Dp, onBack: (() -> Unit)? = null) {
    val record = Store.days[date] ?: DayRecord()
    val view = LocalView.current
    // The "most recent" order is worked out when Today comes on screen (a tab switch recreates
    // it), when the app comes back to the front, and when switching into the mode; it is then
    // fixed, so tapped chips stay put. Switching out of the mode leaves it alone, so the list
    // that is fading out doesn't reshuffle.
    var recentOrder by remember(date, Store.resumes) { mutableStateOf(byMostRecent(Store.items)) }
    val switchSort = {
        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
        if (!Store.sortRecent) recentOrder = byMostRecent(Store.items)
        Store.switchSort()
    }
    Column(Modifier.fillMaxSize().padding(bottom = bottomGap)) {
        Column(Modifier.padding(horizontal = 14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (onBack == null) {
                TitleRow("Today", date.format(shortDate))
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    BackTitleRow(date.format(shortDate), "", "Back to " + date.month.getDisplayName(java.time.format.TextStyle.FULL, Locale.ENGLISH), onBack)
                    EditingBanner(date)
                }
            }
            AphthaCard(record.aphtha) {
                val on = !record.aphtha
                if (on) view.performHapticFeedback(
                    if (Build.VERSION.SDK_INT >= 30) HapticFeedbackConstants.CONFIRM
                    else HapticFeedbackConstants.VIRTUAL_KEY
                )
                Store.updateDay(date) { it.copy(aphtha = on) }
            }
        }
        // A double-tap anywhere between the aphtha card and the tab bar that is not a chip, or a
        // single tap on the sort indicator, switches the sort.
        Column(
            Modifier.weight(1f).fillMaxWidth().pointerInput(Unit) {
                detectTapGestures(onDoubleTap = { switchSort() })
            }
        ) {
            AnimatedContent(
                targetState = Store.sortRecent,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                transitionSpec = { fadeIn(ease(150, delay = 100)) togetherWith fadeOut(ease(100)) },
                label = "sort",
            ) { recent ->
                ChipList(recent, recentOrder, record) { item -> Store.tapItem(date, item) }
            }
            SortIndicator(Store.sortRecent, switchSort, Modifier.align(Alignment.CenterHorizontally).padding(bottom = 6.dp))
        }
    }
}

/** The blue-grey strip under a past day's title: a pencil and "Editing Sunday 8 November". */
@Composable
private fun EditingBanner(date: LocalDate) {
    Row(
        Modifier.fillMaxWidth().background(C.chipOn, RoundedCornerShape(12.dp)).padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(rememberVectorPainter(Icons.pencil), null, Modifier.size(16.dp), colorFilter = ColorFilter.tint(C.white))
        BasicText("Editing " + date.format(longDate), style = type(13.sp, FontWeight.SemiBold, C.white))
    }
}

@Composable
private fun AphthaCard(on: Boolean, onTap: () -> Unit) {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.98f else 1f, ease(if (pressed) 100 else 150), label = "aphScale")
    val bg by animateColorAsState(if (on) C.red else C.card, ease(200), label = "aphBg")
    val disc by animateColorAsState(if (on) C.white else C.white.copy(alpha = 0f), ease(200), label = "aphDisc")
    val ringColor by animateColorAsState(if (on) C.ring.copy(alpha = 0f) else C.ring, ease(200), label = "aphRing")
    val check by animateColorAsState(if (on) C.red else C.red.copy(alpha = 0f), ease(200), label = "aphCheck")
    Row(
        Modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .fillMaxWidth()
            .defaultMinSize(minHeight = 72.dp)
            .background(bg, RoundedCornerShape(20.dp))
            .clickable(source, indication = null, onClick = onTap)
            .padding(start = 20.dp, top = 12.dp, end = 14.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BasicText("Aphtha", style = type(24.sp, FontWeight.Bold, C.white, tracking = (-0.02).em))
        Box(
            Modifier.size(46.dp).background(disc, CircleShape).border(2.5.dp, ringColor, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Image(rememberVectorPainter(Icons.check), null, Modifier.size(26.dp), colorFilter = ColorFilter.tint(check))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChipList(recent: Boolean, recentOrder: List<Item>, record: DayRecord, onTap: (Item) -> Unit) {
    val sections = if (recent) listOf("" to recentOrder) else byCategory(Store.items)
    Column(
        Modifier.fillMaxSize().padding(start = 14.dp, top = 10.dp, end = 14.dp, bottom = 6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp, if (recent) Alignment.Bottom else Alignment.Top),
    ) {
        for ((name, items) in sections) {
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                if (name.isNotEmpty()) BasicText(
                    name, Modifier.padding(horizontal = 4.dp),
                    style = type(12.sp, FontWeight.SemiBold, C.muted, lineHeight = 14.sp),
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    for (item in items) Chip(item, record.values[item.key] ?: 0) { onTap(item) }
                }
            }
        }
    }
}

/** A chip for one item, showing `value` as SPEC.md section 6 describes. */
@Composable
private fun Chip(item: Item, value: Int, onTap: () -> Unit) {
    val unsure = item.type == ItemType.THREE_STATE && value == 2
    val on = value > 0 && !unsure
    val badge = when {
        value == 0 -> ""
        item.type == ItemType.COUNT -> if (value >= 4) "4+" else value.toString()
        item.type == ItemType.TIMES -> value.toString()
        item.type == ItemType.LEVEL -> if (value == 2) "••" else "•"
        else -> ""
    }
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.96f else 1f, ease(if (pressed) 100 else 150), label = "chipScale")
    val bg by animateColorAsState(
        when {
            unsure -> C.chipOff.copy(alpha = 0f)
            on -> C.chipOn
            else -> C.chipOff
        }, ease(150), label = "chipBg",
    )
    val fg by animateColorAsState(
        when {
            unsure -> C.text
            on -> C.white
            else -> C.muted
        }, ease(150), label = "chipFg",
    )
    val fixedWidth = item.type == ItemType.COUNT || item.type == ItemType.TIMES || item.type == ItemType.LEVEL
    Box(Modifier.graphicsLayer { scaleX = scale; scaleY = scale }) {
        Column(
            Modifier
                .defaultMinSize(minWidth = if (fixedWidth) 72.dp else Dp.Unspecified, minHeight = 44.dp)
                .background(bg, CircleShape)
                .then(if (unsure) Modifier.dashedOutline(C.quiet) else Modifier)
                .clickable(source, indication = null, onClick = onTap)
                .padding(horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            BasicText(item.label, style = type(15.sp, FontWeight.SemiBold, fg, lineHeight = 1.15.em))
            if (unsure) BasicText(
                "don’t know", Modifier.padding(top = 1.dp),
                style = type(10.5.sp, FontWeight.SemiBold, fg.copy(alpha = 0.75f), lineHeight = 1.15.em),
            )
        }
        Badge(badge, Modifier.align(Alignment.TopEnd))
    }
}

/** The white count / level badge on a chip's top-right corner, with a 2 dp black ring. */
@Composable
private fun Badge(text: String, modifier: Modifier) {
    // Keep showing the last text while the badge fades out.
    val last = remember { mutableStateOf(text) }
    if (text.isNotEmpty()) last.value = text
    AnimatedVisibility(
        visible = text.isNotEmpty(),
        // The ring sits outside the 20 dp badge, so the offsets are 2 dp larger than the spec's.
        modifier = modifier.offset(x = 6.dp, y = (-7).dp),
        enter = scaleIn(ease(150), initialScale = 0.6f) + fadeIn(ease(150)),
        exit = scaleOut(ease(150), targetScale = 0.6f) + fadeOut(ease(150)),
        label = "badge",
    ) {
        Box(Modifier.background(C.bg, CircleShape).padding(2.dp)) {
            AnimatedContent(
                targetState = last.value,
                transitionSpec = {
                    (fadeIn(ease(150)) + slideInVertically(ease(150)) { it / 5 }) togetherWith fadeOut(ease(150))
                },
                label = "badgeText",
            ) { t ->
                Box(
                    Modifier.defaultMinSize(minWidth = 20.dp).height(20.dp)
                        .background(C.text, RoundedCornerShape(10.dp)).padding(horizontal = 5.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(t, style = type(11.sp, FontWeight.ExtraBold, C.bg))
                }
            }
        }
    }
}

/**
 * Two small icons showing the sort mode; the highlight slides to the active one. A tap switches
 * the mode. Compose widens its touch area to the 48 dp minimum where nothing else is under the
 * finger, so the small capsule is still easy to hit.
 */
@Composable
private fun SortIndicator(recent: Boolean, onTap: () -> Unit, modifier: Modifier) {
    val x by animateDpAsState(if (recent) 28.dp else 0.dp, ease(200), label = "sortX")
    val catColor by animateColorAsState(if (recent) C.ring else C.text, ease(200), label = "catColor")
    val recentColor by animateColorAsState(if (recent) C.text else C.ring, ease(200), label = "recentColor")
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.96f else 1f, ease(if (pressed) 100 else 150), label = "sortScale")
    Box(
        modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .background(C.chipOff, RoundedCornerShape(12.dp))
            .clickable(
                source, indication = null, role = Role.Button,
                onClickLabel = if (recent) "Sort by category" else "Sort by most recent", onClick = onTap,
            )
            .padding(2.dp)
    ) {
        Box(Modifier.offset(x = x).size(26.dp, 20.dp).background(C.hairline, RoundedCornerShape(10.dp)))
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            IndicatorIcon(Icons.grid, catColor)
            IndicatorIcon(Icons.recent, recentColor)
        }
    }
}

@Composable
private fun IndicatorIcon(icon: ImageVector, color: androidx.compose.ui.graphics.Color) {
    Box(Modifier.size(26.dp, 20.dp), contentAlignment = Alignment.Center) {
        Image(rememberVectorPainter(icon), null, Modifier.size(14.dp), colorFilter = ColorFilter.tint(color))
    }
}
