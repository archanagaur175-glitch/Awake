package com.awake.alarm.ui

import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.awake.alarm.alarm.AlarmScheduler
import com.awake.alarm.alarm.BatteryOptimizationHelper
import com.awake.alarm.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var showBatteryDialog by remember { mutableStateOf(false) }

    // Check battery optimization on first load
    LaunchedEffect(Unit) {
        if (BatteryOptimizationHelper.isBatteryOptimizationEnabled(context)) {
            showBatteryDialog = true
        }
    }

    // Battery optimization dialog
    if (showBatteryDialog) {
        AlertDialog(
            onDismissRequest = { showBatteryDialog = false },
            title = {
                Text("Improve Reliability", fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "To ensure alarms fire reliably, please disable battery optimization for Awake. " +
                    "This prevents your device from killing the alarm service in the background."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        BatteryOptimizationHelper.requestIgnoreBatteryOptimization(context)
                        showBatteryDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentIndigo)
                ) {
                    Text("Open Settings")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBatteryDialog = false }) {
                    Text("Maybe later")
                }
            },
            containerColor = DarkSurfaceVariant
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        containerColor = DarkBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Battery optimization card
            SettingsCard(
                icon = Icons.Default.BatteryFull,
                title = "Battery Optimization",
                subtitle = if (BatteryOptimizationHelper.isBatteryOptimizationEnabled(context))
                    "Not optimized — alarms may be unreliable"
                else
                    "Optimized — alarms will fire reliably",
                iconTint = if (BatteryOptimizationHelper.isBatteryOptimizationEnabled(context))
                    WarningAmber else SuccessGreen,
                onClick = {
                    if (BatteryOptimizationHelper.isBatteryOptimizationEnabled(context)) {
                        BatteryOptimizationHelper.requestIgnoreBatteryOptimization(context)
                    }
                }
            )

            // Notification permission (Android 13+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                SettingsCard(
                    icon = Icons.Default.Info,
                    title = "Notifications",
                    subtitle = "Required for alarm alerts",
                    iconTint = AccentCyan,
                    onClick = {
                        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                        }
                        context.startActivity(intent)
                    }
                )
            }

            // About section
            SettingsCard(
                icon = Icons.Default.Info,
                title = "About Awake",
                subtitle = "Version 1.0.0 — Free forever, no ads",
                iconTint = AccentPurple,
                onClick = { }
            )

            Spacer(modifier = Modifier.weight(1f))

            // Philosophy note
            Text(
                text = "🌅 Awake is free forever. No ads, no paywalls, no tracking.",
                style = MaterialTheme.typography.bodySmall,
                color = TextTertiary,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Composable
fun SettingsCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    iconTint: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}
