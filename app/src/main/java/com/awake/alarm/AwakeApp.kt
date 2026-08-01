package com.awake.alarm

import android.app.Application
import com.awake.alarm.data.AlarmRepository
import com.awake.alarm.data.AwakeDatabase

class AwakeApp : Application() {

    val database by lazy { AwakeDatabase.getDatabase(this) }
    val repository by lazy { AlarmRepository(database.alarmDao()) }
}
