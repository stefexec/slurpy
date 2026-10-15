package io.github.opendabby.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.opendabby.AppViewModel
import io.github.opendabby.data.Preset
import io.github.opendabby.data.TempUnit
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

private val PRESET_EMOJIS = listOf(
    "⚖️", "🌿", "💨", "💎", "⚙️", "🎛️",
    "🔥", "❄️", "🍯", "🚀", "🧪", "🧊",
    "⚡", "🌋", "⏱️", "🎯", "🛸", "👑",
    "⭐", "🕯️", "☀️", "🌙", "🌊", "🍄",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: AppViewModel,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val presets = viewModel.presets
    val selectedPreset = viewModel.selectedPreset
    val listState = rememberLazyListState()
    val reorderableLazyListState = rememberReorderableLazyListState(listState) { from, to ->
        viewModel.movePreset(from.key as String, to.key as String)
    }

    var expandedPresetId by remember { mutableStateOf<String?>(null) }
    var emojiTargetId by remember { mutableStateOf<String?>(null) }
    var deleteCandidateId by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(dabby.background)
            .statusBarsPadding()
            .imePadding()
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 12.dp,
                bottom = 32.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Header: Logo + Title + Settings Button
            item(key = "header") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        DabbyLogo(size = 38.dp)
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = "DABBY",
                            color = TextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                        )
                    }
                    CircleIconButton(
                        icon = Icons.Filled.Settings,
                        contentDescription = "Settings",
                        onClick = onOpenSettings,
                    )
                }
            }

            // Big Start Button Card
            item(key = "start_button") {
                StartHeroCard(
                    preset = selectedPreset,
                    onStart = { viewModel.startTimer() },
                )
            }

            // Section Label
            item(key = "section_label") {
                SectionLabel(
                    text = "CHOOSE YOUR STYLE",
                    modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
                )
            }

            // Presets List
            items(presets, key = { it.id }) { preset ->
                ReorderableItem(reorderableLazyListState, key = preset.id) { isDragging ->
                    val isSelected = preset.id == viewModel.selectedId
                    val isExpanded = expandedPresetId == preset.id
                    val elevation by animateFloatAsState(if (isDragging) 8f else 0f, label = "elevation")
                    val scale by animateFloatAsState(if (isDragging) 1.02f else 1.0f, label = "scale")

                    PresetCard(
                        preset = preset,
                        isSelected = isSelected,
                        isExpanded = isExpanded,
                        tempUnit = viewModel.tempUnit,
                        onSelect = {
                            viewModel.select(preset.id)
                        },
                        onToggleExpand = {
                            expandedPresetId = if (isExpanded) null else preset.id
                        },
                        onPickEmoji = {
                            emojiTargetId = preset.id
                        },
                        onChangePreset = { updated ->
                            viewModel.updatePreset(updated)
                        },
                        onDelete = {
                            if (presets.size > 1) {
                                deleteCandidateId = preset.id
                            }
                        },
                        dragHandle = {
                            Icon(
                                imageVector = Icons.Filled.DragIndicator,
                                contentDescription = "Reorder",
                                tint = Color(0xFF666666),
                                modifier = Modifier
                                    .draggableHandle()
                                    .size(24.dp)
                                    .padding(2.dp),
                            )
                        },
                        modifier = Modifier
                            .scale(scale)
                            .shadow(elevation.dp, RoundedCornerShape(18.dp)),
                    )
                }
            }

            // Add Preset Card
            item(key = "add_preset") {
                AddPresetCard(
                    onClick = {
                        val newPreset = viewModel.addPreset()
                        expandedPresetId = newPreset.id
                    }
                )
            }
        }

        // Emoji Picker Dialog
        emojiTargetId?.let { targetId ->
            val targetPreset = presets.firstOrNull { it.id == targetId }
            if (targetPreset != null) {
                EmojiPickerDialog(
                    currentEmoji = targetPreset.emoji,
                    onSelectEmoji = { newEmoji ->
                        viewModel.updatePreset(targetPreset.copy(emoji = newEmoji))
                        emojiTargetId = null
                    },
                    onDismiss = { emojiTargetId = null },
                )
            } else {
                emojiTargetId = null
            }
        }

        // Delete Confirmation Dialog
        deleteCandidateId?.let { candidateId ->
            val candidate = presets.firstOrNull { it.id == candidateId }
            if (candidate != null) {
                ConfirmDialog(
                    title = "Delete Preset",
                    text = "Are you sure you want to delete \"${candidate.displayName}\"?",
                    confirmLabel = "Delete",
                    onConfirm = {
                        viewModel.deletePreset(candidateId)
                        if (expandedPresetId == candidateId) expandedPresetId = null
                        deleteCandidateId = null
                    },
                    onDismiss = { deleteCandidateId = null },
                )
            } else {
                deleteCandidateId = null
            }
        }
    }
}

/** The prominent Start Card at the top of the Home Screen */
@Composable
private fun StartHeroCard(
    preset: Preset,
    onStart: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = dabby.accent

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF221614),
                        Color(0xFF181211),
                        Color(0xFF14100F),
                    )
                )
            )
            .border(
                width = 1.5.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        accent.copy(alpha = 0.8f),
                        accent.copy(alpha = 0.25f),
                    )
                ),
                shape = RoundedCornerShape(24.dp),
            )
            .clickable(onClick = onStart)
            .padding(vertical = 24.dp, horizontal = 20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Pills row: Flame and Snowflake
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(bottom = 8.dp),
            ) {
                TimePill(seconds = preset.heatSeconds, heat = true)
                Spacer(Modifier.width(8.dp))
                TimePill(seconds = preset.coolSeconds, heat = false)
            }

            // Big "Start" label with play icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "Start",
                    color = Color.White,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                )
                Spacer(Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(36.dp),
                )
            }

            Spacer(Modifier.height(4.dp))

            // Subtitle with selected preset
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = preset.emoji,
                    fontSize = 18.sp,
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = preset.displayName,
                    color = TextSecondary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/** Individual preset card, expandable for full editing */
@Composable
private fun PresetCard(
    preset: Preset,
    isSelected: Boolean,
    isExpanded: Boolean,
    tempUnit: TempUnit = TempUnit.Fahrenheit,
    onSelect: () -> Unit,
    onToggleExpand: () -> Unit,
    onPickEmoji: () -> Unit,
    onChangePreset: (Preset) -> Unit,
    onDelete: () -> Unit,
    dragHandle: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor by animateColorAsState(
        if (isSelected) dabby.accent.copy(alpha = 0.75f) else Color(0xFF262626),
        label = "borderColor"
    )
    val bgColor by animateColorAsState(
        if (isSelected) dabby.selectedCard else dabby.surface,
        label = "bgColor"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(bgColor)
            .border(1.2.dp, borderColor, RoundedCornerShape(18.dp))
            .clickable(onClick = onSelect)
            .padding(14.dp)
    ) {
        // Collapsed Top Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Drag handle on left
            dragHandle()

            Spacer(Modifier.width(6.dp))

            // Emoji badge (tap to change)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF242424))
                    .border(1.dp, Color(0xFF333333), CircleShape)
                    .clickable(onClick = onPickEmoji),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = preset.emoji, fontSize = 20.sp)
            }

            Spacer(Modifier.width(10.dp))

            // Name and Temp Range (full room for text without cut-off)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = preset.displayName,
                    color = TextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    lineHeight = 21.sp,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = preset.tempRange(tempUnit),
                    color = TextSecondary,
                    fontSize = 13.sp,
                    maxLines = 1,
                    softWrap = false,
                )
            }

            // Time Pills (stacked vertically to maximize horizontal room for preset names)
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                TimePill(seconds = preset.heatSeconds, heat = true)
                TimePill(seconds = preset.coolSeconds, heat = false)
            }

            Spacer(Modifier.width(4.dp))

            // Expand/Edit button
            IconButton(
                onClick = onToggleExpand,
                modifier = Modifier.size(32.dp),
            ) {
                Icon(
                    imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = if (isExpanded) dabby.accent else TextSecondary,
                    modifier = Modifier.size(20.dp),
                )
            }
        }

        // Expanded Editor Body
        AnimatedVisibility(visible = isExpanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Name Field
                OutlinedTextField(
                    value = preset.name,
                    onValueChange = { newName ->
                        if (newName.length <= Preset.NAME_MAX_LENGTH) {
                            onChangePreset(preset.copy(name = newName))
                        }
                    },
                    label = { Text("Preset Name", color = TextSecondary) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = dabby.accent,
                        unfocusedBorderColor = Color(0xFF333333),
                        cursorColor = dabby.accent,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )

                // Temp Low
                EditorRow(
                    emoji = "🌡️",
                    label = "Temp Low",
                    stepperValue = tempUnit.toDisplayValue(preset.tempLow),
                    onMinus = {
                        val nextF = stepTemp(preset.tempLow, isPlus = false, isLow = true, otherF = preset.tempHigh, unit = tempUnit)
                        onChangePreset(preset.copy(tempLow = nextF))
                    },
                    onPlus = {
                        val nextF = stepTemp(preset.tempLow, isPlus = true, isLow = true, otherF = preset.tempHigh, unit = tempUnit)
                        onChangePreset(preset.copy(tempLow = nextF))
                    },
                )

                // Temp High
                EditorRow(
                    emoji = "🌡️",
                    label = "Temp High",
                    stepperValue = tempUnit.toDisplayValue(preset.tempHigh),
                    onMinus = {
                        val nextF = stepTemp(preset.tempHigh, isPlus = false, isLow = false, otherF = preset.tempLow, unit = tempUnit)
                        onChangePreset(preset.copy(tempHigh = nextF))
                    },
                    onPlus = {
                        val nextF = stepTemp(preset.tempHigh, isPlus = true, isLow = false, otherF = preset.tempLow, unit = tempUnit)
                        onChangePreset(preset.copy(tempHigh = nextF))
                    },
                )

                // Heat Duration
                EditorRow(
                    emoji = "🔥",
                    label = "Heat",
                    stepperValue = "${preset.heatSeconds}s",
                    onMinus = {
                        val next = (preset.heatSeconds - Preset.TIME_STEP).coerceAtLeast(Preset.TIME_MIN)
                        onChangePreset(preset.copy(heatSeconds = next))
                    },
                    onPlus = {
                        val next = (preset.heatSeconds + Preset.TIME_STEP).coerceAtMost(Preset.TIME_MAX)
                        onChangePreset(preset.copy(heatSeconds = next))
                    },
                )

                // Cool Duration
                EditorRow(
                    emoji = "❄️",
                    label = "Cool",
                    stepperValue = "${preset.coolSeconds}s",
                    onMinus = {
                        val next = (preset.coolSeconds - Preset.TIME_STEP).coerceAtLeast(Preset.TIME_MIN)
                        onChangePreset(preset.copy(coolSeconds = next))
                    },
                    onPlus = {
                        val next = (preset.coolSeconds + Preset.TIME_STEP).coerceAtMost(Preset.TIME_MAX)
                        onChangePreset(preset.copy(coolSeconds = next))
                    },
                )

                // Actions: Change Emoji & Delete
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF262626))
                            .clickable(onClick = onPickEmoji)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(preset.emoji, fontSize = 16.sp)
                        Spacer(Modifier.width(6.dp))
                        Text("Change Icon", color = TextPrimary, fontSize = 13.sp)
                    }

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF2A1A1A))
                            .clickable(onClick = onDelete)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Delete",
                            tint = Danger,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Delete", color = Danger, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

private fun stepTemp(currentF: Int, isPlus: Boolean, isLow: Boolean, otherF: Int, unit: TempUnit): Int {
    return if (unit == TempUnit.Fahrenheit) {
        val next = if (isPlus) currentF + Preset.TEMP_STEP else currentF - Preset.TEMP_STEP
        if (isLow) next.coerceIn(Preset.TEMP_MIN, otherF)
        else next.coerceIn(otherF, Preset.TEMP_MAX)
    } else {
        val currentC = TempUnit.fahrenheitToCelsius(currentF)
        val otherC = TempUnit.fahrenheitToCelsius(otherF)
        val nextC = if (isPlus) currentC + 10 else currentC - 10
        val clampedC = if (isLow) nextC.coerceIn(40, otherC) else nextC.coerceIn(otherC, 550)
        TempUnit.celsiusToFahrenheit(clampedC)
    }
}

@Composable
private fun EditorRow(
    emoji: String,
    label: String,
    stepperValue: String,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        EmojiLabel(emoji = emoji, label = label, fontSize = 16)
        Stepper(value = stepperValue, onMinus = onMinus, onPlus = onPlus)
    }
}

/** Dashed card for adding a new preset */
@Composable
private fun AddPresetCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(58.dp)
            .clip(RoundedCornerShape(18.dp))
            .dashedBorder(color = Color(0xFF383838), radius = 18.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Add Custom Preset",
                color = TextSecondary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

/** Dialog for picking an emoji from a curated grid */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun EmojiPickerDialog(
    currentEmoji: String,
    onSelectEmoji: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    BasicAlertDialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF1E1E1E),
            modifier = Modifier.padding(16.dp),
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Choose an Icon",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp),
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    maxItemsInEachRow = 6,
                ) {
                    PRESET_EMOJIS.forEach { emoji ->
                        val isSelected = emoji == currentEmoji
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) dabby.accent.copy(alpha = 0.25f) else Color(0xFF282828))
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) dabby.accent else Color(0xFF333333),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    onSelectEmoji(emoji)
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(text = emoji, fontSize = 22.sp)
                        }
                    }
                }
            }
        }
    }
}
