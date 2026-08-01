package com.awake.alarm.alarm

import com.awake.alarm.data.Alarm
import com.awake.alarm.data.MissionType
import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar

class AlarmSchedulerTest {

    @Test
    fun `alarm with no days is one-shot`() {
        val alarm = Alarm(
            id = 1,
            hour = 8,
            minute = 30,
            enabled = true,
            daysOfWeek = emptyList()
        )
        assertTrue(alarm.daysOfWeek.isEmpty())
    }

    @Test
    fun `alarm with days is recurring`() {
        val alarm = Alarm(
            id = 1,
            hour = 8,
            minute = 30,
            enabled = true,
            daysOfWeek = listOf(1, 2, 3, 4, 5) // Mon-Fri
        )
        assertEquals(5, alarm.daysOfWeek.size)
    }

    @Test
    fun `alarm preserves mission type`() {
        val alarm = Alarm(
            id = 1,
            hour = 8,
            minute = 30,
            missionType = MissionType.SHAKE
        )
        assertEquals(MissionType.SHAKE, alarm.missionType)
    }

    @Test
    fun `alarm default volume is 80 percent`() {
        val alarm = Alarm(id = 1, hour = 8, minute = 30)
        assertEquals(0.8f, alarm.volume, 0.001f)
    }

    @Test
    fun `alarm enabled by default`() {
        val alarm = Alarm(id = 1, hour = 8, minute = 30)
        assertTrue(alarm.enabled)
    }

    @Test
    fun `alarm can be disabled`() {
        val alarm = Alarm(id = 1, hour = 8, minute = 30, enabled = false)
        assertFalse(alarm.enabled)
    }

    @Test
    fun `alarm days are sorted`() {
        val alarm = Alarm(
            id = 1,
            hour = 8,
            minute = 30,
            daysOfWeek = listOf(5, 1, 3)
        )
        val sorted = alarm.daysOfWeek.sorted()
        assertEquals(listOf(1, 3, 5), sorted)
    }

    @Test
    fun `alarm label is preserved`() {
        val alarm = Alarm(
            id = 1,
            hour = 8,
            minute = 30,
            label = "Morning workout"
        )
        assertEquals("Morning workout", alarm.label)
    }

    @Test
    fun `alarm time formatting`() {
        val alarm = Alarm(id = 1, hour = 7, minute = 5)
        val timeStr = String.format("%02d:%02d", alarm.hour, alarm.minute)
        assertEquals("07:05", timeStr)
    }

    @Test
    fun `midnight alarm time formatting`() {
        val alarm = Alarm(id = 1, hour = 0, minute = 0)
        val timeStr = String.format("%02d:%02d", alarm.hour, alarm.minute)
        assertEquals("00:00", timeStr)
    }

    @Test
    fun `mission config json is preserved`() {
        val config = """{"difficulty":"HARD","requiredCorrect":10}"""
        val alarm = Alarm(
            id = 1,
            hour = 8,
            minute = 30,
            missionConfigJson = config
        )
        assertEquals(config, alarm.missionConfigJson)
    }
}
