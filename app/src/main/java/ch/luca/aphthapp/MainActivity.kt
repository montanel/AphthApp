package ch.luca.aphthapp

import android.graphics.Color.TRANSPARENT
import android.os.Bundle
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
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(SystemBarStyle.dark(TRANSPARENT), SystemBarStyle.dark(TRANSPARENT))
        super.onCreate(savedInstanceState)
        Store.load(applicationContext)
        setContent { App() }
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

private val TAB_WIDTH = 64.dp
private val TAB_GAP = 4.dp

@Composable
private fun App() {
    var tab by rememberSaveable { mutableStateOf(Tab.TODAY) }
    // Moves Today to the next day at 04:00 even while the app stays open.
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
    // The tab bar is 64 dp tall and floats 22 dp above the navigation bar.
    val bottomGap = navBar + 86.dp
    Box(Modifier.fillMaxSize().background(C.bg)) {
        Crossfade(tab, Modifier.fillMaxSize().padding(top = top), ease(200), label = "screen") { t ->
            when (t) {
                Tab.TODAY -> DayScreen(Store.today, bottomGap)
                Tab.HISTORY -> Placeholder("History", Store.today.year.toString())
                Tab.SETTINGS -> Placeholder("Settings", "")
            }
        }
        TabBar(tab, { tab = it }, Modifier.align(Alignment.BottomCenter).padding(bottom = navBar + 22.dp))
    }
}

/** A screen that is not built yet: only its title row. */
@Composable
private fun Placeholder(title: String, note: String) {
    Column(Modifier.padding(horizontal = 14.dp)) { TitleRow(title, note) }
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
