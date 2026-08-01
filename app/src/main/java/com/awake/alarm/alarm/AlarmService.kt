package com.awake.alarm.alarm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.awake.alarm.R
import com.awake.alarm.data.MissionType
import com.awake.alarm.ui.AlarmRingActivity
import kotlinx.coroutines.*

class AlarmService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var volumeRampJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification("Alarm ringing..."))
        initVibrator()
        acquireWakeLock()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val alarmId = intent?.getLongExtra(AlarmScheduler.EXTRA_ALARM_ID, -1) ?: -1
        val hour = intent?.getIntExtra(AlarmScheduler.EXTRA_ALARM_HOUR, 0) ?: 0
        val minute = intent?.getIntExtra(AlarmScheduler.EXTRA_ALARM_MINUTE, 0) ?: 0
        val label = intent?.getStringExtra(AlarmScheduler.EXTRA_ALARM_LABEL) ?: ""
        val volume = intent?.getFloatExtra(AlarmScheduler.EXTRA_ALARM_VOLUME, 0.8f) ?: 0.8f

        val timeStr = String.format("%02d:%02d", hour, minute)
        val displayText = if (label.isNotBlank()) "$label • $timeStr" else timeStr
        updateNotification(displayText)

        // Launch the alarm ring activity
        val ringIntent = Intent(this, AlarmRingActivity::class.java).apply {
            putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarmId)
            putExtra(AlarmScheduler.EXTRA_ALARM_HOUR, hour)
            putExtra(AlarmScheduler.EXTRA_ALARM_MINUTE, minute)
            putExtra(AlarmScheduler.EXTRA_ALARM_LABEL, label)
            putExtra(AlarmScheduler.EXTRA_ALARM_VOLUME, volume)
            putExtra(AlarmScheduler.EXTRA_ALARM_MISSION_TYPE,
                intent?.getStringExtra(AlarmScheduler.EXTRA_ALARM_MISSION_TYPE) ?: MissionType.MATH.name)
            putExtra(AlarmScheduler.EXTRA_ALARM_MISSION_CONFIG,
                intent?.getStringExtra(AlarmScheduler.EXTRA_ALARM_MISSION_CONFIG) ?: "{}")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        startActivity(ringIntent)

        startAlarmAudio(volume)
        startVibration()

        return START_STICKY
    }

    private fun startAlarmAudio(targetVolume: Float) {
        try {
            val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                setDataSource(this@AlarmService, alarmUri)
                isLooping = true
                setVolume(0f, 0f) // Start silent
                prepare()
                start()
            }

            // Smooth volume ramp-up: 20% -> target over 6 seconds
            rampVolume(targetVolume)
        } catch (_: Exception) {
            // Fallback: try default ringtone
        }
    }

    private fun rampVolume(targetVolume: Float) {
        volumeRampJob?.cancel()
        volumeRampJob = serviceScope.launch {
            val steps = 30
            val stepDelay = 200L // 6 seconds total
            val startVolume = 0.2f
            val volumeStep = (targetVolume - startVolume) / steps

            for (i in 0..steps) {
                val currentVolume = (startVolume + volumeStep * i).coerceIn(0f, 1f)
                mediaPlayer?.setVolume(currentVolume, currentVolume)
                delay(stepDelay)
            }
        }
    }

    private fun acquireWakeLock() {
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "Awake::AlarmWakeLock"
        ).apply {
            acquire(30 * 60 * 1000L) // 30 minutes max
        }
    }

    private fun initVibrator() {
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(VibratorManager::class.java)
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Vibrator::class.java)
        }
    }

    private fun startVibration() {
        val pattern = longArrayOf(0, 500, 500)
        vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Alarm",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Alarm notification"
            setShowBadge(false)
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun buildNotification(text: String): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, AlarmRingActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("Awake Alarm")
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_alarm)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(text: String) {
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, buildNotification(text))
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(STOP_FOREGROUND_REMOVE)
        volumeRampJob?.cancel()
        serviceScope.cancel()
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        vibrator?.cancel()
        wakeLock?.let {
            if (it.isHeld) it.release()
        }
    }

    companion object {
        private const val CHANNEL_ID = "alarm_channel"
        private const val NOTIFICATION_ID = 1001
    }
}
