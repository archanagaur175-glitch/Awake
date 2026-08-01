package com.awake.alarm.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.awake.alarm.AwakeApp
import com.awake.alarm.alarm.AlarmScheduler
import com.awake.alarm.data.Alarm
import com.awake.alarm.data.MissionType
import com.awake.alarm.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAlarmScreen(
    alarmId: Long? = null,
    onBack: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val app = context.applicationContext as AwakeApp
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    var hour by remember { mutableIntStateOf(7) }
    var minute by remember { mutableIntStateOf(0) }
    var label by remember { mutableStateOf("") }
    var selectedDays by remember { mutableStateOf(setOf<Int>()) }
    var selectedMission by remember { mutableStateOf(MissionType.MATH) }
    var volume by remember { mutableFloatStateOf(0.8f) }
    var isEditing by remember { mutableStateOf(false) }

    // Load existing alarm if editing
    LaunchedEffect(alarmId) {
        if (alarmId != null && alarmId > 0) {
            app.repository.getAlarmById(alarmId)?.let { alarm ->
                hour = alarm.hour
                minute = alarm.minute
                label = alarm.label
                selectedDays = alarm.daysOfWeek.toSet()
                selectedMission = alarm.missionType
                volume = alarm.volume
                isEditing = true
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (isEditing) "Edit Alarm" else "New Alarm")
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isEditing) {
                        IconButton(onClick = {
                            scope.launch {
                                if (alarmId != null) {
                                    app.repository.getAlarmById(alarmId)?.let { alarm ->
                                        AlarmScheduler(context).cancelAlarm(alarm)
                                        app.repository.deleteAlarm(alarm)
                                    }
                                }
                                onBack()
                            }
                        }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = AlarmActive
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        containerColor = DarkBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Time Picker
            CustomTimePicker(
                hour = hour,
                minute = minute,
                onTimeChanged = { h, m -> hour = h; minute = m }
            )

            // Label
            OutlinedTextField(
                value = label,
                onValueChange = { label = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Label (optional)") },
                placeholder = { Text("e.g., Morning workout") },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentIndigo,
                    unfocusedBorderColor = TextTertiary.copy(alpha = 0.3f),
                    focusedContainerColor = DarkSurface,
                    unfocusedContainerColor = DarkSurface
                )
            )

            // Days of Week
            Text(
                text = "Repeat",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val days = listOf("M", "T", "W", "T", "F", "S", "S")
                days.forEachIndexed { index, day ->
                    val dayNum = index + 1
                    val isSelected = dayNum in selectedDays
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) AccentIndigo else DarkSurfaceVariant
                            )
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                selectedDays = if (isSelected) {
                                    selectedDays - dayNum
                                } else {
                                    selectedDays + dayNum
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = day,
                            color = if (isSelected) Color.White else TextTertiary,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            if (selectedDays.isEmpty()) {
                Text(
                    text = "One-shot alarm (no repeat)",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextTertiary
                )
            }

            // Mission Type
            Text(
                text = "Wake-up Mission",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MissionType.entries.take(2).forEach { mission ->
                    val isSelected = mission == selectedMission
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                selectedMission = mission
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) AccentIndigo.copy(alpha = 0.2f) else DarkSurfaceVariant
                        ),
                        shape = RoundedCornerShape(16.dp),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(
                            width = 2.dp,
                            color = AccentIndigo
                        ) else null
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (mission == MissionType.MATH) "🔢" else "📱",
                                fontSize = 28.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = mission.displayName,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (isSelected) AccentIndigo else TextSecondary
                            )
                        }
                    }
                }
            }

            // Volume
            Text(
                text = "Volume",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Default.Alarm,
                    contentDescription = null,
                    tint = TextTertiary,
                    modifier = Modifier.size(20.dp)
                )
                Slider(
                    value = volume,
                    onValueChange = { volume = it },
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = AccentIndigo,
                        activeTrackColor = AccentIndigo
                    )
                )
                Text(
                    text = "${(volume * 100).toInt()}%",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Save button
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    val alarm = Alarm(
                        id = alarmId ?: 0,
                        hour = hour,
                        minute = minute,
                        label = label,
                        daysOfWeek = selectedDays.toList().sorted(),
                        missionType = selectedMission,
                        volume = volume,
                        enabled = true
                    )
                    scope.launch {
                        val id = app.repository.insertAlarm(alarm)
                        val savedAlarm = alarm.copy(id = id)
                        AlarmScheduler(context).scheduleAlarm(savedAlarm)
                        onBack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentIndigo),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isEditing) "Update Alarm" else "Create Alarm",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
fun CustomTimePicker(
    hour: Int,
    minute: Int,
    onTimeChanged: (Int, Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Set time",
                style = MaterialTheme.typography.labelLarge,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Hour picker
                NumberPicker(
                    value = hour,
                    range = 0..23,
                    onValueChange = { onTimeChanged(it, minute) }
                )

                Text(
                    text = ":",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                // Minute picker
                NumberPicker(
                    value = minute,
                    range = 0..59,
                    onValueChange = { onTimeChanged(hour, it) }
                )
            }

            // AM/PM indicator
            Text(
                text = if (hour < 12) "AM" else "PM",
                style = MaterialTheme.typography.headlineMedium,
                color = AccentIndigo,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun NumberPicker(
    value: Int,
    range: IntRange,
    onValueChange: (Int) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Up arrow
        TextButton(onClick = {
            val newValue = if (value >= range.last) range.first else value + 1
            onValueChange(newValue)
        }) {
            Text("▲", color = TextTertiary)
        }

        Text(
            text = String.format("%02d", value),
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        // Down arrow
        TextButton(onClick = {
            val newValue = if (value <= range.first) range.last else value - 1
            onValueChange(newValue)
        }) {
            Text("▼", color = TextTertiary)
        }
    }
}
