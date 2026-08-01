package com.awake.alarm.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import com.awake.alarm.data.Alarm
import java.util.Calendar

class AlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    fun scheduleAlarm(alarm: Alarm) {
        val pendingIntent = createPendingIntent(alarm)
        val triggerTime = calculateNextTriggerTime(alarm)

        if (triggerTime == null) {
            Log.e("AlarmScheduler", "Failed to calculate trigger time for alarm ${alarm.id}")
            return
        }

        // Use setAlarmClock for highest OS priority — shows alarm icon on lock screen
        val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerTime, pendingIntent)
        alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
    }

    fun cancelAlarm(alarm: Alarm) {
        val pendingIntent = createPendingIntent(alarm)
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    private fun calculateNextTriggerTime(alarm: Alarm): Long? {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, alarm.hour)
            set(Calendar.MINUTE, alarm.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (alarm.daysOfWeek.isEmpty()) {
            // One-shot: if time already passed today, schedule for tomorrow
            if (calendar.timeInMillis <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }
            return calendar.timeInMillis
        }

        // Recurring: find next matching day of week
        val today = calendar.get(Calendar.DAY_OF_WEEK)
        val daysOfWeek = alarm.daysOfWeek.toMutableList()

        // Convert our 1=Mon..7=Sun to Calendar's 1=Sun..7=Sat
        // 1(Mon)->2, 2(Tue)->3, ..., 6(Sat)->7, 7(Sun)->1
        val calendarDays = daysOfWeek.map { day ->
            if (day == 7) Calendar.SUNDAY else day + 1
        }

        // Try today first
        if (calendarDays.contains(today) && calendar.timeInMillis > System.currentTimeMillis()) {
            return calendar.timeInMillis
        }

        // Find next matching day within the next 7 days
        for (i in 1..7) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            if (calendarDays.contains(calendar.get(Calendar.DAY_OF_WEEK))) {
                return calendar.timeInMillis
            }
        }

        return null
    }

    private fun createPendingIntent(alarm: Alarm): PendingIntent {
        val intent = Intent(context, AlarmBroadcastReceiver::class.java).apply {
            action = ACTION_ALARM_FIRED
            putExtra(EXTRA_ALARM_ID, alarm.id)
            putExtra(EXTRA_ALARM_HOUR, alarm.hour)
            putExtra(EXTRA_ALARM_MINUTE, alarm.minute)
            putExtra(EXTRA_ALARM_LABEL, alarm.label)
            putExtra(EXTRA_ALARM_VOLUME, alarm.volume)
            putExtra(EXTRA_ALARM_MISSION_TYPE, alarm.missionType.name)
            putExtra(EXTRA_ALARM_MISSION_CONFIG, alarm.missionConfigJson)
        }

        return PendingIntent.getBroadcast(
            context,
            alarm.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        const val ACTION_ALARM_FIRED = "com.awake.alarm.ACTION_ALARM_FIRED"
        const val EXTRA_ALARM_ID = "alarm_id"
        const val EXTRA_ALARM_HOUR = "alarm_hour"
        const val EXTRA_ALARM_MINUTE = "alarm_minute"
        const val EXTRA_ALARM_LABEL = "alarm_label"
        const val EXTRA_ALARM_VOLUME = "alarm_volume"
        const val EXTRA_ALARM_MISSION_TYPE = "alarm_mission_type"
        const val EXTRA_ALARM_MISSION_CONFIG = "alarm_mission_config"
    }
}
