package com.awake.alarm.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.awake.alarm.data.MissionType

class AlarmBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != AlarmScheduler.ACTION_ALARM_FIRED) return

        val alarmId = intent.getLongExtra(AlarmScheduler.EXTRA_ALARM_ID, -1)
        val hour = intent.getIntExtra(AlarmScheduler.EXTRA_ALARM_HOUR, 0)
        val minute = intent.getIntExtra(AlarmScheduler.EXTRA_ALARM_MINUTE, 0)
        val label = intent.getStringExtra(AlarmScheduler.EXTRA_ALARM_LABEL) ?: ""
        val volume = intent.getFloatExtra(AlarmScheduler.EXTRA_ALARM_VOLUME, 0.8f)
        val missionType = try {
            MissionType.valueOf(intent.getStringExtra(AlarmScheduler.EXTRA_ALARM_MISSION_TYPE) ?: "MATH")
        } catch (_: Exception) {
            MissionType.MATH
        }
        val missionConfig = intent.getStringExtra(AlarmScheduler.EXTRA_ALARM_MISSION_CONFIG) ?: "{}"

        val serviceIntent = Intent(context, AlarmService::class.java).apply {
            putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarmId)
            putExtra(AlarmScheduler.EXTRA_ALARM_HOUR, hour)
            putExtra(AlarmScheduler.EXTRA_ALARM_MINUTE, minute)
            putExtra(AlarmScheduler.EXTRA_ALARM_LABEL, label)
            putExtra(AlarmScheduler.EXTRA_ALARM_VOLUME, volume)
            putExtra(AlarmScheduler.EXTRA_ALARM_MISSION_TYPE, missionType.name)
            putExtra(AlarmScheduler.EXTRA_ALARM_MISSION_CONFIG, missionConfig)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }
}
