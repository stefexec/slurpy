package io.github.opendabby.ui

import android.os.SystemClock
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.opendabby.AppViewModel
import io.github.opendabby.Phase
import io.github.opendabby.TimerState
import kotlinx.coroutines.delay
import kotlin.math.ceil

@Composable
fun TimerScreen(
    viewModel: AppViewModel,
    timerState: TimerState,
    modifier: Modifier = Modifier,
) {
    // High-resolution clock tick for smooth countdown and ring rendering
    val now by produceState(initialValue = SystemClock.elapsedRealtime()) {
        while (true) {
            value = SystemClock.elapsedRealtime()
            delay(33) // ~30 fps updates
        }
    }

    val elapsedMillis = (now - timerState.phaseStart).coerceAtLeast(0)
    val remainingMillis = (timerState.phaseMillis - elapsedMillis).coerceAtLeast(0)
    val remainingSeconds = ceil(remainingMillis / 1000.0).toInt()
    val progress = if (timerState.phaseMillis > 0) {
        (elapsedMillis.toFloat() / timerState.phaseMillis.toFloat()).coerceIn(0f, 1f)
    } else 1f

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(dabby.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(20.dp),
    ) {
        // Top Bar: Home/Exit Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopStart),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CircleIconButton(
                icon = if (timerState.phase == Phase.Done) Icons.Filled.Done else Icons.Filled.Home,
                contentDescription = "Home",
                onClick = { viewModel.stopTimer() },
            )
        }

        // Center Content based on Phase
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 60.dp, bottom = 80.dp),
            contentAlignment = Alignment.Center,
        ) {
            AnimatedContent(
                targetState = timerState.phase,
                transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) },
                label = "phaseContent"
            ) { phase ->
                when (phase) {
                    Phase.GetReady -> GetReadyContent(remainingSeconds = remainingSeconds)
                    Phase.Heat -> ActiveTimerContent(
                        preset = timerState.preset,
                        isHeat = true,
                        remainingSeconds = remainingSeconds,
                        progress = progress,
                    )
                    Phase.Cool -> ActiveTimerContent(
                        preset = timerState.preset,
                        isHeat = false,
                        remainingSeconds = remainingSeconds,
                        progress = progress,
                    )
                    Phase.Done -> DoneContent(preset = timerState.preset)
                }
            }
        }

        // Bottom Action (e.g. "Cool Down Now" during Heat phase)
        if (timerState.phase == Phase.Heat) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                CoolDownNowButton(onClick = { viewModel.coolDownNow() })
            }
        } else if (timerState.phase == Phase.Done) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Button(
                    onClick = { viewModel.stopTimer() },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF242424),
                        contentColor = TextPrimary,
                    ),
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(52.dp)
                        .border(1.dp, Color(0xFF383838), RoundedCornerShape(16.dp)),
                ) {
                    Text("Return Home", fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

/** Phase 1: Get Ready */
@Composable
private fun GetReadyContent(remainingSeconds: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "GET READY",
            color = TextSecondary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 4.sp,
            modifier = Modifier.padding(bottom = 24.dp),
        )

        Box(
            modifier = Modifier
                .size(160.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(Color(0xFF1E1715))
                .border(2.dp, HeatColor.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "${remainingSeconds.coerceAtLeast(1)}",
                color = HeatColor,
                fontSize = 72.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

/** Phase 2 & 3: Heat Up & Cool Down */
@Composable
private fun ActiveTimerContent(
    preset: io.github.opendabby.data.Preset,
    isHeat: Boolean,
    remainingSeconds: Int,
    progress: Float,
) {
    val accentColor = if (isHeat) HeatColor else CoolColor
    val trackBgColor = if (isHeat) HeatPillBg.copy(alpha = 0.5f) else CoolPillBg.copy(alpha = 0.5f)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // Preset Header: Emoji + Title
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp),
        ) {
            Text(preset.emoji, fontSize = 20.sp)
            Spacer(Modifier.width(8.dp))
            Text(
                text = preset.displayName,
                color = TextSecondary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
            )
        }

        // Phase Title: 🔥 Heat Up or ❄️ Cool Down
        Text(
            text = if (isHeat) "🔥 Heat Up" else "❄️ Cool Down",
            color = accentColor,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 28.dp),
        )

        // Large Circular Countdown Display
        Box(
            modifier = Modifier.size(240.dp),
            contentAlignment = Alignment.Center,
        ) {
            // Animated Canvas Ring
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 12.dp.toPx()
                val radius = (size.minDimension - strokeWidth) / 2
                val centerOffset = Offset(size.width / 2, size.height / 2)

                // Background track
                drawCircle(
                    color = trackBgColor,
                    radius = radius,
                    center = centerOffset,
                    style = Stroke(width = strokeWidth),
                )

                // Animated Progress Arc (clockwise countdown)
                val sweepAngle = (1f - progress) * 360f
                drawArc(
                    color = accentColor,
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                    size = Size(size.width - strokeWidth, size.height - strokeWidth),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                )
            }

            // Big Countdown Digits
            Text(
                text = "${remainingSeconds}s",
                color = TextPrimary,
                fontSize = 62.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        Spacer(Modifier.height(32.dp))

        // Progress Pill: 🔥 25s heat ➔ ❄️ 50s cool
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF1A1A1A))
                .border(1.dp, Color(0xFF282828), RoundedCornerShape(20.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "🔥 ${preset.heatSeconds}s heat",
                color = HeatColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "➔",
                color = TextSecondary,
                fontSize = 14.sp,
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "❄️ ${preset.coolSeconds}s cool",
                color = CoolColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

/** Phase 4: Done / Liftoff */
@Composable
private fun DoneContent(preset: io.github.opendabby.data.Preset) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "🚀",
            fontSize = 72.sp,
            modifier = Modifier.padding(bottom = 16.dp),
        )

        Text(
            text = "Time for liftoff!",
            color = TextPrimary,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 12.dp),
        )

        Text(
            text = "${preset.displayName} dab session complete",
            color = TextSecondary,
            fontSize = 17.sp,
            textAlign = TextAlign.Center,
        )
    }
}

/** Button to skip directly to cool down */
@Composable
private fun CoolDownNowButton(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1B2631))
            .border(1.2.dp, CoolColor.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("❄️", fontSize = 18.sp)
        Spacer(Modifier.width(10.dp))
        Text(
            text = "Cool Down Now",
            color = CoolColor,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
