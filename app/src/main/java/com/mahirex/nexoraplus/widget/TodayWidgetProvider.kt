package com.mahirex.nexoraplus.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Intent
import android.content.Context
import android.widget.RemoteViews
import com.mahirex.nexoraplus.MainActivity
import com.mahirex.nexoraplus.R
import com.mahirex.nexoraplus.core.calendar.CalendarConverter
import com.mahirex.nexoraplus.core.util.Bidi
import com.mahirex.nexoraplus.core.util.PersianDigits
import com.mahirex.nexoraplus.data.fortune.FortuneEngine
import com.mahirex.nexoraplus.data.local.NexoraDatabase
import com.mahirex.nexoraplus.data.repo.AppSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Home-screen widget: today's plan (events/tasks counts) + the daily Hafez fortune —
 * exactly the same fortune the header & fortune page show.
 */
class TodayWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // onUpdate runs on the MAIN thread (broadcast). goAsync() hands us a PendingResult so the
        // system keeps the process alive while we read DataStore/Room on Dispatchers.IO —
        // never block the UI thread with runBlocking here (ANR risk every 30 min refresh).
        val pendingResult = goAsync()
        val appContext = context.applicationContext
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Build the RemoteViews ONCE and reuse for every widget id.
                val views = buildViews(appContext)
                for (id in appWidgetIds) {
                    appWidgetManager.updateAppWidget(id, views)
                }
            } catch (e: Exception) {
                // Widget refresh is best-effort; a transient DataStore/Room hiccup must never crash the host.
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        suspend fun buildViews(context: Context): RemoteViews {
            val db = NexoraDatabase.get(context)
            val settings = try {
                com.mahirex.nexoraplus.data.repo.SettingsRepository(context).settings.first()
            } catch (e: Exception) {
                AppSettings()
            }
            val today = CalendarConverter.todayEpochDay()
            val accountId = settings.activeAccountId
            val fortune = FortuneEngine.build(today, accountId)

            var eventsCount = 0
            var tasksCount = 0
            if (accountId > 0) {
                eventsCount = db.calendarDao().snapshotFor(accountId).count { it.epochDay == today }
                tasksCount = db.taskDao().snapshotFor(accountId).count { it.dueEpochDay == today && !it.isDone }
            }

            val persian = if (settings.languageTag.isEmpty()) {
                java.util.Locale.getDefault().language in setOf("fa", "ur")
            } else settings.languageTag.substringBefore('-') == "fa"

            val dig = { s: String -> if (persian) PersianDigits.toFa(s) else s }
            val dateText = CalendarConverter.formatFullDate(
                today,
                settings.calendarType == 0,
                persian
            ) { s -> Bidi.safe(dig(s)) }

            val views = RemoteViews(context.packageName, R.layout.widget_today)
            views.setTextViewText(R.id.widget_date, dateText)
            views.setTextViewText(
                R.id.widget_fortune,
                Bidi.safe(fortune.verse.replace("\n", "  ❖  "))
            )
            views.setTextViewText(
                R.id.widget_events,
                Bidi.safe(dig("📅 ") + " " + dig(eventsCount.toString()) + " " + Bidi.safe(context.getString(R.string.sec_events)))
            )
            views.setTextViewText(
                R.id.widget_tasks,
                Bidi.safe(dig("✅ ") + " " + dig(tasksCount.toString()) + " " + Bidi.safe(context.getString(R.string.nav_tasks)))
            )

            val intent = PendingIntent.getActivity(
                context, 0,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, intent)
            views
        }

        suspend fun updateAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, TodayWidgetProvider::class.java))
            if (ids.isNotEmpty()) {
                val views = buildViews(context)
                for (id in ids) manager.updateAppWidget(id, views)
            }
        }
    }
}
