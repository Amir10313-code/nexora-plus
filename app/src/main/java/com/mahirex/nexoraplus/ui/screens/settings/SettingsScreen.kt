package com.mahirex.nexoraplus.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.mahirex.nexoraplus.R
import com.mahirex.nexoraplus.data.repo.BackupManager
import com.mahirex.nexoraplus.di.AppContainer
import com.mahirex.nexoraplus.ui.AppViewModelFactory
import com.mahirex.nexoraplus.ui.components.LocalAppFormat
import com.mahirex.nexoraplus.ui.components.NexoraTopBar
import com.mahirex.nexoraplus.ui.components.SectionCard
import com.mahirex.nexoraplus.ui.components.SheetHeader

/** The 16 supported languages, each shown with its NATIVE name. */
data class AppLanguage(val tag: String, val nativeName: String, val flag: String)

val Languages = listOf(
    AppLanguage("", "زبان پیش‌فرض دستگاه", "🌐"),
    AppLanguage("fa", "فارسی", "🇮🇷"),
    AppLanguage("en", "English", "🇬🇧"),
    AppLanguage("ar", "العربية", "🇸🇦"),
    AppLanguage("tr", "Türkçe", "🇹🇷"),
    AppLanguage("de", "Deutsch", "🇩🇪"),
    AppLanguage("fr", "Français", "🇫🇷"),
    AppLanguage("es", "Español", "🇪🇸"),
    AppLanguage("ru", "Русский", "🇷🇺"),
    AppLanguage("zh", "中文", "🇨🇳"),
    AppLanguage("ja", "日本語", "🇯🇵"),
    AppLanguage("hi", "हिन्दी", "🇮🇳"),
    AppLanguage("ur", "اردو", "🇵🇰"),
    AppLanguage("pt", "Português", "🇵🇹"),
    AppLanguage("it", "Italiano", "🇮🇹"),
    AppLanguage("id", "Bahasa Indonesia", "🇮🇩"),
    AppLanguage("ku", "کوردی", "🏴")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavHostController, factory: AppViewModelFactory) {
    val vm: SettingsViewModel = viewModel(factory = factory)
    val settings by vm.settings.collectAsState()
    val updating by vm.occasionUpdating.collectAsState()
    val working by vm.backupWorking.collectAsState()
    val context = LocalContext.current
    val fmt = LocalAppFormat.current

    var showLangs by remember { mutableStateOf(false) }
    var importConfirmed by remember { mutableStateOf(false) }
    var toast by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(toast) {
        toast?.let {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_SHORT).show()
            toast = null
        }
    }

    // SAF launchers — all file I/O is delegated to the ViewModel (Dispatchers.IO);
    // SAF callbacks run on the main thread, so they must not read/write files directly.
    var pendingExport by remember { mutableStateOf<String?>(null) }
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        val content = pendingExport
        if (uri != null && content != null) {
            vm.exportTo(uri, content) { ok ->
                toast = context.getString(if (ok) R.string.backup_ok else R.string.backup_failed)
            }
        }
        pendingExport = null
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            vm.importFrom(uri) { ok ->
                toast = context.getString(if (ok) R.string.backup_ok else R.string.backup_failed)
            }
        } else toast = context.getString(R.string.backup_failed)
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 40.dp)
    ) {
        NexoraTopBar(navController, stringResource(R.string.settings_title))

        Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {

            // ── Appearance ──
            SectionCard(title = stringResource(R.string.settings_theme)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        0 to Triple(stringResource(R.string.theme_light), "☀️"),
                        1 to Triple(stringResource(R.string.theme_dark), "🌙"),
                        2 to Triple(stringResource(R.string.theme_system), "⚙️")
                    ).forEach { (mode, pair) ->
                        val selected = settings.themeMode == mode
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { vm.setTheme(mode) }
                        ) {
                            Column(
                                Modifier.padding(vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(pair.second, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    pair.first,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (selected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1, overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            // ── Language ──
            SectionCard(title = stringResource(R.string.settings_language)) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showLangs = true }
                ) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Language, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(10.dp))
                        val current = Languages.firstOrNull { it.tag == settings.languageTag }
                        Text(
                            current?.let { "${it.flag}  ${it.nativeName}" }
                                ?: "${"🌐"}  ${stringResource(R.string.lang_default)}",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )
                        Text("▾", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // ── Calendar & time ──
            SectionCard(title = stringResource(R.string.settings_calendar)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        0 to stringResource(R.string.cal_jalali),
                        1 to stringResource(R.string.cal_gregorian)
                    ).forEach { (mode, label) ->
                        val selected = settings.calendarType == mode
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { vm.setCalendar(mode) }
                        ) {
                            Row(
                                Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Filled.CalendarMonth, null,
                                    tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    label,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Schedule, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(R.string.settings_time24), style = MaterialTheme.typography.bodyLarge)
                        Text(
                            stringResource(R.string.settings_time24_desc),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = settings.time24,
                        onCheckedChange = { vm.setTime24(it) },
                        colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary)
                    )
                }
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Event, null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(R.string.settings_reminder), style = MaterialTheme.typography.bodyLarge)
                        Text(
                            stringResource(R.string.settings_reminder_desc),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = settings.dailyReminder,
                        onCheckedChange = { vm.setReminder(it) },
                        colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.tertiary)
                    )
                }
            }

            // ── Occasions update (only online feature) ──
            SectionCard(title = stringResource(R.string.occasion_update), accent = MaterialTheme.colorScheme.tertiary) {
                Text(
                    stringResource(R.string.occasion_update_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(10.dp))
                OutlinedButton(
                    onClick = {
                        vm.updateOccasions { ok, version ->
                            toast = if (ok) context.getString(R.string.occasion_updated_toast, version)
                            else context.getString(R.string.occasion_update_failed)
                        }
                    },
                    enabled = !updating,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (updating) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.occasion_updating))
                    } else {
                        Icon(Icons.Filled.CloudDownload, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.occasion_update))
                    }
                }
            }

            // ── Backup & restore ──
            SectionCard(title = stringResource(R.string.settings_backup), accent = MaterialTheme.colorScheme.secondary) {
                OutlinedButton(
                    onClick = {
                        vm.export("all") { json ->
                            pendingExport = json
                            exportLauncher.launch(BackupManager.suggestedFileName())
                        }
                    },
                    enabled = !working,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.CloudUpload, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.backup_export_all))
                }
                Spacer(Modifier.height(6.dp))
                OutlinedButton(
                    onClick = {
                        vm.export("account") { json ->
                            pendingExport = json
                            exportLauncher.launch(BackupManager.suggestedFileName())
                        }
                    },
                    enabled = !working,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.CloudUpload, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.backup_export_one))
                }
                Spacer(Modifier.height(6.dp))
                OutlinedButton(
                    onClick = { importConfirmed = true },
                    enabled = !working,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.UploadFile, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.backup_import))
                }
            }
            Spacer(Modifier.height(30.dp))
        }
    }

    // ── Language picker sheet ──
    if (showLangs) {
        ModalBottomSheet(onDismissRequest = { showLangs = false }) {
            Column(Modifier.padding(horizontal = 20.dp)) {
                SheetHeader(stringResource(R.string.settings_language)) { showLangs = false }
                Column(
                    Modifier
                        .verticalScroll(rememberScrollState())
                        .height(420.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Languages.forEach { lang ->
                        val selected = settings.languageTag == lang.tag
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    vm.setLanguage(lang.tag)
                                    showLangs = false
                                }
                        ) {
                            Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(lang.flag, style = MaterialTheme.typography.titleLarge)
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    if (lang.tag.isEmpty()) stringResource(R.string.lang_default) else lang.nativeName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                if (selected) Text("✓", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }

    // ── Import confirm ──
    if (importConfirmed) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { importConfirmed = false },
            title = { Text(stringResource(R.string.backup_import)) },
            text = { Text(stringResource(R.string.backup_import_confirm)) },
            confirmButton = {
                TextButton(onClick = {
                    importConfirmed = false
                    importLauncher.launch(arrayOf("application/json", "text/*", "application/octet-stream"))
                }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { importConfirmed = false }) { Text(stringResource(R.string.cancel)) }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }
}
