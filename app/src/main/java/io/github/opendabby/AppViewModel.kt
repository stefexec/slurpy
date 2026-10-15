package io.github.opendabby

import android.app.Application
import android.os.SystemClock
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.opendabby.data.AppTheme
import io.github.opendabby.data.DefaultPresets
import io.github.opendabby.data.Preset
import io.github.opendabby.data.Session
import io.github.opendabby.data.Sound
import io.github.opendabby.data.Storage
import io.github.opendabby.sound.SoundPlayer
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class Phase { GetReady, Heat, Cool, Done }

/** Snapshot of the running timer. Time is measured with [SystemClock.elapsedRealtime]. */
data class TimerState(
    val preset: Preset,
    val phase: Phase,
    val phaseStart: Long = SystemClock.elapsedRealtime(),
    val phaseMillis: Long = 0,
)

class AppViewModel(app: Application) : AndroidViewModel(app) {
    private val storage = Storage(app)
    private val sounds = SoundPlayer()
    private var timerJob: Job? = null

    var presets by mutableStateOf(storage.loadPresets())
        private set
    var selectedId by mutableStateOf(
        storage.selectedPresetId?.takeIf { id -> presets.any { it.id == id } } ?: presets.first().id
    )
        private set
    var heatSound by mutableStateOf(storage.heatSound)
        private set
    var coolSound by mutableStateOf(storage.coolSound)
        private set
    var theme by mutableStateOf(storage.theme)
        private set
    var tempUnit by mutableStateOf(storage.tempUnit)
        private set
    var sessions by mutableStateOf(storage.loadSessions())
        private set
    var timer by mutableStateOf<TimerState?>(null)
        private set

    val selectedPreset: Preset
        get() = presets.firstOrNull { it.id == selectedId } ?: presets.first()

    // ---- Presets ----

    fun select(id: String) {
        selectedId = id
        storage.selectedPresetId = id
    }

    fun updatePreset(preset: Preset) = persistPresets(presets.map { if (it.id == preset.id) preset else it })

    fun addPreset(): Preset {
        val preset = Preset.new()
        persistPresets(presets + preset)
        select(preset.id)
        return preset
    }

    /** Deletes a preset. The last remaining preset can't be deleted. */
    fun deletePreset(id: String) {
        if (presets.size <= 1) return
        val index = presets.indexOfFirst { it.id == id }
        if (index < 0) return
        val remaining = presets.filterNot { it.id == id }
        persistPresets(remaining)
        if (selectedId == id) select(remaining[(index - 1).coerceAtLeast(0)].id)
    }

    fun movePreset(fromId: String, toId: String) {
        val from = presets.indexOfFirst { it.id == fromId }
        val to = presets.indexOfFirst { it.id == toId }
        if (from < 0 || to < 0 || from == to) return
        persistPresets(presets.toMutableList().apply { add(to, removeAt(from)) })
    }

    fun resetPresets() {
        persistPresets(DefaultPresets)
        select(DefaultPresets.first().id)
    }

    private fun persistPresets(list: List<Preset>) {
        presets = list
        storage.savePresets(list)
    }

    // ---- Settings ----

    fun updateHeatSound(sound: Sound) {
        heatSound = sound
        storage.heatSound = sound
    }

    fun updateCoolSound(sound: Sound) {
        coolSound = sound
        storage.coolSound = sound
    }

    fun updateTheme(value: AppTheme) {
        theme = value
        storage.theme = value
    }

    fun updateTempUnit(unit: TempUnit) {
        tempUnit = unit
        storage.tempUnit = unit
    }

    fun previewSound(sound: Sound) = sounds.play(sound)

    fun clearHistory() {
        sessions = emptyList()
        storage.saveSessions(sessions)
    }

    fun deleteSession(sessionId: String): Pair<Session, Int>? {
        val index = sessions.indexOfFirst { it.id == sessionId }
        if (index < 0) return null
        val target = sessions[index]
        sessions = sessions.filterIndexed { i, _ -> i != index }
        storage.saveSessions(sessions)
        return Pair(target, index)
    }

    fun restoreSession(session: Session, index: Int = 0) {
        val list = sessions.toMutableList()
        val insertIndex = index.coerceIn(0, list.size)
        list.add(insertIndex, session)
        sessions = list
        storage.saveSessions(sessions)
    }

    // ---- Timer ----

    fun startTimer() = runTimer(selectedPreset, Phase.GetReady)

    fun coolDownNow() {
        timer?.takeIf { it.phase == Phase.Heat }?.let { runTimer(it.preset, Phase.Cool) }
    }

    fun stopTimer() {
        timerJob?.cancel()
        timer = null
    }

    private fun runTimer(preset: Preset, from: Phase) {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            if (from == Phase.GetReady) {
                runPhase(preset, Phase.GetReady, GET_READY_MILLIS)
            }
            if (from <= Phase.Heat) {
                runPhase(preset, Phase.Heat, preset.heatSeconds * 1000L)
                sounds.play(heatSound)
            }
            runPhase(preset, Phase.Cool, preset.coolSeconds * 1000L)
            sounds.play(coolSound)
            recordSession(preset)
            timer = TimerState(preset, Phase.Done)
        }
    }

    /** Publishes the phase, then waits until it's over by wall-clock (robust against delay drift). */
    private suspend fun runPhase(preset: Preset, phase: Phase, millis: Long) {
        val state = TimerState(preset, phase, phaseMillis = millis)
        timer = state
        val end = state.phaseStart + millis
        while (true) {
            val remaining = end - SystemClock.elapsedRealtime()
            if (remaining <= 0) break
            delay(minOf(remaining, 250))
        }
    }

    private fun recordSession(preset: Preset) {
        val session = Session(
            timestamp = System.currentTimeMillis(),
            presetName = preset.displayName,
            emoji = preset.emoji,
            heatSeconds = preset.heatSeconds,
            coolSeconds = preset.coolSeconds,
        )
        sessions = (listOf(session) + sessions).take(MAX_SESSIONS)
        storage.saveSessions(sessions)
    }

    override fun onCleared() {
        sounds.release()
    }

    private companion object {
        const val GET_READY_MILLIS = 3000L
        const val MAX_SESSIONS = 2000
    }
}
