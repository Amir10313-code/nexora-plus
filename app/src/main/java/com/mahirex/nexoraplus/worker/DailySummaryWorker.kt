package com.mahirex.nexoraplus.worker

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.mahirex.nexoraplus.MainActivity
import com.mahirex.nexoraplus.NexoraPlusApp
import com.mahirex.nexoraplus.R
import com.mahirex.nexoraplus.core.calendar.CalendarConverter
import com.mahirex.nexoraplus.core.util.Bidi
import com.mahirex.nexoraplus.core.util.PersianDigits
import com.mahirex.nexoraplus.widget.TodayWidgetProvider
import kotlinx.coroutines.flow.first
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

/** Nightly summary: tomorrow's events + tasks (if enabled), plus widget refresh. */
class DailySummaryWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as? NexoraPlusApp ?: return Result.success()
        val container = app.container
        val settings = container.settings.settings.first()
        val db = container.db
        val tomorrow = CalendarConverter.todayEpochDay() + 1

        if (settings.dailyReminder && settings.activeAccountId > 0) {
            val events = db.calendarDao().snapshotFor(settings.activeAccountId).count { it.epochDay == tomorrow }
            val tasks = db.taskDao().snapshotFor(settings.activeAccountId).count { it.dueEpochDay == tomorrow && !it.isDone }
            if (events + tasks > 0) {
                postSummary(applicationContext, events, tasks, settings.languageTag)
            }
        }

        // Refresh the widget every night as well
        TodayWidgetProvider.updateAll(applicationContext)
        return Result.success()
    }

    private fun postSummary(context: Context, events: Int, tasks: Int, languageTag: String) {
        val granted = androidx.core.content.ContextCompat.checkSelfPermission(
            context, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED || android.os.Build.VERSION.SDK_INT < 33
        if (!granted) return

        // Match the app's in-app language (fa/ur use Persian digits) — not the raw system locale.
        val persian = if (languageTag.isEmpty()) {
            java.util.Locale.getDefault().language in setOf("fa", "ur")
        } else languageTag.substringBefore('-') in setOf("fa", "ur")
        val dig = { s: String -> if (persian) PersianDigits.toFa(s) else s }
        val text = Bidi.safe(
            "📅 " + dig(events.toString()) + " " + context.getString(R.string.sec_events) +
                "  •  ✅ " + dig(tasks.toString()) + " " + context.getString(R.string.nav_tasks)
        )
        val intent = PendingIntent.getActivity(
            context, 10,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, NexoraPlusApp.CHANNEL_NEXORA)
            .setSmallIcon(R.drawable.ic_nexora_mark)
            .setContentTitle(context.getString(R.string.daily_summary_title))
            .setContentText(text)
            .setContentIntent(intent)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(1001, notification)
    }

    companion object {
        fun schedule(context: Context) {
            val now = LocalDateTime.now()
            var next = now.toLocalDate().atTime(LocalTime.of(20, 0))
            if (!next.isAfter(now)) next = next.plusDays(1)
            val delay = Duration.between(now, next).toMinutes().coerceAtLeast(1)
            val request = PeriodicWorkRequestBuilder<DailySummaryWorker>(24, TimeUnit.HOURS)
                .setInitialDelay(delay, TimeUnit.MINUTES)
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "nexora_daily_summary",
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }
    }
}
