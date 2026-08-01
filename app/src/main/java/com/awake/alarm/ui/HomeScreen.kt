package com.awake.alarm.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.awake.alarm.AwakeApp
import com.awake.alarm.alarm.AlarmScheduler
import com.awake.alarm.data.Alarm
import com.awake.alarm.ui.theme.*
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onCreateAlarm: () -> Unit,
    onEditAlarm: (Long) -> Unit,
    onSettings: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val app = context.applicationContext as AwakeApp
    val alarms by app.repository.allAlarms.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    // Calculate next alarm
    val nextAlarm = remember(alarms) {
        alarms.filter { it.enabled }.minByOrNull { alarm ->
            val now = java.util.Calendar.getInstance()
            val alarmTime = java.util.Calendar.getInstance().apply {
                set(java.util.Calendar.HOUR_OF_DAY, alarm.hour)
                set(java.util.Calendar.MINUTE, alarm.minute)
                set(java.util.Calendar.SECOND, 0)
            }
            if (alarmTime.before(now)) alarmTime.add(java.util.Calendar.DAY_OF_YEAR, 1)
            alarmTime.timeInMillis
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Awake",
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp
                    )
                },
                actions = {
                    IconButton(onClick = onSettings) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = TextSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateAlarm,
                containerColor = AccentIndigo,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.scale(1.1f)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Alarm")
            }
        },
        containerColor = DarkBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Hero: Next alarm countdown
            if (nextAlarm != null) {
                NextAlarmHero(alarm = nextAlarm)
            }

            // Alarm list
            if (alarms.isEmpty()) {
                EmptyState(onCreateAlarm)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(alarms, key = { it.id }) { alarm ->
                        AlarmCard(
                            alarm = alarm,
                            onToggle = { enabled ->
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                scope.launch {
                                    val updated = alarm.copy(enabled = enabled)
                                    app.repository.updateAlarm(updated)
                                    if (enabled) {
                                        AlarmScheduler(context).scheduleAlarm(updated)
                                    } else {
                                        AlarmScheduler(context).cancelAlarm(updated)
                                    }
                                }
                            },
                            onClick = { onEditAlarm(alarm.id) },
                            onDelete = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                scope.launch {
                                    AlarmScheduler(context).cancelAlarm(alarm)
                                    app.repository.deleteAlarm(alarm)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NextAlarmHero(alarm: Alarm) {
    val infiniteTransition = rememberInfiniteTransition(label = "hero_pulse")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    val now = java.util.Calendar.getInstance()
    val alarmTime = java.util.Calendar.getInstance().apply {
        set(java.util.Calendar.HOUR_OF_DAY, alarm.hour)
        set(java.util.Calendar.MINUTE, alarm.minute)
        set(java.util.Calendar.SECOND, 0)
    }
    if (alarmTime.before(now)) alarmTime.add(java.util.Calendar.DAY_OF_YEAR, 1)

    val diffMs = alarmTime.timeInMillis - now.timeInMillis
    val hours = TimeUnit.MILLISECONDS.toHours(diffMs)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(diffMs) % 60

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            AccentIndigo.copy(alpha = glowAlpha),
                            AccentCyan.copy(alpha = glowAlpha * 0.5f)
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Column {
                Text(
                    text = "Next alarm in",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextSecondary
                )
                Text(
                    text = "${hours}h ${minutes}m",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                if (alarm.label.isNotBlank()) {
                    Text(
                        text = alarm.label,
                        style = MaterialTheme.typography.bodyLarge,
                        color = AccentCyan
                    )
                }
                Text(
                    text = String.format("%02d:%02d", alarm.hour, alarm.minute),
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
fun AlarmCard(
    alarm: Alarm,
    onToggle: (Boolean) -> Unit,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val timeStr = String.format("%02d:%02d", alarm.hour, alarm.minute)
    val dayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (alarm.enabled) DarkCard else DarkSurface
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = timeStr,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (alarm.enabled) TextPrimary else TextTertiary
                )
                if (alarm.label.isNotBlank()) {
                    Text(
                        text = alarm.label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
                if (alarm.daysOfWeek.isNotEmpty()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        alarm.daysOfWeek.forEach { day ->
                            val name = dayNames.getOrElse(day - 1) { "?" }
                            Text(
                                text = name.take(1),
                                style = MaterialTheme.typography.labelSmall,
                                color = AccentCyan,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(AccentCyan.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Switch(
                checked = alarm.enabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = AccentIndigo,
                    uncheckedThumbColor = TextTertiary,
                    uncheckedTrackColor = DarkSurfaceVariant
                )
            )
        }
    }
}

@Composable
fun EmptyState(onCreateAlarm: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🌅",
            fontSize = 64.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No alarms yet",
            style = MaterialTheme.typography.headlineMedium,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Create your first alarm to get started",
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onCreateAlarm,
            colors = ButtonDefaults.buttonColors(containerColor = AccentIndigo),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.height(56.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Create Alarm", fontWeight = FontWeight.SemiBold)
        }
    }
}
