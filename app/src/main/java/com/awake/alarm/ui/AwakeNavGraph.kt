package com.awake.alarm.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

@Composable
fun AwakeNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(
                onCreateAlarm = { navController.navigate("create") },
                onEditAlarm = { id -> navController.navigate("edit/$id") },
                onSettings = { navController.navigate("settings") }
            )
        }

        composable("create") {
            CreateAlarmScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "edit/{alarmId}",
            arguments = listOf(navArgument("alarmId") { type = NavType.LongType })
        ) { backStackEntry ->
            val alarmId = backStackEntry.arguments?.getLong("alarmId")
            CreateAlarmScreen(
                alarmId = alarmId,
                onBack = { navController.popBackStack() }
            )
        }

        composable("settings") {
            SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
