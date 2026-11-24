package ch.luca.aphthapp

import android.app.TimePickerDialog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.time.LocalDate

/** Settings (SPEC section 13): reminders, what to track, and the data. It scrolls. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(bottomGap: Dp) {
    val context = LocalContext.current
    var adding by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf(false) }
    val export = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        if (uri != null) context.contentResolver.openOutputStream(uri)?.use {
            it.write(toCsv(Store.items, Store.days).encodeToByteArray())
        }
    }
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(start = 14.dp, end = 14.dp, bottom = bottomGap + 14.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        TitleRow("Settings", "")
        Section("Reminders") {
            ListCard {
                val m = Store.eveningMinutes
                CardRow("Evening reminder", divider = true, onClick = {
                    TimePickerDialog(
                        context, R.style.AphthApp_TimePicker,
                        { _, h, min -> Store.setEvening(h * 60 + min); Reminders.schedule(context) },
                        m / 60, m % 60, true,
                    ).show()
                }) {
                    BasicText("%02d:%02d".format(m / 60, m % 60), style = type(16.sp, FontWeight.Bold))
                }
                CardRow("Nudge next morning if I skip", divider = false) {
                    SmallChip(if (Store.nudge) "On" else "Off", Store.nudge, minWidth = 60.dp) {
                        Store.switchNudge()
                        Reminders.schedule(context)
                    }
                }
            }
        }
        Section("What to track", "Tap to show or hide on Today") {
            Column(
                Modifier.fillMaxWidth().background(C.card, RoundedCornerShape(20.dp)).padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                for ((group, items) in byCategory(Store.items, includeHidden = true)) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        BasicText(group, Modifier.padding(horizontal = 2.dp), style = type(11.sp, FontWeight.SemiBold, C.quiet))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            for (item in items) SmallChip(item.label, item.visible) { Store.setVisible(item.key, !item.visible) }
                        }
                    }
                }
                Box(
                    Modifier.defaultMinSize(minHeight = 36.dp).tap(0.96f, Role.Button) { adding = true }
                        .dashedOutline(C.quietLine).padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText("+ Add your own", style = type(14.sp, FontWeight.SemiBold, C.muted))
                }
            }
        }
        Section("Data") {
            ListCard {
                CardRow("Export my data", divider = true, onClick = { export.launch("aphthapp-${LocalDate.now()}.csv") }) {
                    BasicText("CSV", style = type(14.sp, FontWeight.Normal, C.muted))
                }
                CardRow("Delete all data", divider = false, labelColor = C.red, onClick = { deleting = true })
            }
        }
    }
    if (adding) AddItemDialog(onDismiss = { adding = false }) { label, group ->
        Store.addItem(label, group)
        adding = false
    }
    if (deleting) ConfirmDialog(
        title = "Delete all data?",
        text = "Every logged day is deleted, and can't be brought back. What you track and your reminders stay.",
        action = "Delete",
        actionColor = C.red,
        onDismiss = { deleting = false },
    ) {
        Store.deleteAll()
        deleting = false
    }
}

@Composable
private fun Section(label: String, note: String = "", content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionLabel(label, note)
        content()
    }
}

/** A 36 dp chip for Settings: `chipOn` with white text when on, `chipOff` with muted text when off. */
@Composable
fun SmallChip(label: String, on: Boolean, minWidth: Dp = Dp.Unspecified, onTap: () -> Unit) {
    val bg by animateColorAsState(if (on) C.chipOn else C.chipOff, ease(150), label = "smallBg")
    val fg by animateColorAsState(if (on) C.white else C.muted, ease(150), label = "smallFg")
    Box(
        Modifier.defaultMinSize(minWidth = minWidth, minHeight = 36.dp).tap(0.96f, Role.Button, onClick = onTap)
            .background(bg, CircleShape).padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        BasicText(label, style = type(14.sp, FontWeight.SemiBold, fg))
    }
}

/** A dialog card in the app's style: title, body, then Cancel and an action button on the right. */
@Composable
private fun DialogCard(
    title: String,
    action: String,
    actionColor: Color,
    actionEnabled: Boolean,
    onDismiss: () -> Unit,
    onAction: () -> Unit,
    body: @Composable () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            Modifier.fillMaxWidth().background(C.card, RoundedCornerShape(20.dp)).padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            BasicText(title, style = type(18.sp, FontWeight.Bold))
            body()
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)) {
                DialogButton("Cancel", C.muted, C.chipOff, true, onDismiss)
                DialogButton(action, actionColor, C.chipOn, actionEnabled, onAction)
            }
        }
    }
}

@Composable
private fun DialogButton(label: String, color: Color, bg: Color, enabled: Boolean, onTap: () -> Unit) {
    Box(
        Modifier.defaultMinSize(minWidth = 72.dp, minHeight = 44.dp)
            .then(if (enabled) Modifier.tap(0.96f, Role.Button, onClick = onTap) else Modifier)
            .background(bg, CircleShape).padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        BasicText(label, style = type(15.sp, FontWeight.SemiBold, if (enabled) color else C.quiet))
    }
}

@Composable
private fun ConfirmDialog(
    title: String, text: String, action: String, actionColor: Color, onDismiss: () -> Unit, onConfirm: () -> Unit,
) = DialogCard(title, action, actionColor, true, onDismiss, onConfirm) {
    BasicText(text, style = type(15.sp, FontWeight.Normal, C.muted, lineHeight = 21.sp))
}

/** "Add your own": a name and a group for a new yes/no item. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddItemDialog(onDismiss: () -> Unit, onAdd: (String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var group by remember { mutableStateOf(GROUPS[1]) }
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { focus.requestFocus() }
    DialogCard("Add your own", "Add", C.white, name.isNotBlank(), onDismiss, { onAdd(name.trim(), group) }) {
        Box(
            Modifier.fillMaxWidth().height(44.dp).background(C.chipOff, CircleShape).padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            if (name.isEmpty()) BasicText("Name, e.g. Orange juice", style = type(15.sp, FontWeight.Medium, C.quiet))
            BasicTextField(
                name, { name = it.take(30) }, Modifier.fillMaxWidth().focusRequester(focus),
                singleLine = true, textStyle = type(15.sp, FontWeight.Medium), cursorBrush = SolidColor(C.info),
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            BasicText("Group", Modifier.padding(horizontal = 2.dp), style = type(11.sp, FontWeight.SemiBold, C.quiet))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                for (g in GROUPS) SmallChip(g, g == group) { group = g }
            }
        }
    }
}
