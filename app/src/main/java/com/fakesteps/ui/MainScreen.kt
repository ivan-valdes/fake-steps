package com.fakesteps.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fakesteps.data.StepsConfig
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    config: StepsConfig,
    hasHealthPermissions: Boolean,
    healthConnectAvailable: Boolean,
    batteryOptimizationDisabled: Boolean,
    onRequestHealthPermissions: () -> Unit,
    onInstallHealthConnect: () -> Unit,
    onRequestBatteryExemption: () -> Unit,
    onUpdateHour: (Int, Int) -> Unit,
    onUpdateStepCount: (Int) -> Unit,
    onUpdateDuration: (Int) -> Unit,
    onToggleEnabled: (Boolean) -> Unit,
    onRunNow: () -> Unit
) {
    var showTimePicker by remember { mutableStateOf(false) }
    var stepCountText by remember(config.stepCount) { mutableStateOf(config.stepCount.toString()) }
    var durationText by remember(config.durationMinutes) { mutableStateOf(config.durationMinutes.toString()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("FakeSteps") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Status Section
            StatusCard(
                healthConnectAvailable = healthConnectAvailable,
                hasHealthPermissions = hasHealthPermissions,
                batteryOptimizationDisabled = batteryOptimizationDisabled,
                isEnabled = config.isEnabled,
                lastExecution = config.lastExecutionTimestamp
            )

            // Permissions Section
            if (!healthConnectAvailable || !hasHealthPermissions || !batteryOptimizationDisabled) {
                PermissionsCard(
                    healthConnectAvailable = healthConnectAvailable,
                    hasHealthPermissions = hasHealthPermissions,
                    batteryOptimizationDisabled = batteryOptimizationDisabled,
                    onRequestHealthPermissions = onRequestHealthPermissions,
                    onInstallHealthConnect = onInstallHealthConnect,
                    onRequestBatteryExemption = onRequestBatteryExemption
                )
            }

            // Configuration Section
            ConfigurationCard(
                config = config,
                stepCountText = stepCountText,
                durationText = durationText,
                onShowTimePicker = { showTimePicker = true },
                onStepCountChange = { text ->
                    stepCountText = text
                    text.toIntOrNull()?.let { count ->
                        if (count in 1..200000) onUpdateStepCount(count)
                    }
                },
                onDurationChange = { text ->
                    durationText = text
                    text.toIntOrNull()?.let { minutes ->
                        if (minutes in 1..480) onUpdateDuration(minutes)
                    }
                },
                onToggleEnabled = onToggleEnabled,
                enabled = hasHealthPermissions && healthConnectAvailable
            )

            // Run Now Button
            if (hasHealthPermissions && healthConnectAvailable) {
                Button(
                    onClick = onRunNow,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary
                    )
                ) {
                    Text("Run Now", modifier = Modifier.padding(8.dp))
                }
            }
        }
    }

    // Time Picker Dialog
    if (showTimePicker) {
        TimePickerDialog(
            initialHour = config.hour,
            initialMinute = config.minute,
            onConfirm = { hour, minute ->
                onUpdateHour(hour, minute)
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false }
        )
    }
}

@Composable
private fun StatusCard(
    healthConnectAvailable: Boolean,
    hasHealthPermissions: Boolean,
    batteryOptimizationDisabled: Boolean,
    isEnabled: Boolean,
    lastExecution: Long
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isEnabled && hasHealthPermissions)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Status",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            StatusRow("Health Connect", healthConnectAvailable)
            StatusRow("Permissions", hasHealthPermissions)
            StatusRow("Battery exemption", batteryOptimizationDisabled)
            StatusRow("Scheduled", isEnabled)

            if (lastExecution > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                Text(
                    text = "Last run: ${dateFormat.format(Date(lastExecution))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun StatusRow(label: String, ok: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = if (ok) "OK" else "Pending",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = if (ok) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.error
        )
    }
}

@Composable
private fun PermissionsCard(
    healthConnectAvailable: Boolean,
    hasHealthPermissions: Boolean,
    batteryOptimizationDisabled: Boolean,
    onRequestHealthPermissions: () -> Unit,
    onInstallHealthConnect: () -> Unit,
    onRequestBatteryExemption: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Setup Required",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (!healthConnectAvailable) {
                Button(
                    onClick = onInstallHealthConnect,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Install/Update Health Connect")
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (healthConnectAvailable && !hasHealthPermissions) {
                Button(
                    onClick = onRequestHealthPermissions,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Grant Health Permissions")
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (!batteryOptimizationDisabled) {
                OutlinedButton(
                    onClick = onRequestBatteryExemption,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Disable Battery Optimization")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConfigurationCard(
    config: StepsConfig,
    stepCountText: String,
    durationText: String,
    onShowTimePicker: () -> Unit,
    onStepCountChange: (String) -> Unit,
    onDurationChange: (String) -> Unit,
    onToggleEnabled: (Boolean) -> Unit,
    enabled: Boolean
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Configuration",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Scheduled Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Scheduled Time", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = String.format("%02d:%02d", config.hour, config.minute),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                FilledTonalButton(
                    onClick = onShowTimePicker,
                    enabled = enabled
                ) {
                    Text("Change")
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // Step Count
            OutlinedTextField(
                value = stepCountText,
                onValueChange = onStepCountChange,
                label = { Text("Steps per day") },
                supportingText = { Text("Range: 1 - 200,000") },
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled,
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Duration
            OutlinedTextField(
                value = durationText,
                onValueChange = onDurationChange,
                label = { Text("Duration (minutes)") },
                supportingText = { Text("Range: 1 - 480 min") },
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled,
                singleLine = true
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // Enable/Disable Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Auto-register daily", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        text = if (config.isEnabled) "Active" else "Inactive",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (config.isEnabled) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = config.isEnabled,
                    onCheckedChange = onToggleEnabled,
                    enabled = enabled
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onConfirm: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onConfirm(timePickerState.hour, timePickerState.minute)
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        title = { Text("Select time") },
        text = {
            TimePicker(state = timePickerState)
        }
    )
}
