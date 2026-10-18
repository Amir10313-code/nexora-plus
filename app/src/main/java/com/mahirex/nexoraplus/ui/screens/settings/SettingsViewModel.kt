package com.mahirex.nexoraplus.ui.screens.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mahirex.nexoraplus.data.repo.AppSettings
import com.mahirex.nexoraplus.data.occasions.OccasionSyncManager
import com.mahirex.nexoraplus.di.AppContainer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsViewModel(private val container: AppContainer) : ViewModel() {

    val settings: StateFlow<AppSettings> = container.settings.settings
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings())

    val occasionUpdating = MutableStateFlow(false)
    val backupWorking = MutableStateFlow(false)

    fun setTheme(v: Int) = viewModelScope.launch { container.settings.setThemeMode(v) }
    fun setLanguage(tag: String) = viewModelScope.launch { container.settings.setLanguageTag(tag) }
    fun setCalendar(v: Int) = viewModelScope.launch { container.settings.setCalendarType(v) }
    fun setTime24(v: Boolean) = viewModelScope.launch { container.settings.setTime24(v) }
    fun setReminder(v: Boolean) = viewModelScope.launch {
        container.settings.setDailyReminder(v)
        if (v) {
            com.mahirex.nexoraplus.worker.DailySummaryWorker.schedule(container.appContext)
        } else {
            androidx.work.WorkManager.getInstance(container.appContext)
                .cancelUniqueWork("nexora_daily_summary")
        }
    }

    /**
     * The only online feature: refresh the occasions calendar and apply it
     * to ALL accounts (occasions are stored once, shared).
     */
    fun updateOccasions(onResult: (Boolean, String) -> Unit) = viewModelScope.launch {
        occasionUpdating.value = true
        val result = withContext(Dispatchers.IO) {
            try {
                val file = OccasionSyncManager.fetch()
                container.occasions.replaceWith(file)
                container.settings.setOccasionVersion(file.version)
                file.version.toString()
            } catch (e: Exception) {
                null
            }
        }
        occasionUpdating.value = false
        onResult(result != null, result ?: "")
    }

    fun export(mode: String, create: (String) -> Unit) = viewModelScope.launch {
        backupWorking.value = true
        val id = container.activeIdCompat()
        val json = withContext(Dispatchers.IO) {
            if (mode == "all") container.backup.exportAll() else container.backup.exportAccount(id)
        }
        backupWorking.value = false
        create(json)
    }

    fun import(text: String, onResult: (Boolean) -> Unit) = viewModelScope.launch {
        backupWorking.value = true
        val id = container.activeIdCompat()
        val ok = withContext(Dispatchers.IO) { container.backup.import(text, id) }
        backupWorking.value = false
        onResult(ok)
    }

    /** Writes the exported JSON through the SAF Uri — file I/O stays off the main thread. */
    fun exportTo(uri: Uri, json: String, onResult: (Boolean) -> Unit) = viewModelScope.launch {
        backupWorking.value = true
        val ok = withContext(Dispatchers.IO) {
            try {
                container.appContext.contentResolver.openOutputStream(uri)?.use { os ->
                    os.write(json.toByteArray(Charsets.UTF_8))
                    os.flush()
                } != null
            } catch (e: Exception) {
                false
            }
        }
        backupWorking.value = false
        onResult(ok)
    }

    /** Reads the picked backup file through the SAF Uri on IO, then imports it. */
    fun importFrom(uri: Uri, onResult: (Boolean) -> Unit) = viewModelScope.launch {
        backupWorking.value = true
        val ok = withContext(Dispatchers.IO) {
            try {
                val text = container.appContext.contentResolver.openInputStream(uri)
                    ?.use { it.readBytes().toString(Charsets.UTF_8) }
                text?.let { container.backup.import(it, container.activeIdCompat()) } ?: false
            } catch (e: Exception) {
                false
            }
        }
        backupWorking.value = false
        onResult(ok)
    }
}
