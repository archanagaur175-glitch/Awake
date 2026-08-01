package com.awake.alarm.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromIntList(value: List<Int>): String = value.joinToString(",")

    @TypeConverter
    fun toIntList(value: String): List<Int> {
        if (value.isBlank()) return emptyList()
        return value.split(",").mapNotNull { it.trim().toIntOrNull() }
    }

    @TypeConverter
    fun fromMissionType(value: MissionType): String = value.name

    @TypeConverter
    fun toMissionType(value: String): MissionType =
        try { MissionType.valueOf(value) } catch (_: Exception) { MissionType.MATH }
}
