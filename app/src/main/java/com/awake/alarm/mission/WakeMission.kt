package com.awake.alarm.mission

import androidx.compose.runtime.Composable

/**
 * Interface for all wake-up missions.
 * Implementations must provide a start trigger, completion check, and Compose UI.
 */
interface WakeMission {
    /** Called when the alarm fires and the mission should begin */
    fun start()

    /** Returns true only when the user has genuinely completed the mission */
    fun isComplete(): Boolean

    /** Called when the alarm is dismissed — clean up resources */
    fun onDismiss()

    /** The composable UI that renders the mission challenge */
    @Composable
    fun render()

    /** Progress 0.0..1.0 for animated progress indicators */
    fun progress(): Float

    /** Human-readable status text */
    fun statusText(): String
}
