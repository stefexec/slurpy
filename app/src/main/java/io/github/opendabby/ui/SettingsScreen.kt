package io.github.opendabby.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.opendabby.AppViewModel
import io.github.opendabby.data.AppTheme
import io.github.opendabby.data.Session
import io.github.opendabby.data.Sound
import io.github.opendabby.data.TempUnit
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class SettingsSubScreen {
    None, History, CleaningGuide
}

@Composable
fun SettingsScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var subScreen by remember { mutableStateOf(SettingsSubScreen.None) }
    var soundDialogTarget by remember { mutableStateOf<String?>(null) } // "heat" or "cool"
    var showThemeDialog by remember { mutableStateOf(false) }
    var showTempUnitDialog by remember { mutableStateOf(false) }
    var showResetPresetsDialog by remember { mutableStateOf(false) }

    when (subScreen) {
        SettingsSubScreen.History -> {
            HistoryScreen(
                sessions = viewModel.sessions,
                onDeleteSession = { viewModel.deleteSession(it) },
                onRestoreSession = { session, index -> viewModel.restoreSession(session, index) },
                onClearHistory = { viewModel.clearHistory() },
                onBack = { subScreen = SettingsSubScreen.None },
            )
            return
        }
        SettingsSubScreen.CleaningGuide -> {
            CleaningGuideScreen(
                onBack = { subScreen = SettingsSubScreen.None },
            )
            return
        }
        SettingsSubScreen.None -> { /* show main settings */ }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(dabby.background)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Top Header: Back + Title
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CircleIconButton(
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        onClick = onBack,
                    )
                    Spacer(Modifier.width(16.dp))
                    Text(
                        text = "Settings",
                        color = TextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            // SOUND Section
            item {
                SectionLabel(text = "SOUND")
                Spacer(Modifier.height(8.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(dabby.surface)
                        .border(1.dp, Color(0xFF262626), RoundedCornerShape(18.dp)),
                ) {
                    SettingsRow(
                        icon = Icons.AutoMirrored.Filled.VolumeUp,
                        title = "Heat Up Sound",
                        subtitle = viewModel.heatSound.label,
                        onClick = { soundDialogTarget = "heat" },
                    )
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF242424)))
                    SettingsRow(
                        icon = Icons.AutoMirrored.Filled.VolumeUp,
                        title = "Cool Down Sound",
                        subtitle = viewModel.coolSound.label,
                        onClick = { soundDialogTarget = "cool" },
                    )
                }
            }

            // PREFERENCES Section (Celsius / Fahrenheit)
            item {
                SectionLabel(text = "PREFERENCES")
                Spacer(Modifier.height(8.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(dabby.surface)
                        .border(1.dp, Color(0xFF262626), RoundedCornerShape(18.dp)),
                ) {
                    SettingsRow(
                        icon = Icons.Filled.Thermostat,
                        title = "Temperature Unit",
                        subtitle = "${viewModel.tempUnit.label} (${viewModel.tempUnit.symbol})",
                        onClick = { showTempUnitDialog = true },
                    )
                }
            }

            // PRO FEATURES Section (All Unlocked)
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    SectionLabel(text = "PRO FEATURES")
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0xFF1E3A24))
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                    ) {
                        Text(
                            text = "UNLOCKED",
                            color = Color(0xFF6CCB5F),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(dabby.surface)
                        .border(1.dp, Color(0xFF262626), RoundedCornerShape(18.dp)),
                ) {
                    SettingsRow(
                        icon = Icons.Filled.History,
                        title = "Session History & Stats",
                        subtitle = "${viewModel.sessions.size} dabs logged",
                        onClick = { subScreen = SettingsSubScreen.History },
                    )
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF242424)))
                    SettingsRow(
                        icon = Icons.Filled.Palette,
                        title = "Theme",
                        subtitle = "${viewModel.theme.emoji} ${viewModel.theme.label}",
                        onClick = { showThemeDialog = true },
                    )
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF242424)))
                    SettingsRow(
                        icon = Icons.Filled.CleaningServices,
                        title = "Banger Cleaning Guide",
                        subtitle = "Maintenance & best practices",
                        onClick = { subScreen = SettingsSubScreen.CleaningGuide },
                    )
                }
            }

            // SUPPORT & MANAGEMENT Section
            item {
                SectionLabel(text = "PRESETS & APP")
                Spacer(Modifier.height(8.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(dabby.surface)
                        .border(1.dp, Color(0xFF262626), RoundedCornerShape(18.dp)),
                ) {
                    SettingsRow(
                        icon = Icons.Filled.Refresh,
                        title = "Reset Presets to Defaults",
                        subtitle = "Restore original 6 presets",
                        onClick = { showResetPresetsDialog = true },
                    )
                }
            }

            // About Footer Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFF141414))
                        .border(1.dp, Color(0xFF222222), RoundedCornerShape(18.dp))
                        .padding(16.dp),
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Dabby Open Source",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Free, privacy-friendly Android dab timer clone with full Pro features included.",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "Version 1.0",
                            color = dabby.accent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }

        // Sound Selection Dialog
        soundDialogTarget?.let { target ->
            val isHeat = target == "heat"
            val currentSound = if (isHeat) viewModel.heatSound else viewModel.coolSound

            SoundPickerDialog(
                title = if (isHeat) "Heat Up Sound" else "Cool Down Sound",
                selectedSound = currentSound,
                onSelect = { sound ->
                    if (isHeat) viewModel.updateHeatSound(sound) else viewModel.updateCoolSound(sound)
                    viewModel.previewSound(sound)
                    soundDialogTarget = null
                },
                onPreview = { sound ->
                    viewModel.previewSound(sound)
                },
                onDismiss = { soundDialogTarget = null },
            )
        }

        // Theme Selection Dialog
        if (showThemeDialog) {
            ThemePickerDialog(
                currentTheme = viewModel.theme,
                onSelectTheme = { theme ->
                    viewModel.updateTheme(theme)
                    showThemeDialog = false
                },
                onDismiss = { showThemeDialog = false },
            )
        }

        // Temperature Unit Selection Dialog
        if (showTempUnitDialog) {
            TempUnitPickerDialog(
                currentUnit = viewModel.tempUnit,
                onSelectUnit = { unit ->
                    viewModel.updateTempUnit(unit)
                    showTempUnitDialog = false
                },
                onDismiss = { showTempUnitDialog = false },
            )
        }

        // Reset Presets Confirmation Dialog
        if (showResetPresetsDialog) {
            ConfirmDialog(
                title = "Reset Presets?",
                text = "This will restore the factory default presets and remove any customized timings.",
                confirmLabel = "Reset",
                onConfirm = {
                    viewModel.resetPresets()
                    showResetPresetsDialog = false
                },
                onDismiss = { showResetPresetsDialog = false },
            )
        }
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color(0xFF222222)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = dabby.accent, modifier = Modifier.size(18.dp))
        }

        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Text(subtitle, color = TextSecondary, fontSize = 13.sp)
        }

        Text("›", color = TextSecondary, fontSize = 20.sp, fontWeight = FontWeight.Light)
    }
}

/** Sound Picker Dialog with preview audio button */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SoundPickerDialog(
    title: String,
    selectedSound: Sound,
    onSelect: (Sound) -> Unit,
    onPreview: (Sound) -> Unit,
    onDismiss: () -> Unit,
) {
    BasicAlertDialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF1E1E1E),
            modifier = Modifier.padding(16.dp),
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp),
                )

                Sound.entries.forEach { sound ->
                    val isSelected = sound == selectedSound
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(sound) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { onSelect(sound) },
                            colors = RadioButtonDefaults.colors(selectedColor = dabby.accent),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = sound.label,
                            color = if (isSelected) TextPrimary else TextSecondary,
                            fontSize = 16.sp,
                            modifier = Modifier.weight(1f),
                        )
                        if (sound != Sound.Silent) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF2B2B2B))
                                    .clickable { onPreview(sound) },
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PlayArrow,
                                    contentDescription = "Preview",
                                    tint = dabby.accent,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Theme Picker Dialog */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemePickerDialog(
    currentTheme: AppTheme,
    onSelectTheme: (AppTheme) -> Unit,
    onDismiss: () -> Unit,
) {
    BasicAlertDialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF1E1E1E),
            modifier = Modifier.padding(16.dp),
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Select Theme",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp),
                )

                AppTheme.entries.forEach { theme ->
                    val isSelected = theme == currentTheme
                    val colors = theme.colors()

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) Color(0xFF2A2A2A) else Color.Transparent)
                            .border(
                                width = if (isSelected) 1.dp else 0.dp,
                                color = if (isSelected) colors.accent else Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { onSelectTheme(theme) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(theme.emoji, fontSize = 22.sp)
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = theme.label,
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.weight(1f),
                        )
                        // Swatch dot
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(colors.accent)
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

/** Temperature Unit Picker Dialog (Celsius vs Fahrenheit) */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TempUnitPickerDialog(
    currentUnit: TempUnit,
    onSelectUnit: (TempUnit) -> Unit,
    onDismiss: () -> Unit,
) {
    BasicAlertDialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF1E1E1E),
            modifier = Modifier.padding(16.dp),
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Temperature Unit",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp),
                )

                TempUnit.entries.forEach { unit ->
                    val isSelected = unit == currentUnit
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) Color(0xFF2A2A2A) else Color.Transparent)
                            .clickable { onSelectUnit(unit) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { onSelectUnit(unit) },
                            colors = RadioButtonDefaults.colors(selectedColor = dabby.accent),
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "${unit.label} (${unit.symbol})",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                }
            }
        }
    }
}

/** Session History & Stats Screen */
@Composable
private fun HistoryScreen(
    sessions: List<Session>,
    onDeleteSession: (String) -> Pair<Session, Int>?,
    onRestoreSession: (Session, Int) -> Unit,
    onClearHistory: () -> Unit,
    onBack: () -> Unit,
) {
    var showClearConfirm by remember { mutableStateOf(false) }
    var deletedInfo by remember { mutableStateOf<Pair<Session, Int>?>(null) }
    var secondsRemaining by remember { mutableIntStateOf(5) }
    val coroutineScope = rememberCoroutineScope()
    var timerJob by remember { mutableStateOf<Job?>(null) }

    fun undoDelete() {
        val info = deletedInfo ?: return
        timerJob?.cancel()
        deletedInfo = null
        onRestoreSession(info.first, info.second)
    }

    val totalDabs = sessions.size
    val totalSeconds = sessions.sumOf { it.heatSeconds + it.coolSeconds }
    val totalMinutes = totalSeconds / 60

    val todayStart = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
    val todayDabs = sessions.count { it.timestamp >= todayStart }

    val favPreset = remember(sessions) {
        sessions.groupBy { it.presetName }
            .maxByOrNull { it.value.size }
            ?.let { entry ->
                val emoji = entry.value.first().emoji
                "$emoji ${entry.key} (${entry.value.size})"
            }
    }

    val avgHeat = if (sessions.isNotEmpty()) (sessions.sumOf { it.heatSeconds } / sessions.size) else 0
    val avgCool = if (sessions.isNotEmpty()) (sessions.sumOf { it.coolSeconds } / sessions.size) else 0

    // Compute dabs per day of week (0=Mon .. 6=Sun)
    val dayCounts = remember(sessions) {
        val counts = IntArray(7)
        val cal = Calendar.getInstance()
        sessions.forEach { s ->
            cal.timeInMillis = s.timestamp
            val dayIndex = when (cal.get(Calendar.DAY_OF_WEEK)) {
                Calendar.MONDAY -> 0
                Calendar.TUESDAY -> 1
                Calendar.WEDNESDAY -> 2
                Calendar.THURSDAY -> 3
                Calendar.FRIDAY -> 4
                Calendar.SATURDAY -> 5
                Calendar.SUNDAY -> 6
                else -> 0
            }
            counts[dayIndex]++
        }
        counts
    }

    val todayIndex = remember {
        when (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> 0
            Calendar.TUESDAY -> 1
            Calendar.WEDNESDAY -> 2
            Calendar.THURSDAY -> 3
            Calendar.FRIDAY -> 4
            Calendar.SATURDAY -> 5
            Calendar.SUNDAY -> 6
            else -> 0
        }
    }

    val maxCount = dayCounts.maxOrNull() ?: 0
    val peakDayIndex = if (maxCount > 0) dayCounts.indexOfFirst { it == maxCount } else -1

    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy · h:mm a", Locale.getDefault()) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(dabby.background)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 90.dp),
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircleIconButton(
                            icon = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            onClick = onBack,
                        )
                        Spacer(Modifier.width(16.dp))
                        Text(
                            text = "Session History",
                            color = TextPrimary,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }

                    if (sessions.isNotEmpty()) {
                        TextButton(onClick = { showClearConfirm = true }) {
                            Text("Clear", color = Danger, fontSize = 15.sp)
                        }
                    }
                }
            }

            // Stats Cards Row: Total Dabs, Today, Total Time
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    StatCard(title = "Total Dabs", value = "$totalDabs", modifier = Modifier.weight(1f))
                    StatCard(title = "Today", value = "$todayDabs", modifier = Modifier.weight(1f))
                    StatCard(title = "Total Time", value = "${totalMinutes}m", modifier = Modifier.weight(1f))
                }
            }

            // Insights Pill: Favorite Preset & Average Times
            if (sessions.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF141414))
                            .border(1.dp, Color(0xFF222222), RoundedCornerShape(14.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (favPreset != null) {
                            Text(
                                text = "Fav: $favPreset",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                        Text(
                            text = "Avg: 🔥 ${avgHeat}s · ❄️ ${avgCool}s",
                            color = TextSecondary,
                            fontSize = 13.sp,
                        )
                    }
                }
            }

            // Day of Week Bar Chart
            item {
                WeeklyActivityChart(
                    dayCounts = dayCounts,
                    peakDayIndex = peakDayIndex,
                    todayIndex = todayIndex,
                    maxCount = maxCount,
                )
            }

            item {
                SectionLabel(text = "RECENT SESSIONS")
            }

            if (sessions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "No sessions logged yet.\nComplete a timer run to record your dab history!",
                            color = TextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            fontSize = 15.sp,
                        )
                    }
                }
            } else {
                items(sessions, key = { it.id }) { session ->
                    SwipeableSessionItem(
                        session = session,
                        dateFormat = dateFormat,
                        onDelete = {
                            val info = onDeleteSession(session.id)
                            if (info != null) {
                                deletedInfo = info
                                secondsRemaining = 5
                                timerJob?.cancel()
                                timerJob = coroutineScope.launch {
                                    for (sec in 5 downTo 1) {
                                        secondsRemaining = sec
                                        delay(1000L)
                                    }
                                    deletedInfo = null
                                }
                            }
                        },
                    )
                }
            }
        }

        // Undo Notification Banner (Slides in from bottom with 5-second countdown & tap to undo)
        AnimatedVisibility(
            visible = deletedInfo != null,
            enter = slideInVertically(
                initialOffsetY = { it + 60 },
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
            ) + fadeIn(),
            exit = slideOutVertically(
                targetOffsetY = { it + 60 },
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
            ) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 20.dp),
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { undoDelete() },
                color = Color(0xFF1E1E1E),
                border = BorderStroke(1.dp, Color(0xFF333333)),
                shadowElevation = 8.dp,
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Danger.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = null,
                                tint = Danger,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Dab entry deleted",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                text = "Tap to undo (${secondsRemaining}s)",
                                color = TextSecondary,
                                fontSize = 12.sp,
                            )
                        }
                    }

                    TextButton(
                        onClick = { undoDelete() },
                        colors = ButtonDefaults.textButtonColors(contentColor = dabby.accent),
                    ) {
                        Text(
                            text = "UNDO",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                        )
                    }
                }
            }
        }

        if (showClearConfirm) {
            ConfirmDialog(
                title = "Clear History?",
                text = "Are you sure you want to delete all recorded dab sessions?",
                confirmLabel = "Clear All",
                onConfirm = {
                    onClearHistory()
                    showClearConfirm = false
                },
                onDismiss = { showClearConfirm = false },
            )
        }
    }
}

/** Swipeable Session Pill with revealed Delete button on left swipe */
@Composable
private fun SwipeableSessionItem(
    session: Session,
    dateFormat: SimpleDateFormat,
    onDelete: () -> Unit,
) {
    val density = LocalDensity.current
    val buttonWidthDp = 84.dp
    val buttonWidthPx = with(density) { buttonWidthDp.toPx() }
    val maxDragPx = with(density) { 160.dp.toPx() }

    val offsetX = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
    ) {
        // Red Delete background revealed when swiping left
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Danger),
            contentAlignment = Alignment.CenterEnd,
        ) {
            Box(
                modifier = Modifier
                    .width(buttonWidthDp)
                    .fillMaxHeight()
                    .clickable {
                        coroutineScope.launch { offsetX.snapTo(0f) }
                        onDelete()
                    },
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp),
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "Delete",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }

        // Foreground session pill
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .background(dabby.surface)
                .border(1.dp, Color(0xFF262626), RoundedCornerShape(16.dp))
                .pointerInput(session.id) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            coroutineScope.launch {
                                val current = offsetX.value
                                when {
                                    current < -maxDragPx * 0.85f -> {
                                        offsetX.animateTo(-with(density) { 400.dp.toPx() })
                                        onDelete()
                                    }
                                    current < -buttonWidthPx / 2f -> {
                                        offsetX.animateTo(-buttonWidthPx, spring(stiffness = Spring.StiffnessMediumLow))
                                    }
                                    else -> {
                                        offsetX.animateTo(0f, spring(stiffness = Spring.StiffnessMediumLow))
                                    }
                                }
                            }
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            coroutineScope.launch {
                                val newOffset = (offsetX.value + dragAmount).coerceIn(-maxDragPx, 0f)
                                offsetX.snapTo(newOffset)
                            }
                        },
                    )
                }
                .clickable(
                    enabled = offsetX.value < -10f,
                    onClick = {
                        coroutineScope.launch {
                            offsetX.animateTo(0f, spring(stiffness = Spring.StiffnessMediumLow))
                        }
                    },
                )
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(session.emoji, fontSize = 24.sp)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = session.presetName,
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = dateFormat.format(Date(session.timestamp)),
                    color = TextSecondary,
                    fontSize = 12.sp,
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                TimePill(seconds = session.heatSeconds, heat = true)
                Spacer(Modifier.width(6.dp))
                TimePill(seconds = session.coolSeconds, heat = false)
            }
        }
    }
}

@Composable
private fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(dabby.surface)
            .border(1.dp, Color(0xFF262626), RoundedCornerShape(16.dp))
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, color = dabby.accent, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(title, color = TextSecondary, fontSize = 13.sp)
        }
    }
}

/** Modern bar chart showing dab distribution across all days of the week */
@Composable
private fun WeeklyActivityChart(
    dayCounts: IntArray,
    peakDayIndex: Int,
    todayIndex: Int,
    maxCount: Int,
    modifier: Modifier = Modifier,
) {
    val dayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val fullDayNames = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(dabby.surface)
            .border(1.dp, Color(0xFF262626), RoundedCornerShape(18.dp))
            .padding(16.dp),
    ) {
        Column {
            // Header of chart
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "ACTIVITY BY DAY",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 2.sp,
                )
                if (peakDayIndex in 0..6 && maxCount > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Peak: ",
                            color = TextSecondary,
                            fontSize = 12.sp,
                        )
                        Text(
                            text = "${fullDayNames[peakDayIndex]} ($maxCount)",
                            color = dabby.accent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // 7 Bar Columns
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(118.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                for (i in 0..6) {
                    val count = dayCounts[i]
                    val isPeak = i == peakDayIndex && count > 0
                    val isToday = i == todayIndex
                    val barHeightFraction = if (maxCount > 0) (count.toFloat() / maxCount.toFloat()) else 0f
                    val barHeight = (barHeightFraction * 68).dp.coerceAtLeast(if (count > 0) 8.dp else 4.dp)

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier.weight(1f),
                    ) {
                        // Count number label on top of bar
                        Text(
                            text = if (count > 0) "$count" else "",
                            color = if (isPeak) dabby.accent else TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = if (isPeak) FontWeight.Bold else FontWeight.Medium,
                            modifier = Modifier.height(16.dp),
                        )

                        Spacer(Modifier.height(4.dp))

                        // Bar track + fill
                        Box(
                            modifier = Modifier
                                .width(22.dp)
                                .height(70.dp)
                                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp, bottomStart = 3.dp, bottomEnd = 3.dp))
                                .background(Color(0xFF202020)),
                            contentAlignment = Alignment.BottomCenter,
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(barHeight)
                                    .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp, bottomStart = 3.dp, bottomEnd = 3.dp))
                                    .background(
                                        if (isPeak) {
                                            androidx.compose.ui.graphics.Brush.verticalGradient(
                                                listOf(
                                                    dabby.accent,
                                                    dabby.accent.copy(alpha = 0.75f)
                                                )
                                            )
                                        } else if (count > 0) {
                                            androidx.compose.ui.graphics.Brush.verticalGradient(
                                                listOf(
                                                    dabby.accent.copy(alpha = 0.65f),
                                                    dabby.accent.copy(alpha = 0.35f)
                                                )
                                            )
                                        } else {
                                            androidx.compose.ui.graphics.SolidColor(Color(0xFF282828))
                                        }
                                    )
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        // Day Label
                        Text(
                            text = dayNames[i],
                            color = if (isToday) TextPrimary else TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                        )

                        // Indicator for today
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .padding(top = 1.dp)
                        ) {
                            if (isToday) {
                                Box(
                                    modifier = Modifier
                                        .size(3.dp)
                                        .clip(CircleShape)
                                        .background(dabby.accent)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Comprehensive Banger Cleaning & Maintenance Guide Screen */
@Composable
private fun CleaningGuideScreen(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(dabby.background)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CircleIconButton(
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        onClick = onBack,
                    )
                    Spacer(Modifier.width(16.dp))
                    Text(
                        text = "Cleaning Guide",
                        color = TextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            item {
                GuideCard(
                    step = "1",
                    title = "Dry Swab Immediately",
                    description = "Right after taking your dab while the banger is still warm (~200°F–300°F / ~95°C–150°C), use a dry cotton swab (Q-tip) to mop up any leftover oil puddle. Rotate the swab around the floor and corners.",
                )
            }

            item {
                GuideCard(
                    step = "2",
                    title = "Alcohol Swab (ISO)",
                    description = "Dip a second swab into 91% or 99% isopropyl alcohol. Only apply after the quartz has cooled below 200°F / 95°C to prevent thermal shock. Wipe down the entire interior until crystal clear.",
                )
            }

            item {
                GuideCard(
                    step = "3",
                    title = "Never Torch Leftovers (\"Chazzing\")",
                    description = "Never use your torch to burn off dark residue! High heat scorches microscopic pores in the quartz, causing cloudiness and devitrification that permanently ruins heat retention and flavor.",
                )
            }

            item {
                GuideCard(
                    step = "4",
                    title = "Deep ISO Soak",
                    description = "For stubborn reclaim or carbon specks, soak your cooled quartz banger in a jar of 99% isopropyl alcohol or Dark Crystal cleaner overnight. Rinse thoroughly with warm water and let dry.",
                )
            }

            item {
                GuideCard(
                    step = "5",
                    title = "Cold-Start Technique",
                    description = "Place your concentrate into a cold, clean banger first. Put your carb cap on, then gently torch the sides and bottom until bubbling begins (~10-15s). Inhale immediately for maximum terpenes!",
                )
            }

            item {
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun GuideCard(step: String, title: String, description: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(dabby.surface)
            .border(1.dp, Color(0xFF262626), RoundedCornerShape(16.dp))
            .padding(16.dp),
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(dabby.accent.copy(alpha = 0.2f))
                .border(1.dp, dabby.accent, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(step, color = dabby.accent, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(description, color = TextSecondary, fontSize = 14.sp, lineHeight = 20.sp)
        }
    }
}
