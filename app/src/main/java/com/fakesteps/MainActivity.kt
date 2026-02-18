package com.fakesteps

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.health.connect.client.PermissionController
import androidx.lifecycle.lifecycleScope
import com.fakesteps.data.HealthConnectManager
import com.fakesteps.data.PreferencesManager
import com.fakesteps.receiver.AlarmReceiver
import com.fakesteps.ui.MainScreen
import com.fakesteps.ui.theme.FakeStepsTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var preferencesManager: PreferencesManager
    private lateinit var healthConnectManager: HealthConnectManager

    private var hasHealthPermissions = mutableStateOf(false)
    private var healthConnectAvailable = mutableStateOf(false)
    private var batteryOptimizationDisabled = mutableStateOf(false)

    private val requestPermissions = registerForActivityResult(
        PermissionController.createRequestPermissionResultContract()
    ) { granted ->
        hasHealthPermissions.value = granted.containsAll(HealthConnectManager.REQUIRED_PERMISSIONS)
    }

    private val requestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* no-op, just best effort */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        preferencesManager = PreferencesManager(this)
        healthConnectManager = HealthConnectManager(this)

        healthConnectAvailable.value = healthConnectManager.isAvailable()
        checkBatteryOptimization()

        // Request notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermission.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            FakeStepsTheme {
                val config by preferencesManager.configFlow.collectAsState(
                    initial = com.fakesteps.data.StepsConfig()
                )

                MainScreen(
                    config = config,
                    hasHealthPermissions = hasHealthPermissions.value,
                    healthConnectAvailable = healthConnectAvailable.value,
                    batteryOptimizationDisabled = batteryOptimizationDisabled.value,
                    onRequestHealthPermissions = { requestHealthPermissions() },
                    onInstallHealthConnect = { installHealthConnect() },
                    onRequestBatteryExemption = { requestBatteryExemption() },
                    onUpdateHour = { hour, minute ->
                        lifecycleScope.launch {
                            preferencesManager.updateScheduledTime(hour, minute)
                            saveAlarmPrefs(hour, minute, config.isEnabled)
                            if (config.isEnabled) {
                                AlarmReceiver.scheduleAlarm(this@MainActivity, hour, minute)
                            }
                        }
                    },
                    onUpdateStepCount = { count ->
                        lifecycleScope.launch {
                            preferencesManager.updateStepCount(count)
                        }
                    },
                    onUpdateDuration = { minutes ->
                        lifecycleScope.launch {
                            preferencesManager.updateDurationMinutes(minutes)
                        }
                    },
                    onToggleEnabled = { enabled ->
                        lifecycleScope.launch {
                            preferencesManager.updateEnabled(enabled)
                            val currentConfig = preferencesManager.configFlow.first()
                            saveAlarmPrefs(currentConfig.hour, currentConfig.minute, enabled)
                            if (enabled) {
                                AlarmReceiver.scheduleAlarm(
                                    this@MainActivity,
                                    currentConfig.hour,
                                    currentConfig.minute
                                )
                            } else {
                                AlarmReceiver.cancelAlarm(this@MainActivity)
                            }
                        }
                    },
                    onRunNow = {
                        lifecycleScope.launch {
                            try {
                                val currentConfig = preferencesManager.configFlow.first()
                                healthConnectManager.insertStepsWithSession(
                                    stepCount = currentConfig.stepCount,
                                    durationMinutes = currentConfig.durationMinutes
                                )
                                preferencesManager.updateLastExecution(System.currentTimeMillis())
                            } catch (e: Exception) {
                                // Error handled in UI via lastExecution timestamp not changing
                            }
                        }
                    }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        checkBatteryOptimization()
        lifecycleScope.launch {
            hasHealthPermissions.value = healthConnectManager.hasAllPermissions()
        }
        healthConnectAvailable.value = healthConnectManager.isAvailable()
    }

    private fun requestHealthPermissions() {
        if (healthConnectManager.needsUpdate()) {
            startActivity(healthConnectManager.getInstallIntent())
        } else if (healthConnectManager.isAvailable()) {
            requestPermissions.launch(HealthConnectManager.REQUIRED_PERMISSIONS)
        }
    }

    private fun installHealthConnect() {
        startActivity(healthConnectManager.getInstallIntent())
    }

    private fun requestBatteryExemption() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                val intent = Intent(
                    Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                    Uri.parse("package:$packageName")
                )
                startActivity(intent)
            }
        }
    }

    private fun checkBatteryOptimization() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
            batteryOptimizationDisabled.value = pm.isIgnoringBatteryOptimizations(packageName)
        } else {
            batteryOptimizationDisabled.value = true
        }
    }

    private fun saveAlarmPrefs(hour: Int, minute: Int, enabled: Boolean) {
        getSharedPreferences("fake_steps_alarm", Context.MODE_PRIVATE).edit().apply {
            putInt("alarm_hour", hour)
            putInt("alarm_minute", minute)
            putBoolean("is_enabled", enabled)
            apply()
        }
    }
}
