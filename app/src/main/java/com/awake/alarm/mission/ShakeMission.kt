package com.awake.alarm.mission

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.awake.alarm.ui.theme.*
import kotlin.math.sqrt

enum class ShakeIntensity(val threshold: Float, val displayName: String) {
    GENTLE(12f, "Gentle"),
    VIGOROUS(20f, "Vigorous"),
    INTENSE(28f, "Intense")
}

class ShakeMission(
    private val context: Context,
    private val requiredShakes: Int = 20,
    private val intensity: ShakeIntensity = ShakeIntensity.VIGOROUS
) : WakeMission, SensorEventListener {

    private var _complete = false
    private var _shakeCount = 0
    private var _started = false
    private var _lastShakeTime = 0L

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private var lastX = 0f
    private var lastY = 0f
    private var lastZ = 0f

    val shakeCount: Int get() = _shakeCount
    val requiredCount: Int get() = requiredShakes
    val shakeThreshold: Float get() = intensity.threshold

    override fun start() {
        _started = true
        _complete = false
        _shakeCount = 0
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun isComplete(): Boolean = _complete

    override fun onDismiss() {
        sensorManager.unregisterListener(this)
    }

    override fun progress(): Float = _shakeCount.toFloat() / requiredShakes

    override fun statusText(): String = "$_shakeCount / $requiredShakes shakes"

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return
        if (!_started || _complete) return
        if (event.sensor.type != Sensor.TYPE_ACCELEROMETER) return
        if (event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) return

        val currentTime = System.currentTimeMillis()
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        val deltaX = x - lastX
        val deltaY = y - lastY
        val deltaZ = z - lastZ

        val acceleration = sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ)

        if (acceleration > intensity.threshold) {
            // Debounce: at least 200ms between shakes
            if (currentTime - _lastShakeTime > 200) {
                _shakeCount++
                _lastShakeTime = currentTime

                if (_shakeCount >= requiredShakes) {
                    _complete = true
                    sensorManager.unregisterListener(this)
                }
            }
        }

        lastX = x
        lastY = y
        lastZ = z
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    @Composable
    override fun render() {
        val haptic = LocalHapticFeedback.current
        val progressAnim by animateFloatAsState(
            targetValue = progress(),
            animationSpec = tween(300, easing = FastOutSlowInEasing),
            label = "shake_progress"
        )

        // Pulse animation for the ring
        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
        val pulseScale by infiniteTransition.animateFloat(
            initialValue = 0.95f,
            targetValue = 1.05f,
            animationSpec = infiniteRepeatable(
                animation = tween(800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse_scale"
        )

        val ringColor = when {
            progressAnim >= 1f -> SuccessGreen
            progressAnim > 0.6f -> AccentCyan
            else -> ShakeAccent
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Shake to dismiss",
                style = MaterialTheme.typography.titleMedium,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = intensity.displayName + " shakes required",
                style = MaterialTheme.typography.labelMedium,
                color = TextTertiary
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Circular progress ring
            Box(
                modifier = Modifier.size(200.dp),
                contentAlignment = Alignment.Center
            ) {
                // Background ring
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = DarkSurfaceVariant,
                        radius = size.minDimension / 2 - 8.dp.toPx(),
                        style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // Progress ring
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .scale(pulseScale)
                ) {
                    val sweepAngle = 360f * progressAnim
                    drawArc(
                        color = ringColor,
                        startAngle = -90f,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round),
                        topLeft = Offset(8.dp.toPx(), 8.dp.toPx()),
                        size = androidx.compose.ui.geometry.Size(
                            size.width - 16.dp.toPx(),
                            size.height - 16.dp.toPx()
                        )
                    )
                }

                // Center count
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "$_shakeCount",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "of $requiredShakes",
                        fontSize = 14.sp,
                        color = TextTertiary
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Shake instruction card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "📱",
                        fontSize = 32.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Shake your phone vigorously!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextPrimary
                    )
                    Text(
                        text = "Keep going until the ring fills up",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Status
            if (_complete) {
                Text(
                    text = "✅ Shake challenge complete!",
                    style = MaterialTheme.typography.titleMedium,
                    color = SuccessGreen,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Text(
                    text = "Progress: ${(progressAnim * 100).toInt()}%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextTertiary
                )
            }
        }
    }
}
