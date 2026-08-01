package com.awake.alarm.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.awake.alarm.data.MissionType
import com.awake.alarm.mission.MathDifficulty
import com.awake.alarm.mission.MathMission
import com.awake.alarm.mission.ShakeIntensity
import com.awake.alarm.mission.ShakeMission
import com.awake.alarm.ui.theme.*
import kotlin.math.sin

@Composable
fun AlarmRingScreen(
    alarmId: Long,
    hour: Int,
    minute: Int,
    label: String,
    volume: Float,
    missionType: String,
    missionConfig: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val timeStr = String.format("%02d:%02d", hour, minute)

    // Create mission based on type
    val mission = remember(missionType) {
        when (missionType) {
            "SHAKE" -> ShakeMission(
                context = context,
                requiredShakes = 20,
                intensity = ShakeIntensity.VIGOROUS
            )
            else -> MathMission(difficulty = MathDifficulty.MEDIUM)
        }
    }

    LaunchedEffect(Unit) {
        mission.start()
    }

    // Track completion for haptic feedback
    var wasComplete by remember { mutableStateOf(false) }
    LaunchedEffect(mission.isComplete()) {
        if (mission.isComplete() && !wasComplete) {
            wasComplete = true
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    // Pulsing background animation
    val infiniteTransition = rememberInfiniteTransition(label = "bg_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bg_pulse_alpha"
    )

    // Wave animation for visualizer
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        AlarmActive.copy(alpha = pulseAlpha),
                        DarkBackground,
                        AlarmActive.copy(alpha = pulseAlpha * 0.5f)
                    )
                )
            )
    ) {
        // Waveform visualizer background
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .align(Alignment.TopCenter)
        ) {
            val width = size.width
            val height = size.height
            val centerY = height / 2

            for (i in 0 until 100) {
                val x = (i / 100f) * width
                val waveY = centerY + sin(
                    Math.toRadians((wavePhase + i * 5).toDouble())
                ).toFloat() * 40f

                drawCircle(
                    color = AccentIndigo.copy(alpha = 0.4f),
                    radius = 3f,
                    center = Offset(x, waveY)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top: Time display
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 48.dp)
            ) {
                Text(
                    text = timeStr,
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                if (label.isNotBlank()) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.headlineMedium,
                        color = AccentCyan
                    )
                }
            }

            // Center: Mission
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                if (mission.isComplete()) {
                    // Success state
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "✅",
                            fontSize = 64.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Alarm dismissed!",
                            style = MaterialTheme.typography.headlineLarge,
                            color = SuccessGreen
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.height(56.dp)
                        ) {
                            Text(
                                "Turn off alarm",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            )
                        }
                    }
                } else {
                    // Mission challenge
                    mission.render()
                }
            }

            // Bottom: Hint
            if (!mission.isComplete()) {
                Text(
                    text = "Complete the mission to dismiss",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextTertiary,
                    modifier = Modifier.padding(bottom = 32.dp)
                )
            }
        }
    }
}
