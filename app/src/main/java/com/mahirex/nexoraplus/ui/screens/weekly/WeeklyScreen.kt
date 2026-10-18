package com.mahirex.nexoraplus.ui.screens.weekly

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestoreFromTrash
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.mahirex.nexoraplus.R
import com.mahirex.nexoraplus.core.util.TimeFormat
import com.mahirex.nexoraplus.data.local.entity.ScheduleEntryEntity
import com.mahirex.nexoraplus.data.local.entity.ScheduleSlotEntity
import com.mahirex.nexoraplus.di.AppContainer
import com.mahirex.nexoraplus.ui.AppViewModelFactory
import com.mahirex.nexoraplus.ui.components.ConfirmDeleteDialog
import com.mahirex.nexoraplus.ui.components.LocalAppFormat
import com.mahirex.nexoraplus.ui.components.NexoraTopBar
import com.mahirex.nexoraplus.ui.components.SheetHeader
import com.mahirex.nexoraplus.ui.pickers.LuxTimePickerDialog
import com.mahirex.nexoraplus.ui.theme.PaletteColors
import com.mahirex.nexoraplus.ui.theme.colorFromHex
import com.mahirex.nexoraplus.ui.theme.colorToHex

private val dayResShort = listOf(
    R.string.day_sat_short, R.string.day_sun_short, R.string.day_mon_short,
    R.string.day_tue_short, R.string.day_wed_short, R.string.day_thu_short, R.string.day_fri_short
)
private val dayResFull = listOf(
    R.string.day_sat, R.string.day_sun, R.string.day_mon,
    R.string.day_tue, R.string.day_wed, R.string.day_thu, R.string.day_fri
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyScreen(navController: NavHostController, factory: AppViewModelFactory) {
    val vm: WeeklyViewModel = viewModel(factory = factory)
    val fmt = LocalAppFormat.current
    val haptic = LocalHapticFeedback.current
    val context = androidx.compose.ui.platform.LocalContext.current

    val slots by vm.slots.collectAsState()
    val entries by vm.entries.collectAsState()
    val presets by vm.presets.collectAsState()
    val time24 by vm.time24.collectAsState()

    var slotEditor by remember { mutableStateOf<ScheduleSlotEntity?>(null) }
    var entryEditor by remember { mutableStateOf<Pair<Int, ScheduleSlotEntity>?>(null) }
    var editEntry by remember { mutableStateOf<ScheduleEntryEntity?>(null) }
    var deleteSlot by remember { mutableStateOf<ScheduleSlotEntity?>(null) }
    var showPresetSave by remember { mutableStateOf(false) }
    var showPresets by remember { mutableStateOf(false) }
    var showResetConfirm by remember { mutableStateOf(false) }
    var toast by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(toast) {
        toast?.let {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_SHORT).show()
            toast = null
        }
    }

    Column(Modifier.fillMaxSize()) {
        NexoraTopBar(
            navController = navController,
            title = stringResource(R.string.weekly_title),
            subtitle = stringResource(R.string.nav_weekly)
        )

        // Toolbar
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { slotEditor = ScheduleSlotEntity(accountId = 0, title = "", startMinutes = 8 * 60, endMinutes = 10 * 60, colorHex = "#5B3FE0", orderIndex = slots.size) },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.Add, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.weekly_add_slot), style = MaterialTheme.typography.labelLarge, maxLines = 1)
            }
            OutlinedButton(
                onClick = { showPresetSave = true },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.BookmarkAdd, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.weekly_save_as_default), style = MaterialTheme.typography.labelLarge, maxLines = 1)
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(onClick = { showPresets = true }, modifier = Modifier.weight(1f)) {
                Icon(Icons.Filled.Edit, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.weekly_presets), style = MaterialTheme.typography.labelLarge, maxLines = 1)
            }
            OutlinedButton(onClick = { showResetConfirm = true }, modifier = Modifier.weight(1f)) {
                Icon(Icons.Filled.Refresh, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.weekly_reset_defaults), style = MaterialTheme.typography.labelLarge, maxLines = 1)
            }
        }

        Spacer(Modifier.height(12.dp))

        // ─────────────── Table ───────────────
        val rowScroll = rememberScrollState()
        Row(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rowScroll)
                .padding(horizontal = 12.dp)
        ) {
            // Sticky first column (time blocks)
            Column(Modifier.width(96.dp)) {
                TableHeaderCell(
                    text = stringResource(R.string.weekly_col_time),
                    modifier = Modifier.fillMaxWidth(),
                    header = true
                )
                slots.forEach { slot ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .padding(1.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(colorFromHex(slot.colorHex).copy(alpha = 0.14f))
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                slotEditor = slot
                            }
                            .padding(6.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            slot.title,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            fmt.time(slot.startMinutes) + "\n" + fmt.time(slot.endMinutes),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = MaterialTheme.typography.labelSmall.lineHeight
                        )
                        Row {
                            IconButton(
                                onClick = { vm.moveSlot(slot, -1) },
                                modifier = Modifier.size(18.dp)
                            ) {
                                Icon(Icons.Filled.KeyboardArrowUp, null, modifier = Modifier.size(14.dp))
                            }
                            IconButton(
                                onClick = { vm.moveSlot(slot, 1) },
                                modifier = Modifier.size(18.dp)
                            ) {
                                Icon(Icons.Filled.KeyboardArrowDown, null, modifier = Modifier.size(14.dp))
                            }
                            IconButton(
                                onClick = { deleteSlot = slot },
                                modifier = Modifier.size(18.dp)
                            ) {
                                Icon(
                                    Icons.Outlined.Delete, null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.width(4.dp))

            // Day columns
            Row(Modifier.horizontalScroll(rememberScrollState())) {
                dayResShort.forEachIndexed { day, res ->
                    Column(Modifier.width(76.dp)) {
                        TableHeaderCell(text = stringResource(res), header = false)
                        slots.forEach { slot ->
                            val cellEntries = entries.filter { it.dayIndex == day && it.slotId == slot.id }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(64.dp)
                                    .padding(1.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                                    .border(
                                        0.7.dp,
                                        MaterialTheme.colorScheme.outline.copy(alpha = 0.18f),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        entryEditor = day to slot
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (cellEntries.isNotEmpty()) {
                                    Column(
                                        Modifier
                                            .fillMaxSize()
                                            .padding(3.dp),
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        cellEntries.take(2).forEach { e ->
                                            Surface(
                                                color = colorFromHex(e.colorHex).copy(alpha = 0.92f),
                                                shape = RoundedCornerShape(7.dp),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 1.dp)
                                                    .clickable { editEntry = e }
                                            ) {
                                                Text(
                                                    e.title,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Color.White,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 3.dp)
                                                )
                                            }
                                        }
                                        if (cellEntries.size > 2) {
                                            Text(
                                                "+" + fmt.num(cellEntries.size - 2),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(90.dp))
    }

    // ─────────────── Editors & dialogs ───────────────
    slotEditor?.let { slot ->
        SlotEditorSheet(
            initial = slot,
            isNew = slots.none { it.id == slot.id },
            time24 = time24,
            onSave = { vm.saveSlot(it); slotEditor = null },
            onDismiss = { slotEditor = null }
        )
    }

    entryEditor?.let { (day, slot) ->
        EntryEditorSheet(
            initial = null,
            baseEntry = ScheduleEntryEntity(accountId = 0, dayIndex = day, slotId = slot.id, title = ""),
            dayIndex = day,
            slotTitle = slot.title,
            existing = entries.filter { it.dayIndex == day && it.slotId == slot.id },
            onSave = { vm.saveEntry(it); entryEditor = null },
            onDelete = { vm.deleteEntry(it); entryEditor = null },
            onDismiss = { entryEditor = null }
        )
    }

    editEntry?.let { e ->
        EntryEditorSheet(
            initial = e,
            baseEntry = e,
            dayIndex = e.dayIndex,
            slotTitle = slots.firstOrNull { it.id == e.slotId }?.title ?: "",
            existing = emptyList(),
            onSave = { vm.saveEntry(it); editEntry = null },
            onDelete = { vm.deleteEntry(it); editEntry = null },
            onDismiss = { editEntry = null }
        )
    }

    deleteSlot?.let { slot ->
        ConfirmDeleteDialog(
            itemName = slot.title,
            onConfirm = { vm.deleteSlot(slot); deleteSlot = null },
            onDismiss = { deleteSlot = null }
        )
    }

    if (showPresetSave) {
        var name by remember { mutableStateOf("") }
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showPresetSave = false },
            title = { Text(stringResource(R.string.weekly_save_as_default)) },
            text = {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.weekly_preset_name)) },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (name.isNotBlank()) {
                            vm.saveAsPreset(name.trim())
                            toast = context.getString(R.string.weekly_saved_preset, name.trim())
                        }
                        showPresetSave = false
                    }
                ) { Text(stringResource(R.string.save)) }
            },
            dismissButton = { TextButton(onClick = { showPresetSave = false }) { Text(stringResource(R.string.cancel)) } },
            shape = RoundedCornerShape(24.dp)
        )
    }

    if (showPresets) {
        ModalBottomSheet(onDismissRequest = { showPresets = false }) {
            Column(Modifier.padding(20.dp)) {
                SheetHeader(stringResource(R.string.weekly_presets)) { showPresets = false }
                if (presets.isEmpty()) {
                    Text(
                        stringResource(R.string.empty_message),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                presets.forEach { p ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(p.name, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                            TextButton(onClick = {
                                vm.applyPreset(p)
                                showPresets = false
                            }) { Text(stringResource(R.string.weekly_apply)) }
                            IconButton(onClick = { vm.deletePreset(p) }) {
                                Icon(Icons.Filled.Delete, stringResource(R.string.weekly_delete_preset), tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }

    if (showResetConfirm) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text(stringResource(R.string.weekly_reset_defaults)) },
            text = { Text(stringResource(R.string.weekly_reset_confirm)) },
            confirmButton = {
                TextButton(onClick = { vm.resetDefaults(); showResetConfirm = false }) {
                    Text(stringResource(R.string.ok), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { showResetConfirm = false }) { Text(stringResource(R.string.cancel)) } },
            shape = RoundedCornerShape(24.dp)
        )
    }
}

@Composable
private fun TableHeaderCell(text: String, modifier: Modifier = Modifier, header: Boolean) {
    Box(
        modifier = modifier
            .height(34.dp)
            .padding(1.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (header) MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                else MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelMedium,
            color = if (header) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
            maxLines = 1
        )
    }
}

// ─────────────────────── Slot editor ───────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SlotEditorSheet(
    initial: ScheduleSlotEntity,
    isNew: Boolean,
    time24: Boolean,
    onSave: (ScheduleSlotEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf(initial.title) }
    var start by remember { mutableStateOf(initial.startMinutes) }
    var end by remember { mutableStateOf(initial.endMinutes) }
    var color by remember { mutableStateOf(initial.colorHex) }
    var pickStart by remember { mutableStateOf(false) }
    var pickEnd by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(horizontal = 20.dp)) {
            SheetHeader(stringResource(if (isNew) R.string.weekly_add_slot else R.string.weekly_edit_slot), onDismiss)
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(stringResource(R.string.weekly_slot_name)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { pickStart = true }
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text(
                            stringResource(R.string.weekly_start_time),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            com.mahirex.nexoraplus.core.util.TimeFormat.label(
                                start, time24, com.mahirex.nexoraplus.ui.components.LocalAppFormat.current.persianDigits
                            ),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { pickEnd = true }
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text(
                            stringResource(R.string.weekly_end_time),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            com.mahirex.nexoraplus.core.util.TimeFormat.label(
                                end, time24, com.mahirex.nexoraplus.ui.components.LocalAppFormat.current.persianDigits
                            ),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            com.mahirex.nexoraplus.ui.components.ColorSwatches(selected = color, onSelect = { color = it })
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    if (title.isNotBlank() && end > start) {
                        onSave(initial.copy(title = title.trim(), startMinutes = start, endMinutes = end, colorHex = color))
                    } else error = true
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text(stringResource(R.string.save)) }
            if (error) {
                Text(
                    stringResource(R.string.weekly_invalid_time),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall
                )
            }
            Spacer(Modifier.height(28.dp))
        }
    }

    if (pickStart) {
        LuxTimePickerDialog(
            time24 = time24,
            initialMinutes = start,
            onConfirm = { start = it; pickStart = false },
            onDismiss = { pickStart = false }
        )
    }
    if (pickEnd) {
        LuxTimePickerDialog(
            time24 = time24,
            initialMinutes = end,
            onConfirm = { end = it; pickEnd = false },
            onDismiss = { pickEnd = false }
        )
    }
}

// ─────────────────────── Entry editor ───────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EntryEditorSheet(
    initial: ScheduleEntryEntity?,
    baseEntry: ScheduleEntryEntity,
    dayIndex: Int,
    slotTitle: String,
    existing: List<ScheduleEntryEntity>,
    onSave: (ScheduleEntryEntity) -> Unit,
    onDelete: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var note by remember { mutableStateOf(initial?.note ?: "") }
    var color by remember { mutableStateOf(initial?.colorHex ?: "#7C5CFF") }
    var confirmDelete by remember { mutableStateOf<Long?>(null) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(horizontal = 20.dp)) {
            SheetHeader(
                stringResource(if (initial == null) R.string.weekly_add_entry else R.string.weekly_edit_entry),
                onDismiss
            )
            Text(
                "${stringResource(dayResFull[dayIndex])} • $slotTitle",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(stringResource(R.string.weekly_entry_hint)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text(stringResource(R.string.weekly_note_hint)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )
            Spacer(Modifier.height(12.dp))
            com.mahirex.nexoraplus.ui.components.ColorSwatches(selected = color, onSelect = { color = it })
            Spacer(Modifier.height(14.dp))

            if (existing.isNotEmpty() && initial == null) {
                Text(
                    stringResource(R.string.weekly_col_time),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                existing.forEach { e ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier
                                .size(8.dp)
                                .background(colorFromHex(e.colorHex), RoundedCornerShape(4.dp))
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(e.title, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                        IconButton(onClick = { confirmDelete = e.id }) {
                            Icon(Icons.Outlined.Delete, stringResource(R.string.delete), tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(
                            baseEntry.copy(title = title.trim(), note = note.trim(), colorHex = color)
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = title.isNotBlank()
            ) { Text(stringResource(R.string.save)) }
            initial?.let {
                Spacer(Modifier.height(6.dp))
                OutlinedButton(
                    onClick = { confirmDelete = it.id },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                }
            }
            Spacer(Modifier.height(28.dp))
        }
    }

    confirmDelete?.let { id ->
        ConfirmDeleteDialog(
            itemName = title.ifBlank { "…" },
            onConfirm = { onDelete(id); confirmDelete = null },
            onDismiss = { confirmDelete = null }
        )
    }
}
