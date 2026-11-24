package ch.luca.aphthapp

import android.graphics.Color.TRANSPARENT
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.unit.Dp
import java.time.LocalDate
import java.time.YearMonth
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    private val askNotifications = registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(SystemBarStyle.dark(TRANSPARENT), SystemBarStyle.dark(TRANSPARENT))
        super.onCreate(savedInstanceState)
        Store.load(applicationContext)
        Reminders.schedule(applicationContext)
        // Android shows its own prompt; after two refusals it stops asking.
        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) askNotifications.launch(Manifest.permission.POST_NOTIFICATIONS)
        if (savedInstanceState == null) handle(intent)
        setContent { App() }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handle(intent)
    }

    /** Opens what a notification asks for: Today, or a past day. */
    private fun handle(intent: Intent) {
        val day = intent.getStringExtra(Reminders.EXTRA_DAY)
        when {
            day != null -> Nav.openDay(LocalDate.parse(day))
            intent.hasExtra(Reminders.EXTRA_TODAY) -> Nav.select(Tab.TODAY)
        }
    }

    override fun onResume() {
        super.onResume()
        Store.onResume()
    }
}

/** The three v1 tabs (DECISIONS D-002, D-021). */
private enum class Tab(val label: String, val icon: ImageVector) {
    TODAY("Today", Icons.today), HISTORY("History", Icons.history), SETTINGS("Settings", Icons.settings)
}

/** Where the app is: the tab, and in History the month and the past day that are open, if any. */
private object Nav {
    var tab by mutableStateOf(Tab.TODAY)
    var month by mutableStateOf<YearMonth?>(null)
    var pastDay by mutableStateOf<LocalDate?>(null)

    /** Selects a tab; selecting History again goes back to the year. */
    fun select(t: Tab) {
        if (t == tab && t == Tab.HISTORY) {
            month = null
            pastDay = null
        }
        tab = t
    }

    /** Opens `day` as a past day, with its month behind it. */
    fun openDay(day: LocalDate) {
        tab = Tab.HISTORY
        month = YearMonth.from(day)
        pastDay = day
    }

    fun back() {
        if (pastDay != null) pastDay = null else month = null
    }
}

private val TAB_WIDTH = 64.dp
private val TAB_GAP = 4.dp

@Composable
private fun App() {
    // Moves Today to the next log day at the cutoff (DAY_START_HOUR) even while the app stays open.
    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000)
            Store.refreshToday()
        }
    }
    val statusBar = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBar = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    // The drawings leave 40 dp for the status bar; the real inset is used, with a floor for
    // the rare case of no status bar.
    val top = max(24.dp, statusBar)
    // The tab bar is 64 dp tall and floats 22 dp above the bottom of the screen, as drawn, but
    // always at least 8 dp clear of the navigation bar (the gesture area).
    val tabBarBottom = max(22.dp, navBar + 8.dp)
    val bottomGap = tabBarBottom + 64.dp
    Box(Modifier.fillMaxSize().background(C.bg)) {
        Crossfade(Nav.tab, Modifier.fillMaxSize().padding(top = top), ease(200), label = "screen") { t ->
            when (t) {
                Tab.TODAY -> DayScreen(Store.today, bottomGap)
                Tab.HISTORY -> HistoryTab(bottomGap)
                Tab.SETTINGS -> SettingsScreen(bottomGap)
            }
        }
        TabBar(Nav.tab, Nav::select, Modifier.align(Alignment.BottomCenter).padding(bottom = tabBarBottom))
    }
    BackHandler(enabled = Nav.tab == Tab.HISTORY && (Nav.month != null || Nav.pastDay != null)) { Nav.back() }
}

/** The History tab: the year, a month, or a past day, cross-fading between them. */
@Composable
private fun HistoryTab(bottomGap: Dp) {
    val level = when {
        Nav.pastDay != null -> 2
        Nav.month != null -> 1
        else -> 0
    }
    Crossfade(level, animationSpec = ease(200), label = "history") { l ->
        when (l) {
            // Each screen keeps the date it opened with, so it can fade out after Nav moves on.
            2 -> {
                val day = remember { Nav.pastDay!! }
                DayScreen(day, bottomGap, onBack = { Nav.pastDay = null })
            }
            1 -> {
                val month = remember { Nav.month!! }
                MonthScreen(
                    month,
                    onBack = { Nav.month = null },
                    onMonthChange = { Nav.month = it },
                    onOpenDay = { Nav.pastDay = it },
                    onToday = { Nav.select(Tab.TODAY) },
                )
            }
            else -> YearScreen { Nav.month = it }
        }
    }
}

@Composable
private fun TabBar(selected: Tab, onSelect: (Tab) -> Unit, modifier: Modifier) {
    val pillX by animateDpAsState((TAB_WIDTH + TAB_GAP) * selected.ordinal, ease(250), label = "pill")
    Box(modifier.background(C.card, RoundedCornerShape(32.dp)).padding(6.dp)) {
        Box(Modifier.offset(x = pillX).size(TAB_WIDTH, 52.dp).background(C.chipOn, RoundedCornerShape(26.dp)))
        Row(horizontalArrangement = Arrangement.spacedBy(TAB_GAP)) {
            for (t in Tab.entries) TabButton(t, t == selected) { onSelect(t) }
        }
    }
}

@Composable
private fun TabButton(tab: Tab, selected: Boolean, onTap: () -> Unit) {
    val color by animateColorAsState(if (selected) C.white else C.muted, ease(200), label = "tabColor")
    Column(
        Modifier.size(TAB_WIDTH, 52.dp)
            .clickable(remember { MutableInteractionSource() }, indication = null, onClick = onTap),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp, Alignment.CenterVertically),
    ) {
        Image(rememberVectorPainter(tab.icon), null, Modifier.size(20.dp), colorFilter = ColorFilter.tint(color))
        BasicText(tab.label, style = type(11.sp, FontWeight.SemiBold, color))
    }
}
