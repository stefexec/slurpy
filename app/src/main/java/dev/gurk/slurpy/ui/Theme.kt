package dev.gurk.slurpy.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import dev.gurk.slurpy.data.AppTheme

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
data class SlurpyColors(
    val background: Color,
    val surface: Color,
    val sheet: Color,
    val accent: Color,
    val selectedCard: Color,
    val stepper: Color,
)

fun AppTheme.colors(): SlurpyColors = when (this) {
    AppTheme.Classic -> SlurpyColors(
        background = Color(0xFF0F0F0F), surface = Color(0xFF1A1A1A), sheet = Color(0xFF1C1C1C),
        accent = Color(0xFFEE6436), selectedCard = Color(0xFF1E1312), stepper = Color(0xFF261B1A),
    )
    AppTheme.Ember -> SlurpyColors(
        background = Color(0xFF120C0A), surface = Color(0xFF1E1613), sheet = Color(0xFF1F1714),
        accent = Color(0xFFFF7A3D), selectedCard = Color(0xFF2A1610), stepper = Color(0xFF341E16),
    )
    AppTheme.Arctic -> SlurpyColors(
        background = Color(0xFF0B1014), surface = Color(0xFF141C22), sheet = Color(0xFF161E24),
        accent = Color(0xFF5AB4F0), selectedCard = Color(0xFF10202C), stepper = Color(0xFF172B39),
    )
    AppTheme.Terp -> SlurpyColors(
        background = Color(0xFF0C110D), surface = Color(0xFF151D16), sheet = Color(0xFF172018),
        accent = Color(0xFF6CCB5F), selectedCard = Color(0xFF142416), stepper = Color(0xFF1B301D),
    )
    AppTheme.Goth -> SlurpyColors(
        background = Color(0xFF0A0A0A), surface = Color(0xFF141414), sheet = Color(0xFF171717),
        accent = Color(0xFFE53935), selectedCard = Color(0xFF221113), stepper = Color(0xFF2B1417),
    )
    AppTheme.Haze -> SlurpyColors(
        background = Color(0xFF0E0B16), surface = Color(0xFF171324), sheet = Color(0xFF1C172B),
        accent = Color(0xFFA855F7), selectedCard = Color(0xFF201338), stepper = Color(0xFF2B194C),
    )
}

val LocalSlurpyColors = staticCompositionLocalOf { AppTheme.Classic.colors() }

/** Shorthand for the current theme colors. */
val slurpy: SlurpyColors
    @Composable get() = LocalSlurpyColors.current

@Composable
fun SlurpyTheme(theme: AppTheme, content: @Composable () -> Unit) {
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
    CompositionLocalProvider(LocalSlurpyColors provides colors) {
        MaterialTheme(colorScheme = scheme, content = content)
    }
}
