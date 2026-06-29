package com.habittracker.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.habittracker.MainActivity
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class HabitReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val CHANNEL_ID = "habit_reminder_channel"
        const val CHANNEL_NAME = "Habit Reminders"
        const val KEY_HABIT_TITLE = "habit_title"
        const val KEY_HABIT_ID = "habit_id"

        fun scheduleReminder(
            context: Context,
            habitId: Int,
            habitTitle: String,
            delayMinutes: Long
        ) {
            val data = workDataOf(
                KEY_HABIT_ID to habitId,
                KEY_HABIT_TITLE to habitTitle
            )

            val request = PeriodicWorkRequestBuilder<HabitReminderWorker>(
                24, TimeUnit.HOURS
            )
                .setInitialDelay(delayMinutes, TimeUnit.MINUTES)
                .setInputData(data)
                .addTag("habit_reminder_$habitId")
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "habit_reminder_$habitId",
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }

        fun cancelReminder(context: Context, habitId: Int) {
            WorkManager.getInstance(context).cancelUniqueWork("habit_reminder_$habitId")
        }
    }

    override suspend fun doWork(): Result {
        val habitTitle = inputData.getString(KEY_HABIT_TITLE) ?: return Result.failure()
        val notificationId = inputData.getInt(KEY_HABIT_ID, 0)
        showNotification(habitTitle, notificationId)
        return Result.success()
    }

    private fun showNotification(habitTitle: String, notificationId: Int) {
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(channel)

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, notificationId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("⏰ დროა!")
            .setContentText("$habitTitle — დღევანდელი ჩვევა გელოდება!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(notificationId, notification)
    }
}