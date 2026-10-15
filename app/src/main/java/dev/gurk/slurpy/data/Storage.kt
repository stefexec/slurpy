package dev.gurk.slurpy.data

import android.content.Context
import androidx.core.content.edit
import org.json.JSONArray
import org.json.JSONObject

/** Persists everything in SharedPreferences as small JSON blobs. */
class Storage(context: Context) {
    private val prefs = context.getSharedPreferences("slurpy", Context.MODE_PRIVATE).also { newPrefs ->
        val oldPrefs = context.getSharedPreferences("dabby", Context.MODE_PRIVATE)
        if (oldPrefs.all.isNotEmpty() && newPrefs.all.isEmpty()) {
            newPrefs.edit {
                oldPrefs.all.forEach { (k, v) ->
                    when (v) {
                        is String -> putString(k, v)
                        is Boolean -> putBoolean(k, v)
                        is Int -> putInt(k, v)
                        is Long -> putLong(k, v)
                        is Float -> putFloat(k, v)
                    }
                }
            }
        }
    }

    fun loadPresets(): List<Preset> =
        prefs.getString(KEY_PRESETS, null)
            ?.let { json -> runCatching { parseList(json) { it.toPreset() } }.getOrNull() }
            ?.takeIf { it.isNotEmpty() }
            ?: DefaultPresets

    fun savePresets(presets: List<Preset>) =
        prefs.edit { putString(KEY_PRESETS, JSONArray(presets.map { it.toJson() }).toString()) }

    fun loadSessions(): List<Session> =
        prefs.getString(KEY_SESSIONS, null)
            ?.let { json -> runCatching { parseList(json) { it.toSession() } }.getOrNull() }
            ?: emptyList()

    fun saveSessions(sessions: List<Session>) =
        prefs.edit { putString(KEY_SESSIONS, JSONArray(sessions.map { it.toJson() }).toString()) }

    var selectedPresetId: String?
        get() = prefs.getString(KEY_SELECTED, null)
        set(value) = prefs.edit { putString(KEY_SELECTED, value) }

    var heatSound: Sound
        get() = enumOrDefault(prefs.getString(KEY_HEAT_SOUND, null), Sound.BellChime)
        set(value) = prefs.edit { putString(KEY_HEAT_SOUND, value.name) }

    var coolSound: Sound
        get() = enumOrDefault(prefs.getString(KEY_COOL_SOUND, null), Sound.BellChord)
        set(value) = prefs.edit { putString(KEY_COOL_SOUND, value.name) }

    var theme: AppTheme
        get() = enumOrDefault(prefs.getString(KEY_THEME, null), AppTheme.Classic)
        set(value) = prefs.edit { putString(KEY_THEME, value.name) }

    var tempUnit: TempUnit
        get() = enumOrDefault(prefs.getString(KEY_TEMP_UNIT, null), TempUnit.Fahrenheit)
        set(value) = prefs.edit { putString(KEY_TEMP_UNIT, value.name) }

    private inline fun <reified T : Enum<T>> enumOrDefault(name: String?, default: T): T =
        enumValues<T>().firstOrNull { it.name == name } ?: default

    private fun <T> parseList(json: String, map: (JSONObject) -> T): List<T> {
        val array = JSONArray(json)
        return List(array.length()) { map(array.getJSONObject(it)) }
    }

    private fun Preset.toJson() = JSONObject()
        .put("id", id).put("name", name).put("emoji", emoji)
        .put("tempLow", tempLow).put("tempHigh", tempHigh)
        .put("heat", heatSeconds).put("cool", coolSeconds)

    private fun JSONObject.toPreset() = Preset(
        id = getString("id"),
        name = getString("name"),
        emoji = getString("emoji"),
        tempLow = getInt("tempLow"),
        tempHigh = getInt("tempHigh"),
        heatSeconds = getInt("heat"),
        coolSeconds = getInt("cool"),
    )

    private fun Session.toJson() = JSONObject()
        .put("id", id).put("time", timestamp).put("name", presetName).put("emoji", emoji)
        .put("heat", heatSeconds).put("cool", coolSeconds)

    private fun JSONObject.toSession() = Session(
        id = optString("id").takeIf { it.isNotBlank() } ?: java.util.UUID.randomUUID().toString(),
        timestamp = getLong("time"),
        presetName = getString("name"),
        emoji = getString("emoji"),
        heatSeconds = getInt("heat"),
        coolSeconds = getInt("cool"),
    )

    private companion object {
        const val KEY_PRESETS = "presets"
        const val KEY_SESSIONS = "sessions"
        const val KEY_SELECTED = "selected_preset"
        const val KEY_HEAT_SOUND = "heat_sound"
        const val KEY_COOL_SOUND = "cool_sound"
        const val KEY_THEME = "theme"
        const val KEY_TEMP_UNIT = "temp_unit"
    }
}
