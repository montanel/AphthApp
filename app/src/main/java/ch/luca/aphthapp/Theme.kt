package ch.luca.aphthapp

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

/** Title row: a 30 sp title on the left and a small muted note on the right. */
@Composable
fun TitleRow(title: String, note: String, modifier: Modifier = Modifier) {
    Row(
        modifier.fillMaxWidth().defaultMinSize(minHeight = 36.dp).padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        BasicText(title, Modifier.alignByBaseline(), style = type(30.sp, FontWeight.ExtraBold, tracking = (-0.03).em))
        BasicText(note, Modifier.alignByBaseline(), style = type(15.sp, FontWeight.Normal, C.muted))
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
}
