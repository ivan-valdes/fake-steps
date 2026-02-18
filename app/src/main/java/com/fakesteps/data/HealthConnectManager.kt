package com.fakesteps.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.metadata.Metadata
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit

class HealthConnectManager(private val context: Context) {

    companion object {
        private const val TAG = "HealthConnectManager"

        val REQUIRED_PERMISSIONS = setOf(
            HealthPermission.getWritePermission(StepsRecord::class),
            HealthPermission.getWritePermission(ExerciseSessionRecord::class),
            HealthPermission.getReadPermission(StepsRecord::class),
            HealthPermission.getReadPermission(ExerciseSessionRecord::class)
        )
    }

    private val providerPackageName = "com.google.android.apps.healthdata"

    fun getSdkStatus(): Int {
        return HealthConnectClient.getSdkStatus(context, providerPackageName)
    }

    fun isAvailable(): Boolean {
        return getSdkStatus() == HealthConnectClient.SDK_AVAILABLE
    }

    fun needsUpdate(): Boolean {
        return getSdkStatus() == HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED
    }

    fun getInstallIntent(): Intent {
        val uriString = "market://details?id=$providerPackageName&url=healthconnect%3A%2F%2Fonboarding"
        return Intent(Intent.ACTION_VIEW).apply {
            setPackage("com.android.vending")
            data = Uri.parse(uriString)
            putExtra("overlay", true)
            putExtra("callerId", context.packageName)
        }
    }

    private fun getClient(): HealthConnectClient {
        return HealthConnectClient.getOrCreate(context)
    }

    suspend fun hasAllPermissions(): Boolean {
        return try {
            val client = getClient()
            val granted = client.permissionController.getGrantedPermissions()
            granted.containsAll(REQUIRED_PERMISSIONS)
        } catch (e: Exception) {
            Log.e(TAG, "Error checking permissions", e)
            false
        }
    }

    suspend fun insertStepsWithSession(stepCount: Int, durationMinutes: Int) {
        val client = getClient()

        val endTime = Instant.now()
        val startTime = endTime.minus(durationMinutes.toLong(), ChronoUnit.MINUTES)
        val zoneOffset = ZoneId.systemDefault().rules.getOffset(endTime)

        // Create exercise session (walking)
        val exerciseSession = ExerciseSessionRecord(
            exerciseType = ExerciseSessionRecord.EXERCISE_TYPE_WALKING,
            startTime = startTime,
            endTime = endTime,
            startZoneOffset = zoneOffset,
            endZoneOffset = zoneOffset,
            title = "Daily Walk",
            metadata = Metadata.manualEntry()
        )

        // Create steps record
        val stepsRecord = StepsRecord(
            count = stepCount.toLong(),
            startTime = startTime,
            endTime = endTime,
            startZoneOffset = zoneOffset,
            endZoneOffset = zoneOffset,
            metadata = Metadata.manualEntry()
        )

        try {
            client.insertRecords(listOf(exerciseSession, stepsRecord))
            Log.i(TAG, "Successfully inserted $stepCount steps with ${durationMinutes}min walking session")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to insert records", e)
            throw e
        }
    }
}
