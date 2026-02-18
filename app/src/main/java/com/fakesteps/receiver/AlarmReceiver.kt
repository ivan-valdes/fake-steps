package com.fakesteps.receiver

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.fakesteps.worker.StepsWorker
import java.util.Calendar

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "AlarmReceiver"
        private const val REQUEST_CODE = 1001

        fun scheduleAlarm(context: Context, hour: Int, minute: Int) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            val intent = Intent(context, AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val target = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (before(Calendar.getInstance())) {
                    add(Calendar.DAY_OF_MONTH, 1)
                }
            }

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            target.timeInMillis,
                            pendingIntent
                        )
                        Log.i(TAG, "Exact alarm scheduled for ${target.time}")
                    } else {
                        // Fallback to inexact alarm
                        alarmManager.setAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            target.timeInMillis,
                            pendingIntent
                        )
                        Log.i(TAG, "Inexact alarm scheduled for ${target.time} (exact alarms not permitted)")
                    }
                } else {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        target.timeInMillis,
                        pendingIntent
                    )
                    Log.i(TAG, "Exact alarm scheduled for ${target.time}")
                }
            } catch (e: SecurityException) {
                Log.e(TAG, "SecurityException scheduling alarm, using inexact", e)
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    target.timeInMillis,
                    pendingIntent
                )
            }
        }

        fun cancelAlarm(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
            Log.i(TAG, "Alarm cancelled")
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.i(TAG, "Alarm received, enqueueing steps work")

        // Enqueue one-time work to insert steps
        val workRequest = OneTimeWorkRequestBuilder<StepsWorker>()
            .addTag(StepsWorker.WORK_NAME)
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)

        // Reschedule for tomorrow at the same time
        val prefs = context.getSharedPreferences("fake_steps_alarm", Context.MODE_PRIVATE)
        val hour = prefs.getInt("alarm_hour", 8)
        val minute = prefs.getInt("alarm_minute", 0)

        scheduleAlarm(context, hour, minute)
        Log.i(TAG, "Next alarm rescheduled for $hour:$minute")
    }
}
