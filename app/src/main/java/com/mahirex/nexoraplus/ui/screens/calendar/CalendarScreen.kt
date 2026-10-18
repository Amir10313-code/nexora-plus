package com.mahirex.nexoraplus.ui.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.mahirex.nexoraplus.R
import com.mahirex.nexoraplus.core.calendar.CalendarConverter
import com.mahirex.nexoraplus.data.local.entity.CalEventEntity
import com.mahirex.nexoraplus.di.AppContainer
import com.mahirex.nexoraplus.ui.AppViewModelFactory
import com.mahirex.nexoraplus.ui.components.ConfirmDeleteDialog
import com.mahirex.nexoraplus.ui.components.EmptyState
import com.mahirex.nexoraplus.ui.components.LocalAppFormat
import com.mahirex.nexoraplus.ui.components.NexoraTopBar
import com.mahirex.nexoraplus.ui.components.SheetHeader
import com.mahirex.nexoraplus.ui.pickers.LuxDatePickerDialog
import com.mahirex.nexoraplus.ui.pickers.LuxTimePickerDialog
import com.mahirex.nexoraplus.ui.screens.home.OccasionRow
import com.mahirex.nexoraplus.ui.theme.colorFromHex
import java.time.LocalDate

@Composable
fun CalendarScreen(navController: NavHostController, factory: AppViewModelFactory) {
    val vm: CalendarViewModel = viewModel(factory = factory)
    val fmt = LocalAppFormat.current
    val haptic = LocalHapticFeedback.current

    val anchor by vm.anchor.collectAsState()
    val selected by vm.selected.collectAsState()
    val calType by vm.calendarType.collectAsState()
    val monthEvents by vm.eventsOfMonth.collectAsState()
    val dayEvents by vm.eventsOfSelected.collectAsState()
    val occasions by vm.occasionsOfSelected.collectAsState()
    val categories by vm.categories.collectAsState()

    var editor by remember { mutableStateOf<CalEventEntity?>(null) }
    var showCategories by remember { mutableStateOf(false) }
    var toDelete by remember { mutableStateOf<Long?>(null) }

    // month grid
    val gridDays: List<Long?> = remember(anchor, calType) {
        if (calType == 0) {
            val j = CalendarConverter.gregorianToJalali(LocalDate.ofEpochDay(anchor))
            val first = CalendarConverter.jalaliToGregorian(j.year, j.month, 1).toEpochDay()
            val len = CalendarConverter.jalaliMonthLength(j.year, j.month)
            val lead = CalendarConverter.persianWeekIndex(LocalDate.ofEpochDay(first).dayOfWeek)
            List(lead) { null } + (0 until len).map { first + it }
        } else {
            val g = LocalDate.ofEpochDay(anchor)
            val first = g.withDayOfMonth(1).toEpochDay()
            val lead = CalendarConverter.persianWeekIndex(LocalDate.ofEpochDay(first).dayOfWeek)
            List(lead) { null } + (0 until g.lengthOfMonth()).map { first + it }
        }
    }
    val monthTitle = remember(anchor, calType) {
        if (calType == 0) {
            val j = CalendarConverter.gregorianToJalali(LocalDate.ofEpochDay(anchor))
            fmt.num(j.year) + " " + CalendarConverter.jalaliMonthName(j.month, fmt.persianLocale())
        } else {
            val g = LocalDate.ofEpochDay(anchor)
            CalendarConverter.gregorianMonthName(g.monthValue, fmt.persianLocale()) + " " + fmt.num(g.year)
        }
    }

    Column(Modifier.fillMaxSize()) {
        NexoraTopBar(
            navController,
            stringResource(R.string.calendar_title),
            subtitle = monthTitle,
            trailing = {
                IconButton(onClick = { showCategories = true }) {
                    Icon(Icons.Filled.Category, stringResource(R.string.manage_categories), tint = MaterialTheme.colorScheme.primary)
                }
            }
        )

        // month nav
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { vm.moveMonth(1) }) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null)
            }
            Text(monthTitle, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            IconButton(onClick = { vm.moveMonth(-1) }) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, null)
            }
            TextButton(onClick = { vm.goToday() }) { Text(stringResource(R.string.today)) }
        }

        // weekday header
        Row(Modifier.padding(horizontal = 16.dp)) {
            listOf(R.string.day_sat_short, R.string.day_sun_short, R.string.day_mon_short, R.string.day_tue_short, R.string.day_wed_short, R.string.day_thu_short, R.string.day_fri_short)
                .forEach { res ->
                    Text(
                        stringResource(res),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                        maxLines = 1
                    )
                }
        }

        // grid
        gridDays.chunked(7).forEach { week ->
            Row(Modifier.padding(horizontal = 16.dp)) {
                week.forEach { day ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (day != null) {
                            val isSel = day == selected
                            val isToday = day == vm.today
                            val hasEvent = monthEvents.any { it.epochDay == day }
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(if (isSel) MaterialTheme.colorScheme.primary else Color.Transparent)
                                    .border(
                                        if (isToday && !isSel) 1.4.dp else 0.dp,
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                        CircleShape
                                    )
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        vm.select(day)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        // In Jalali mode the grid is built from Jalali month start/length,
                                        // so the cell number must be the Jalali day — not the Gregorian day-of-month.
                                        fmt.num(
                                            if (calType == 0) CalendarConverter.gregorianToJalali(LocalDate.ofEpochDay(day)).day
                                            else LocalDate.ofEpochDay(day).dayOfMonth
                                        ),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (hasEvent) {
                                        Box(
                                            Modifier
                                                .size(4.dp)
                                                .background(
                                                    if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.secondary,
                                                    CircleShape
                                                )
                                        )
                                    } else {
                                        Spacer(Modifier.height(4.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(4.dp))

        // selected day content
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Text(
                fmt.date(selected, withWeekday = true),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))
            if (occasions.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    occasions.forEach { occ -> OccasionRow(occ) }
                }
                Spacer(Modifier.height(12.dp))
            }
            if (dayEvents.isEmpty()) {
                Text(
                    stringResource(R.string.home_no_events),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                dayEvents.forEach { ev ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 1.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { editor = ev }
                    ) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier
                                    .size(10.dp)
                                    .background(colorFromHex(ev.colorHex), CircleShape)
                            )
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (ev.isPinned) {
                                        Icon(Icons.Filled.PushPin, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(13.dp))
                                        Spacer(Modifier.width(4.dp))
                                    }
                                    Text(
                                        ev.title,
                                        style = MaterialTheme.typography.bodyLarge,
                                        maxLines = 1, overflow = TextOverflow.Ellipsis
                                    )
                                }
                                val cat = categories.firstOrNull { it.id == ev.categoryId }
                                if (cat != null) {
                                    Text(
                                        cat.emoji + " " + cat.name,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            ev.timeMinutes?.let {
                                Text(fmt.time(it), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(110.dp))
        }
    }

    // FAB
    Box(Modifier.fillMaxSize()) {
        androidx.compose.material3.ExtendedFloatingActionButton(
            onClick = {
                editor = CalEventEntity(
                    accountId = 0, title = "", epochDay = selected,
                    colorHex = categories.firstOrNull()?.colorHex ?: "#5B3FE0",
                    categoryId = categories.firstOrNull()?.id
                )
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 100.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(Icons.Filled.Add, null)
            Spacer(Modifier.width(6.dp))
            Text(stringResource(R.string.add_event))
        }
    }

    editor?.let { ev ->
        EventEditorSheet(
            initial = ev,
            isNew = monthEvents.none { it.id == ev.id } && dayEvents.none { it.id == ev.id },
            vm = vm,
            onSave = {
                vm.save(it)
                editor = null
            },
            onDelete = { toDelete = it; editor = null },
            onDismiss = { editor = null }
        )
    }
    toDelete?.let { id ->
        ConfirmDeleteDialog(
            itemName = (monthEvents + dayEvents).firstOrNull { it.id == id }?.title ?: "…",
            onConfirm = { vm.delete(id); toDelete = null },
            onDismiss = { toDelete = null }
        )
    }
    if (showCategories) {
        CategoriesSheet(vm = vm, onDismiss = { showCategories = false })
    }
}

// ─────────────────────── Event editor ───────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventEditorSheet(
    initial: CalEventEntity,
    isNew: Boolean,
    vm: CalendarViewModel,
    onSave: (CalEventEntity) -> Unit,
    onDelete: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val fmt = LocalAppFormat.current
    val categories by vm.categories.collectAsState()
    var title by remember { mutableStateOf(initial.title) }
    var desc by remember { mutableStateOf(initial.description) }
    var epochDay by remember { mutableStateOf(initial.epochDay) }
    var time by remember { mutableStateOf(initial.timeMinutes) }
    var categoryId by remember { mutableStateOf(initial.categoryId) }
    var color by remember { mutableStateOf(initial.colorHex) }
    var pinned by remember { mutableStateOf(initial.isPinned) }
    var countdown by remember { mutableStateOf(initial.showCountdown) }
    var showDate by remember { mutableStateOf(false) }
    var showTime by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            SheetHeader(stringResource(if (isNew) R.string.add_event else R.string.edit), onDismiss)
            OutlinedTextField(
                value = title, onValueChange = { title = it },
                label = { Text(stringResource(R.string.event_hint)) },
                singleLine = true, modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = desc, onValueChange = { desc = it },
                label = { Text(stringResource(R.string.event_desc)) },
                modifier = Modifier.fillMaxWidth(), minLines = 2
            )
            Spacer(Modifier.height(10.dp))

            // category chips
            Text(stringResource(R.string.category), style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(6.dp))
            Row(
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    FilterChip(
                        selected = categoryId == cat.id,
                        onClick = {
                            categoryId = cat.id
                            color = cat.colorHex
                        },
                        label = { Text(cat.emoji + " " + cat.name) }
                    )
                }
            }
            Spacer(Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showDate = true }
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text(stringResource(R.string.select_date), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(fmt.date(epochDay, withWeekday = false), style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showTime = true }
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text(stringResource(R.string.time), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(time?.let { fmt.time(it) } ?: stringResource(R.string.no_time), style = MaterialTheme.typography.titleSmall)
                    }
                }
            }
            Spacer(Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = pinned, onCheckedChange = { pinned = it })
                Icon(Icons.Outlined.PushPin, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.pin_event), style = MaterialTheme.typography.bodyMedium)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = countdown, onCheckedChange = { countdown = it })
                Icon(Icons.Filled.Event, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.show_countdown), style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(Modifier.height(12.dp))
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(
                            initial.copy(
                                title = title.trim(), description = desc.trim(),
                                epochDay = epochDay, timeMinutes = time,
                                categoryId = categoryId, colorHex = color,
                                isPinned = pinned, showCountdown = countdown
                            )
                        )
                    }
                },
                enabled = title.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) { Text(stringResource(R.string.save)) }
            if (!isNew) {
                Spacer(Modifier.height(6.dp))
                OutlinedButton(onClick = { confirmDelete = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                }
            }
            Spacer(Modifier.height(28.dp))
        }
    }

    if (showDate) {
        LuxDatePickerDialog(
            calendarType = fmt.calendarType,
            initialEpochDay = epochDay,
            onConfirm = { epochDay = it; showDate = false },
            onDismiss = { showDate = false }
        )
    }
    if (showTime) {
        LuxTimePickerDialog(
            time24 = fmt.time24,
            initialMinutes = time ?: 8 * 60,
            onConfirm = { time = it; showTime = false },
            onDismiss = { showTime = false }
        )
    }
    if (confirmDelete) {
        ConfirmDeleteDialog(
            itemName = initial.title,
            onConfirm = { onDelete(initial.id); confirmDelete = false },
            onDismiss = { confirmDelete = false }
        )
    }
}

// ─────────────────────── Categories manager ───────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoriesSheet(vm: CalendarViewModel, onDismiss: () -> Unit) {
    val categories by vm.categories.collectAsState()
    var name by remember { mutableStateOf("") }
    var emoji by remember { mutableStateOf("📌") }
    var color by remember { mutableStateOf("#5B3FE0") }
    var toDelete by remember { mutableStateOf<Long?>(null) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(horizontal = 20.dp)) {
            SheetHeader(stringResource(R.string.manage_categories), onDismiss)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = { Text(stringResource(R.string.category_hint)) },
                    singleLine = true, modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            vm.saveCategory(
                                com.mahirex.nexoraplus.data.local.entity.EventCategoryEntity(
                                    accountId = 0, name = name.trim(), emoji = emoji, colorHex = color
                                )
                            )
                            name = ""
                        }
                    },
                    enabled = name.isNotBlank()
                ) { Text(stringResource(R.string.add)) }
            }
            Spacer(Modifier.height(8.dp))
            Row(
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("📌", "🏠", "💼", "📚", "🎂", "⚽", "🎨", "🕌", "✈️", "🎓").forEach { e ->
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                if (emoji == e) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            )
                            .clickable { emoji = e },
                        contentAlignment = Alignment.Center
                    ) { Text(e) }
                }
            }
            Spacer(Modifier.height(8.dp))
            com.mahirex.nexoraplus.ui.components.ColorSwatches(selected = color, onSelect = { color = it })
            Spacer(Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(categories, key = { it.id }) { cat ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier
                                    .size(10.dp)
                                    .background(colorFromHex(cat.colorHex), CircleShape)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(cat.emoji + "  " + cat.name, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                            IconButton(onClick = { toDelete = cat.id }) {
                                Icon(Icons.Filled.Delete, stringResource(R.string.delete), tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    toDelete?.let { id ->
        ConfirmDeleteDialog(
            itemName = categories.firstOrNull { it.id == id }?.name ?: "…",
            onConfirm = {
                categories.firstOrNull { it.id == id }?.let { vm.deleteCategory(it) }
                toDelete = null
            },
            onDismiss = { toDelete = null }
        )
    }
}
