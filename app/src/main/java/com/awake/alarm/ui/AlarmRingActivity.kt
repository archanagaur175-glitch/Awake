package com.awake.alarm.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.awake.alarm.alarm.AlarmScheduler
import com.awake.alarm.alarm.AlarmService
import com.awake.alarm.ui.theme.AwakeTheme
import com.awake.alarm.ui.theme.DarkBackground

class AlarmRingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val alarmId = intent?.getLongExtra(AlarmScheduler.EXTRA_ALARM_ID, -1) ?: -1
        val hour = intent?.getIntExtra(AlarmScheduler.EXTRA_ALARM_HOUR, 0) ?: 0
        val minute = intent?.getIntExtra(AlarmScheduler.EXTRA_ALARM_MINUTE, 0) ?: 0
        val label = intent?.getStringExtra(AlarmScheduler.EXTRA_ALARM_LABEL) ?: ""
        val volume = intent?.getFloatExtra(AlarmScheduler.EXTRA_ALARM_VOLUME, 0.8f) ?: 0.8f
        val missionType = intent?.getStringExtra(AlarmScheduler.EXTRA_ALARM_MISSION_TYPE) ?: "MATH"
        val missionConfig = intent?.getStringExtra(AlarmScheduler.EXTRA_ALARM_MISSION_CONFIG) ?: "{}"

        // Prevent back dismissal — only mission completion stops alarm
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Do nothing — mission must be completed to dismiss
            }
        })

        setContent {
            AwakeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DarkBackground
                ) {
                    AlarmRingScreen(
                        alarmId = alarmId,
                        hour = hour,
                        minute = minute,
                        label = label,
                        volume = volume,
                        missionType = missionType,
                        missionConfig = missionConfig,
                        onDismiss = { dismissAlarm() }
                    )
                }
            }
        }
    }

    private fun dismissAlarm() {
        val serviceIntent = android.content.Intent(this, AlarmService::class.java)
        stopService(serviceIntent)
        finish()
    }
}
