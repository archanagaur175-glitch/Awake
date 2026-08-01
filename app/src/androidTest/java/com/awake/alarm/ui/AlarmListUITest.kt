package com.awake.alarm.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.awake.alarm.ui.theme.AwakeTheme
import org.junit.Rule
import org.junit.Test

class AlarmListUITest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun homeScreen_displaysEmptyState() {
        composeTestRule.setContent {
            AwakeTheme {
                EmptyState(onCreateAlarm = {})
            }
        }

        composeTestRule.onNodeWithText("No alarms yet").assertIsDisplayed()
        composeTestRule.onNodeWithText("Create Alarm").assertIsDisplayed()
    }

    @Test
    fun homeScreen_displaysHeroWhenAlarmExists() {
        composeTestRule.setContent {
            AwakeTheme {
                com.awake.alarm.ui.NextAlarmHero(
                    alarm = com.awake.alarm.data.Alarm(
                        id = 1,
                        hour = 8,
                        minute = 30,
                        label = "Morning",
                        enabled = true
                    )
                )
            }
        }

        composeTestRule.onNodeWithText("Next alarm in").assertIsDisplayed()
    }

    @Test
    fun emptyState_showsCreateButton() {
        composeTestRule.setContent {
            AwakeTheme {
                EmptyState(onCreateAlarm = {})
            }
        }

        composeTestRule.onNodeWithText("Create Alarm").performClick()
    }
}
