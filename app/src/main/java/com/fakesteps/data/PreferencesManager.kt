package com.fakesteps.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "fake_steps_prefs")

data class StepsConfig(
    val hour: Int = 8,
    val minute: Int = 0,
    val stepCount: Int = 6000,
    val durationMinutes: Int = 30,
    val isEnabled: Boolean = false,
    val lastExecutionTimestamp: Long = 0L
)

class PreferencesManager(private val context: Context) {

    companion object {
        private val KEY_HOUR = intPreferencesKey("scheduled_hour")
        private val KEY_MINUTE = intPreferencesKey("scheduled_minute")
        private val KEY_STEP_COUNT = intPreferencesKey("step_count")
        private val KEY_DURATION_MINUTES = intPreferencesKey("duration_minutes")
        private val KEY_ENABLED = booleanPreferencesKey("is_enabled")
        private val KEY_LAST_EXECUTION = longPreferencesKey("last_execution_timestamp")
    }

    val configFlow: Flow<StepsConfig> = context.dataStore.data.map { prefs ->
        StepsConfig(
            hour = prefs[KEY_HOUR] ?: 8,
            minute = prefs[KEY_MINUTE] ?: 0,
            stepCount = prefs[KEY_STEP_COUNT] ?: 6000,
            durationMinutes = prefs[KEY_DURATION_MINUTES] ?: 30,
            isEnabled = prefs[KEY_ENABLED] ?: false,
            lastExecutionTimestamp = prefs[KEY_LAST_EXECUTION] ?: 0L
        )
    }

    suspend fun updateScheduledTime(hour: Int, minute: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_HOUR] = hour
            prefs[KEY_MINUTE] = minute
        }
    }

    suspend fun updateStepCount(count: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_STEP_COUNT] = count
        }
    }

    suspend fun updateDurationMinutes(minutes: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_DURATION_MINUTES] = minutes
        }
    }

    suspend fun updateEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ENABLED] = enabled
        }
    }

    suspend fun updateLastExecution(timestamp: Long) {
        context.dataStore.edit { prefs ->
            prefs[KEY_LAST_EXECUTION] = timestamp
        }
    }
}
