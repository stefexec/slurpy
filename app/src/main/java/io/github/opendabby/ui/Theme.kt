package io.github.opendabby.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import io.github.opendabby.data.AppTheme

// Fixed colors shared by all themes (heat is always orange, cool always blue).
val HeatColor = Color(0xFFEE6A3A)
val HeatPillBg = Color(0xFF3A1F16)
val CoolColor = Color(0xFF5DB0EE)
val CoolPillBg = Color(0xFF172A38)
val TextPrimary = Color(0xFFF2F2F2)
val TextSecondary = Color(0xFF8E8E8E)
val Gold = Color(0xFFF2C744)
val Danger = Color(0xFFEF6B6B)
val Hairline = Color(0x14FFFFFF)

/** Colors that change with the selected theme. */
data class DabbyColors(
    val background: Color,
    val surface: Color,
    val sheet: Color,
    val accent: Color,
    val selectedCard: Color,
    val stepper: Color,
)

fun AppTheme.colors(): DabbyColors = when (this) {
    AppTheme.Classic -> DabbyColors(
        background = Color(0xFF0F0F0F), surface = Color(0xFF1A1A1A), sheet = Color(0xFF1C1C1C),
        accent = Color(0xFFEE6436), selectedCard = Color(0xFF1E1312), stepper = Color(0xFF261B1A),
    )
    AppTheme.Ember -> DabbyColors(
        background = Color(0xFF120C0A), surface = Color(0xFF1E1613), sheet = Color(0xFF1F1714),
        accent = Color(0xFFFF7A3D), selectedCard = Color(0xFF2A1610), stepper = Color(0xFF341E16),
    )
    AppTheme.Arctic -> DabbyColors(
        background = Color(0xFF0B1014), surface = Color(0xFF141C22), sheet = Color(0xFF161E24),
        accent = Color(0xFF5AB4F0), selectedCard = Color(0xFF10202C), stepper = Color(0xFF172B39),
    )
    AppTheme.Terp -> DabbyColors(
        background = Color(0xFF0C110D), surface = Color(0xFF151D16), sheet = Color(0xFF172018),
        accent = Color(0xFF6CCB5F), selectedCard = Color(0xFF142416), stepper = Color(0xFF1B301D),
    )
}

val LocalDabbyColors = staticCompositionLocalOf { AppTheme.Classic.colors() }

/** Shorthand for the current theme colors. */
val dabby: DabbyColors
    @Composable get() = LocalDabbyColors.current

@Composable
fun DabbyTheme(theme: AppTheme, content: @Composable () -> Unit) {
    val colors = theme.colors()
    val scheme = darkColorScheme(
        primary = colors.accent,
        onPrimary = Color.White,
        background = colors.background,
        onBackground = TextPrimary,
        surface = colors.sheet,
        onSurface = TextPrimary,
        surfaceContainerHigh = colors.sheet,
        onSurfaceVariant = TextSecondary,
    )
    CompositionLocalProvider(LocalDabbyColors provides colors) {
        MaterialTheme(colorScheme = scheme, content = content)
    }
}
