package dev.gurk.slurpy.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.gurk.slurpy.R

/** The ring logo inside a dark rounded tile. Reuses the launcher foreground vector. */
@Composable
fun SlurpyLogo(size: Dp, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.3f))
            .background(Color(0xFF141414)),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.ic_launcher_foreground),
            contentDescription = "Slurpy",
            modifier = Modifier.requiredSize(size * 1.16f),
        )
    }
}

/** Small rounded "🔥 30s" / "❄️ 30s" badge. */
@Composable
fun TimePill(seconds: Int, heat: Boolean) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(if (heat) HeatPillBg else CoolPillBg)
            .padding(horizontal = 8.dp, vertical = 2.dp),
    ) {
        Text(
            text = "${if (heat) "🔥" else "❄️"} ${seconds}s",
            color = if (heat) HeatColor else CoolColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

/** "− value +" control from the custom preset editor. */
@Composable
fun Stepper(value: String, onMinus: () -> Unit, onPlus: () -> Unit) {
    Row(
        modifier = Modifier
            .width(134.dp)
            .height(40.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(slurpy.stepper)
            .border(1.dp, Hairline, RoundedCornerShape(14.dp)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StepperButton("−", onMinus)
        Text(
            text = value,
            color = TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f),
        )
        StepperButton("+", onPlus)
    }
}

@Composable
private fun StepperButton(symbol: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(42.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(symbol, color = TextPrimary, fontSize = 20.sp)
    }
}

/** Round dark button used for "home" on the timer and sub screens. */
@Composable
fun CircleIconButton(
    icon: ImageVector = Icons.Filled.Home,
    contentDescription: String = "Home",
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(43.dp)
            .clip(CircleShape)
            .background(Color(0xFF171717))
            .border(1.dp, Color(0xFF2A2A2A), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription, tint = Color(0xFF9A9A9A), modifier = Modifier.size(20.dp))
    }
}

/** Letter-spaced uppercase section header, e.g. "CHOOSE YOUR STYLE". */
@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        color = TextSecondary,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 3.sp,
        modifier = modifier,
    )
}

/** Inline "emoji  label" used for row titles. */
@Composable
fun EmojiLabel(emoji: String, label: String, color: Color = TextPrimary, fontSize: Int = 17) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(emoji, fontSize = (fontSize - 1).sp)
        Spacer(Modifier.width(8.dp))
        Text(label, color = color, fontSize = fontSize.sp)
    }
}

fun Modifier.dashedBorder(color: Color, radius: Dp) = drawBehind {
    val stroke = 1.5.dp.toPx()
    drawRoundRect(
        color = color,
        topLeft = Offset(stroke / 2, stroke / 2),
        size = Size(size.width - stroke, size.height - stroke),
        cornerRadius = CornerRadius(radius.toPx()),
        style = Stroke(width = stroke, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 5.dp.toPx()))),
    )
}

@Composable
fun ConfirmDialog(
    title: String,
    text: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(text, color = TextSecondary) },
        confirmButton = {
            TextButton(onClick = { onConfirm(); onDismiss() }) { Text(confirmLabel, color = Danger) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextPrimary) }
        },
        containerColor = Color(0xFF242424),
    )
}
