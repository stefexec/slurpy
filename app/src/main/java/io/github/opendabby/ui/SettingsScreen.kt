package io.github.opendabby.ui

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
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.opendabby.AppViewModel
import io.github.opendabby.data.AppTheme
import io.github.opendabby.data.Session
import io.github.opendabby.data.Sound
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    var showResetPresetsDialog by remember { mutableStateOf(false) }

    when (subScreen) {
        SettingsSubScreen.History -> {
            HistoryScreen(
                sessions = viewModel.sessions,
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

/** Session History & Stats Screen */
@Composable
private fun HistoryScreen(
    sessions: List<Session>,
    onClearHistory: () -> Unit,
    onBack: () -> Unit,
) {
    var showClearConfirm by remember { mutableStateOf(false) }

    val totalDabs = sessions.size
    val totalSeconds = sessions.sumOf { it.heatSeconds + it.coolSeconds }
    val totalMinutes = totalSeconds / 60

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

            // Stats Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    StatCard(title = "Total Dabs", value = "$totalDabs", modifier = Modifier.weight(1f))
                    StatCard(title = "Total Time", value = "${totalMinutes}m", modifier = Modifier.weight(1f))
                }
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
                items(sessions) { session ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(dabby.surface)
                            .border(1.dp, Color(0xFF262626), RoundedCornerShape(16.dp))
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(session.emoji, fontSize = 24.sp)
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(session.presetName, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
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
                    description = "Right after taking your dab while the banger is still warm (~200°F–300°F), use a dry cotton swab (Q-tip) to mop up any leftover oil puddle. Rotate the swab around the floor and corners.",
                )
            }

            item {
                GuideCard(
                    step = "2",
                    title = "Alcohol Swab (ISO)",
                    description = "Dip a second swab into 91% or 99% isopropyl alcohol. Only apply after the quartz has cooled below 200°F to prevent thermal shock. Wipe down the entire interior until crystal clear.",
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
