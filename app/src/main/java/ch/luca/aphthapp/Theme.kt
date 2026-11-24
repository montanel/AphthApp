package ch.luca.aphthapp

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

/** The colour tokens from SPEC.md section 1. */
object C {
    val bg = Color(0xFF000000)
    val card = Color(0xFF1C1C1E)
    val chipOff = Color(0xFF111113)
    val chipOn = Color(0xFF2F3846)
    val hairline = Color(0xFF2C2C2E)
    val quietLine = Color(0xFF3A3A3C)
    val ring = Color(0xFF48484A)
    val text = Color(0xFFF2F2F7)
    val white = Color(0xFFFFFFFF)
    val muted = Color(0xFF98989F)
    val quiet = Color(0xFF636366)
    val info = Color(0xFF8FA3C0)
    val red = Color(0xFFFF453A)
    val redSoft = Color(0x47FF453A)
}

val Inter = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
    Font(R.font.inter_bold, FontWeight.Bold),
    Font(R.font.inter_extrabold, FontWeight.ExtraBold),
)

/** An Inter text style. */
fun type(
    size: TextUnit,
    weight: FontWeight,
    color: Color = C.text,
    tracking: TextUnit = TextUnit.Unspecified,
    lineHeight: TextUnit = TextUnit.Unspecified,
) = TextStyle(
    fontFamily = Inter, fontSize = size, fontWeight = weight, color = color,
    letterSpacing = tracking, lineHeight = lineHeight,
)

/** The easing used for every animation (SPEC.md section 14). */
fun <T> ease(ms: Int, delay: Int = 0): TweenSpec<T> = tween(ms, delay, FastOutSlowInEasing)

/**
 * Makes an element tappable without a ripple, shrinking it to `scaleTo` while pressed (100 ms in,
 * 150 ms back). Put it before `background` so the whole element scales.
 */
@Composable
fun Modifier.tap(scaleTo: Float = 1f, role: Role? = null, label: String? = null, onClick: () -> Unit): Modifier {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) scaleTo else 1f, ease(if (pressed) 100 else 150), label = "press")
    return graphicsLayer { scaleX = scale; scaleY = scale }
        .clickable(source, indication = null, role = role, onClickLabel = label, onClick = onClick)
}

/** A 1.5 dp dashed outline drawn inside the element's edge, with fully round ends. */
fun Modifier.dashedOutline(color: Color) = drawBehind {
    val w = 1.5.dp.toPx()
    drawRoundRect(
        color,
        topLeft = Offset(w / 2, w / 2),
        size = Size(size.width - w, size.height - w),
        cornerRadius = CornerRadius((minOf(size.width, size.height) - w) / 2),
        style = Stroke(w, pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 3.dp.toPx()))),
    )
}

private val titleStyle = type(30.sp, FontWeight.ExtraBold, tracking = (-0.03).em)

/** Title row: a 30 sp title on the left and a small muted note on the right. */
@Composable
fun TitleRow(title: String, note: String, modifier: Modifier = Modifier) {
    Row(
        modifier.fillMaxWidth().defaultMinSize(minHeight = 36.dp).padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        BasicText(title, Modifier.alignByBaseline(), style = titleStyle)
        BasicText(note, Modifier.alignByBaseline(), style = type(15.sp, FontWeight.Normal, C.muted))
    }
}

/** Title row of a sub-screen: a back button, the title, and an optional muted note on the right. */
@Composable
fun BackTitleRow(title: String, note: String, backLabel: String, onBack: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().defaultMinSize(minHeight = 36.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(44.dp).tap(0.96f, Role.Button, backLabel, onBack).background(C.card, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Image(rememberVectorPainter(Icons.back), backLabel, Modifier.size(20.dp), colorFilter = ColorFilter.tint(C.text))
        }
        BasicText(title, Modifier.weight(1f), style = titleStyle, maxLines = 1)
        if (note.isNotEmpty()) BasicText(note, style = type(15.sp, FontWeight.Normal, C.muted))
    }
}

/** A stat tile: a card with a 26 sp number over a muted label. */
@Composable
fun StatTile(number: String, label: String, modifier: Modifier, labelSize: TextUnit = 12.sp, padX: Dp = 14.dp) {
    Column(
        modifier.background(C.card, RoundedCornerShape(20.dp)).padding(horizontal = padX, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        BasicText(number, style = type(26.sp, FontWeight.ExtraBold, tracking = (-0.03).em))
        BasicText(label, style = type(labelSize, FontWeight.Normal, C.muted, lineHeight = 1.3.em))
    }
}

/** A 12 sp muted section label above a card, with an optional fainter note on the right. */
@Composable
fun SectionLabel(text: String, note: String = "") {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        BasicText(text, Modifier.alignByBaseline(), style = type(12.sp, FontWeight.SemiBold, C.muted))
        if (note.isNotEmpty()) BasicText(note, Modifier.alignByBaseline(), style = type(12.sp, FontWeight.Normal, C.quiet))
    }
}

/** A list card: rows separated by 0.5 dp hairlines, none under the last. */
@Composable
fun ListCard(content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(C.card), content = content)
}

/** A 52 dp row in a [ListCard]: `label` on the left, `trailing` on the right. */
@Composable
fun CardRow(
    label: String,
    divider: Boolean,
    labelColor: Color = C.text,
    onClick: (() -> Unit)? = null,
    trailing: @Composable () -> Unit = {},
) {
    Row(
        Modifier.fillMaxWidth().defaultMinSize(minHeight = 52.dp)
            .then(if (onClick != null) Modifier.tap(onClick = onClick) else Modifier)
            .drawBehind {
                if (divider) drawRect(C.hairline, Offset(0f, size.height - 0.5.dp.toPx()), Size(size.width, 0.5.dp.toPx()))
            }
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BasicText(label, Modifier.weight(1f), style = type(16.sp, FontWeight.Medium, labelColor))
        trailing()
    }
}

// Line icons on a 24-unit grid, drawn with a round-capped stroke in the content colour.

private fun rect(x: Float, y: Float, w: Float, h: Float, r: Float) =
    "M${x + r},${y}h${w - 2 * r}a$r,$r 0 0 1 $r,${r}v${h - 2 * r}a$r,$r 0 0 1 -$r,${r}" +
        "h${-(w - 2 * r)}a$r,$r 0 0 1 -$r,-${r}v${-(h - 2 * r)}a$r,$r 0 0 1 $r,-${r}z"

private fun circle(cx: Float, cy: Float, r: Float) =
    "M${cx - r},${cy}a$r,$r 0 1 0 ${2 * r},0a$r,$r 0 1 0 -${2 * r},0z"

private fun lineIcon(name: String, stroke: Float, vararg paths: String): ImageVector =
    ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f).apply {
        for (p in paths) addPath(
            pathData = addPathNodes(p),
            stroke = SolidColor(Color.White), strokeLineWidth = stroke,
            strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round,
        )
    }.build()

object Icons {
    val today = lineIcon("today", 2f, rect(4f, 4f, 16f, 16f, 5f), "M8.5 12l2.5 2.5 4.5-5")
    val history = lineIcon("history", 2f, rect(3.5f, 5f, 17f, 15f, 4f), "M3.5 10h17M8 3v4M16 3v4")
    val settings = lineIcon(
        "settings", 2f, "M4 7h10M18 7h2M4 17h4M12 17h8", circle(16f, 7f, 2f), circle(10f, 17f, 2f),
    )
    val check = lineIcon("check", 2.6f, "M5 12.5l4.5 4.5L19 7.5")
    val grid = lineIcon(
        "grid", 2.2f,
        rect(4f, 4f, 7f, 7f, 2f), rect(13f, 4f, 7f, 7f, 2f), rect(4f, 13f, 7f, 7f, 2f), rect(13f, 13f, 7f, 7f, 2f),
    )
    val recent = lineIcon("recent", 2.2f, "M3.5 12a8.5 8.5 0 1 0 2.6-6.1", "M3.5 4.5v4h4", "M12 8v4.2l2.8 1.8")
    val back = lineIcon("back", 2f, "M15 5l-7 7 7 7")
    val pencil = lineIcon("pencil", 2f, "M4 20h4L19 9l-4-4L4 16z")
}
