package com.awake.alarm.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class Alarm(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val hour: Int,
    val minute: Int,
    val enabled: Boolean = true,
    val label: String = "",
    val daysOfWeek: List<Int> = emptyList(), // 1=Mon, 7=Sun. Empty = one-shot
    val missionType: MissionType = MissionType.MATH,
    val missionConfigJson: String = "{}",
    val volume: Float = 0.8f,
    val createdAt: Long = System.currentTimeMillis()
)

enum class MissionType(val displayName: String) {
    MATH("Math Challenge"),
    SHAKE("Shake Challenge"),
    MEMORY("Memory Challenge"), // Future
    PHOTO("Photo Mission"),    // Future
    QR("QR / Barcode")        // Future
}
