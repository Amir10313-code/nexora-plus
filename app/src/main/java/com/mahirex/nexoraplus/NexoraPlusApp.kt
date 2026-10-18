package com.mahirex.nexoraplus

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.mahirex.nexoraplus.di.AppContainer
import com.mahirex.nexoraplus.widget.TodayWidgetProvider
import com.mahirex.nexoraplus.worker.DailySummaryWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class NexoraPlusApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        createNotificationChannel()
        container.bootstrap()

        // Keep the per-app language in sync with the stored preference (16 languages + device default).
        // setApplicationLocales must run on the MAIN thread.
        container.appScope.launch(Dispatchers.Main.immediate) {
            container.settings.settings
                .map { it.languageTag }
                .distinctUntilChanged()
                .collect { tag ->
                    val target = if (tag.isEmpty()) LocaleListCompat.getEmptyLocaleList()
                    else LocaleListCompat.forLanguageTags(tag)
                    if (AppCompatDelegate.getApplicationLocales().toLanguageTags() != target.toLanguageTags()) {
                        AppCompatDelegate.setApplicationLocales(target)
                    }
                }
        }

        // Refresh the home-screen widget after the app boots + keep the nightly worker scheduled.
        container.appScope.launch {
            TodayWidgetProvider.updateAll(this@NexoraPlusApp)
            DailySummaryWorker.schedule(this@NexoraPlusApp)
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_NEXORA,
            getString(R.string.notification_channel),
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    companion object {
        const val CHANNEL_NEXORA = "nexora_main"
    }
}
