package com.mahirex.nexoraplus.ui.screens.focus

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mahirex.nexoraplus.core.calendar.CalendarConverter
import com.mahirex.nexoraplus.di.AppContainer
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class FocusViewModel(private val container: AppContainer) : ViewModel() {

    val today: Long = CalendarConverter.todayEpochDay()

    val activeId: StateFlow<Long> = container.settings.settings.map { it.activeAccountId }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0L)

    val sessionsToday: StateFlow<Int> = activeId
        .flatMapLatest { id -> container.focus.sessionsToday(id, today) }
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val minutesToday: StateFlow<Int> = activeId
        .flatMapLatest { id -> container.focus.minutesToday(id, today) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val durations = listOf(15, 25, 45, 60, 90)
    val selectedMinutes = MutableStateFlow(25)
    val running = MutableStateFlow(false)
    val remainingSeconds = MutableStateFlow(25 * 60)
    val dndEnabled = MutableStateFlow(false)

    private var job: Job? = null

    /** True only while this app is actively holding the device in DND, so we never clobber a user-set mode. */
    private var dndAppliedByApp = false

    fun selectMinutes(m: Int) {
        if (running.value) return
        selectedMinutes.value = m
        remainingSeconds.value = m * 60
    }

    fun isDndAccessGranted(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return nm.isNotificationPolicyAccessGranted
    }

    fun requestDndAccess(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pendingDndRequest = true
            context.startActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
        }
    }

    /** True while the user is away in system settings granting the DND access. */
    private var pendingDndRequest = false

    /** Called on every ON_RESUME: applies a granted pending DND request, and turns
     *  the switch off if the user revoked the access while we believed it was on. */
    fun onResumed(context: Context) {
        val granted = isDndAccessGranted(context)
        if (pendingDndRequest) {
            pendingDndRequest = false
            if (granted) dndEnabled.value = true
        } else if (dndEnabled.value && !granted) {
            dndEnabled.value = false
        }
    }

    fun start(context: Context, onFinished: () -> Unit) {
        if (running.value) return
        if (dndEnabled.value && isDndAccessGranted(context)) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY)
            dndAppliedByApp = true
        }
        running.value = true
        job = viewModelScope.launch {
            while (isActive && running.value && remainingSeconds.value > 0) {
                delay(1000)
                if (running.value) remainingSeconds.value -= 1
            }
            if (running.value && remainingSeconds.value <= 0) {
                finish(context, record = true)
                onFinished()
            }
        }
    }

    fun stop(context: Context) {
        // Cancel the ticking coroutine first so it can't keep counting (or fire a phantom
        // "session finished") after the user has stopped the timer manually.
        job?.cancel()
        job = null
        val elapsed = selectedMinutes.value * 60 - remainingSeconds.value
        viewModelScope.launch {
            finish(context, record = elapsed >= 60, minutesOverride = elapsed / 60)
        }
    }

    private suspend fun finish(context: Context, record: Boolean, minutesOverride: Int? = null) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // Restore the normal filter only if THIS app enabled DND — never undo a mode the user set manually.
        if (dndAppliedByApp && isDndAccessGranted(context)) {
            nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
        }
        dndAppliedByApp = false
        val minutes = minutesOverride ?: selectedMinutes.value
        val id = activeId.value
        if (record && id > 0 && minutes > 0) {
            container.focus.add(id, today, minutes, container.appContext.getString(com.mahirex.nexoraplus.R.string.focus_session_label))
        }
        running.value = false
        remainingSeconds.value = selectedMinutes.value * 60
    }

    override fun onCleared() {
        job?.cancel()
        super.onCleared()
    }
}
