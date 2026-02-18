package com.fakesteps.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.fakesteps.data.HealthConnectManager
import com.fakesteps.data.PreferencesManager
import kotlinx.coroutines.flow.first

class StepsWorker(
    private val appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    companion object {
        const val TAG = "StepsWorker"
        const val WORK_NAME = "fake_steps_daily_work"
    }

    override suspend fun doWork(): Result {
        Log.i(TAG, "Starting steps insertion work")

        val preferencesManager = PreferencesManager(appContext)
        val healthConnectManager = HealthConnectManager(appContext)

        return try {
            val config = preferencesManager.configFlow.first()

            if (!config.isEnabled) {
                Log.i(TAG, "Steps insertion is disabled, skipping")
                return Result.success()
            }

            if (!healthConnectManager.isAvailable()) {
                Log.e(TAG, "Health Connect is not available")
                return Result.failure()
            }

            if (!healthConnectManager.hasAllPermissions()) {
                Log.e(TAG, "Missing Health Connect permissions")
                return Result.failure()
            }

            healthConnectManager.insertStepsWithSession(
                stepCount = config.stepCount,
                durationMinutes = config.durationMinutes
            )

            preferencesManager.updateLastExecution(System.currentTimeMillis())

            Log.i(TAG, "Steps insertion completed successfully")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Steps insertion failed", e)
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
}
