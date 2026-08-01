package com.awake.alarm.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.awake.alarm.data.AwakeDatabase
import com.awake.alarm.data.AlarmRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != Intent.ACTION_MY_PACKAGE_REPLACED
        ) return

        val pendingResult = goAsync()
        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

        scope.launch {
            try {
                withTimeoutOrNull(10_000L) {
                    val database = AwakeDatabase.getDatabase(context)
                    val repository = AlarmRepository(database.alarmDao())
                    val scheduler = AlarmScheduler(context)

                    val enabledAlarms = repository.getAllEnabledAlarms()
                    enabledAlarms.forEach { alarm ->
                        scheduler.scheduleAlarm(alarm)
                    }
                } ?: Log.e("BootReceiver", "Timed out rescheduling alarms")
            } finally {
                scope.cancel()
                pendingResult.finish()
            }
        }
    }
}
