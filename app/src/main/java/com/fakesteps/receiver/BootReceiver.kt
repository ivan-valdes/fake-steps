package com.fakesteps.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.i(TAG, "Boot completed, rescheduling alarm")

            val prefs = context.getSharedPreferences("fake_steps_alarm", Context.MODE_PRIVATE)
            val isEnabled = prefs.getBoolean("is_enabled", false)
            val hour = prefs.getInt("alarm_hour", 8)
            val minute = prefs.getInt("alarm_minute", 0)

            if (isEnabled) {
                AlarmReceiver.scheduleAlarm(context, hour, minute)
                Log.i(TAG, "Alarm rescheduled for $hour:$minute after boot")
            } else {
                Log.i(TAG, "Alarm scheduling skipped - disabled")
            }
        }
    }
}
