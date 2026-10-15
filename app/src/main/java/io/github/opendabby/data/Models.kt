package io.github.opendabby.data

import java.util.UUID

/** A heat/cool timing style shown on the home screen. Temperatures are in °F. */
data class Preset(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val emoji: String,
    val tempLow: Int,
    val tempHigh: Int,
    val heatSeconds: Int,
    val coolSeconds: Int,
) {
    val displayName: String get() = name.ifBlank { "Untitled" }
    fun tempRange(unit: TempUnit = TempUnit.Fahrenheit): String = unit.toRange(tempLow, tempHigh)

    companion object {
        const val TEMP_STEP = 25
        const val TEMP_MIN = 100
        const val TEMP_MAX = 1000
        const val TIME_STEP = 5
        const val TIME_MIN = 5
        const val TIME_MAX = 600
        const val NAME_MAX_LENGTH = 24

        fun new() = Preset(
            name = "New Preset", emoji = "🔥",
            tempLow = 400, tempHigh = 450, heatSeconds = 30, coolSeconds = 45,
        )
    }
}

enum class TempUnit(val label: String, val symbol: String) {
    Fahrenheit("Fahrenheit", "°F"),
    Celsius("Celsius", "°C");

    fun toDisplay(fahrenheit: Int): String = when (this) {
        Fahrenheit -> "$fahrenheit°F"
        Celsius -> "${fahrenheitToCelsius(fahrenheit)}°C"
    }

    fun toRange(lowF: Int, highF: Int): String = when (this) {
        Fahrenheit -> "$lowF–$highF°F"
        Celsius -> "${fahrenheitToCelsius(lowF)}–${fahrenheitToCelsius(highF)}°C"
    }

    fun toDisplayValue(fahrenheit: Int): String = when (this) {
        Fahrenheit -> "$fahrenheit°"
        Celsius -> "${fahrenheitToCelsius(fahrenheit)}°"
    }

    companion object {
        fun fahrenheitToCelsius(f: Int): Int = Math.round((f - 32) * 5.0 / 9.0).toInt()
        fun celsiusToFahrenheit(c: Int): Int = Math.round(c * 9.0 / 5.0 + 32.0).toInt()
    }
}

val DefaultPresets = listOf(
    Preset("classic", "Classic", "⚖️", 450, 500, 30, 30),
    Preset("low_slow", "Low & Slow", "🌿", 350, 450, 30, 60),
    Preset("high_temp", "High Temp", "💨", 550, 600, 30, 15),
    Preset("thick_quartz", "Thick Quartz", "💎", 375, 425, 45, 75),
    Preset("titanium", "Titanium Nail", "⚙️", 450, 500, 20, 20),
    Preset("custom", "Custom", "🎛️", 350, 450, 25, 50),
)

/** A completed session, recorded when the cool-down finishes. */
data class Session(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long,
    val presetName: String,
    val emoji: String,
    val heatSeconds: Int,
    val coolSeconds: Int,
)

enum class Sound(val label: String) {
    BellChime("Bell Chime"),
    BellChord("Bell Chord"),
    SingleBell("Single Bell"),
    TripleBeep("Triple Beep"),
    SoftChime("Soft Chime"),
    Silent("Silent"),
}

enum class AppTheme(val label: String, val emoji: String) {
    Classic("Classic", "🌑"),
    Ember("Ember", "🔥"),
    Arctic("Arctic", "🌊"),
    Terp("Terp", "🌿"),
}
