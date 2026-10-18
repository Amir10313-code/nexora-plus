package com.mahirex.nexoraplus.ui.screens.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddTask
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.mahirex.nexoraplus.R
import com.mahirex.nexoraplus.core.calendar.CalendarConverter
import com.mahirex.nexoraplus.data.local.entity.GoalEntity
import com.mahirex.nexoraplus.data.local.entity.HabitEntity
import com.mahirex.nexoraplus.data.local.entity.TaskEntity
import com.mahirex.nexoraplus.di.AppContainer
import com.mahirex.nexoraplus.ui.AppViewModelFactory
import com.mahirex.nexoraplus.ui.components.ConfirmDeleteDialog
import com.mahirex.nexoraplus.ui.components.EmptyState
import com.mahirex.nexoraplus.ui.components.LocalAppFormat
import com.mahirex.nexoraplus.ui.components.NexoraTopBar
import com.mahirex.nexoraplus.ui.components.PinBadge
import com.mahirex.nexoraplus.ui.components.SheetHeader
import com.mahirex.nexoraplus.ui.pickers.LuxDatePickerDialog
import com.mahirex.nexoraplus.ui.theme.AccountColors
import com.mahirex.nexoraplus.ui.theme.colorFromHex
import com.mahirex.nexoraplus.ui.theme.colorToHex

private val priorityColors = listOf(Color(0xFF94A3B8), Color(0xFFF59E0B), Color(0xFFDC2626))

@Composable
fun TasksScreen(navController: NavHostController, factory: AppViewModelFactory) {
    val vm: TasksViewModel = viewModel(factory = factory)
    var tab by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        stringResource(R.string.tab_tasks),
        stringResource(R.string.tab_goals),
        stringResource(R.string.tab_habits)
    )

    Column(Modifier.fillMaxSize()) {
        NexoraTopBar(navController, stringResource(R.string.nav_tasks))
        TabRow(
            selectedTabIndex = tab,
            containerColor = Color.Transparent,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            tabs.forEachIndexed { i, label ->
                Tab(
                    selected = tab == i,
                    onClick = { tab = i },
                    text = { Text(label, style = MaterialTheme.typography.titleSmall) }
                )
            }
        }
        Box(Modifier.weight(1f)) {
            when (tab) {
                0 -> TasksTab(vm, factory)
                1 -> GoalsTab(vm)
                else -> HabitsTab(vm)
            }
        }
    }
}

// ─────────────────────────── Tasks ───────────────────────────
@Composable
private fun TasksTab(vm: TasksViewModel, factory: AppViewModelFactory) {
    val tasks by vm.tasks.collectAsState()
    val filter by vm.currentFilter.collectAsState()
    val fmt = LocalAppFormat.current
    val haptic = LocalHapticFeedback.current
    var editor by remember { mutableStateOf<TaskEntity?>(null) }
    var toDelete by remember { mutableStateOf<Long?>(null) }

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                TaskFilter.ALL to stringResource(R.string.filter_all),
                TaskFilter.OPEN to stringResource(R.string.filter_open),
                TaskFilter.DONE to stringResource(R.string.filter_done)
            ).forEach { (f, label) ->
                FilterChip(
                    selected = filter == f,
                    onClick = { vm.setFilter(f) },
                    label = { Text(label) }
                )
            }
        }

        Box(Modifier.weight(1f)) {
            if (tasks.isEmpty()) {
            EmptyState(Icons.Filled.AddTask, stringResource(R.string.tasks_empty), stringResource(R.string.empty_message))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(start = 16.dp, end = 16.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(tasks, key = { it.id }) { t ->
                    val overdue = !t.isDone && t.dueEpochDay != null && t.dueEpochDay!! < vm.today
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 1.dp,
                        shadowElevation = 1.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                editor = t
                            }
                    ) {
                        Row(
                            Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                Modifier
                                    .width(4.dp)
                                    .height(40.dp)
                                    // coerceIn guards against out-of-range priorities
                                    // (e.g. from a hand-edited backup file) → IndexOutOfBounds.
                                    .background(priorityColors[t.priority.coerceIn(0, priorityColors.lastIndex)], RoundedCornerShape(2.dp))
                            )
                            Spacer(Modifier.width(8.dp))
                            Checkbox(
                                checked = t.isDone,
                                onCheckedChange = { vm.toggleTask(t) },
                                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.tertiary)
                            )
                            Column(Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (t.isPinned) { PinBadge(); Spacer(Modifier.width(5.dp)) }
                                    Text(
                                        t.title,
                                        style = MaterialTheme.typography.bodyLarge,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        textDecoration = if (t.isDone) TextDecoration.LineThrough else null,
                                        color = if (t.isDone) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    t.dueEpochDay?.let { due ->
                                        Text(
                                            fmt.date(due, withWeekday = false),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (overdue) MaterialTheme.colorScheme.error
                                            else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        if (overdue) {
                                            Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.error.copy(alpha = 0.14f)) {
                                                Text(
                                                    stringResource(R.string.overdue),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            IconButton(onClick = { vm.pinTask(t) }) {
                                Icon(
                                    if (t.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                                    stringResource(if (t.isPinned) R.string.unpin else R.string.pin),
                                    tint = if (t.isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            IconButton(onClick = { toDelete = t.id }) {
                                Icon(Icons.Filled.Delete, stringResource(R.string.delete), tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }

        androidx.compose.material3.ExtendedFloatingActionButton(
            onClick = { editor = TaskEntity(accountId = 0, title = "", colorHex = "#5B3FE0") },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 100.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(Icons.Filled.Add, null)
            Spacer(Modifier.width(6.dp))
            Text(stringResource(R.string.add))
        }
        }
    }

    editor?.let { t ->
        TaskEditorSheet(
            initial = t,
            isNew = tasks.none { it.id == t.id },
            onSave = { vm.saveTask(it); editor = null },
            onDelete = { toDelete = it; editor = null },
            onDismiss = { editor = null }
        )
    }
    toDelete?.let { id ->
        ConfirmDeleteDialog(
            itemName = tasks.firstOrNull { it.id == id }?.title ?: "…",
            onConfirm = { vm.deleteTask(id); toDelete = null },
            onDismiss = { toDelete = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskEditorSheet(
    initial: TaskEntity,
    isNew: Boolean,
    onSave: (TaskEntity) -> Unit,
    onDelete: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val fmt = LocalAppFormat.current
    var title by remember { mutableStateOf(initial.title) }
    var desc by remember { mutableStateOf(initial.description) }
    var priority by remember { mutableIntStateOf(initial.priority) }
    var due by remember { mutableStateOf(initial.dueEpochDay) }
    var showDate by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            SheetHeader(stringResource(if (isNew) R.string.add else R.string.edit), onDismiss)
            OutlinedTextField(
                value = title, onValueChange = { title = it },
                label = { Text(stringResource(R.string.task_hint)) },
                singleLine = true, modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = desc, onValueChange = { desc = it },
                label = { Text(stringResource(R.string.task_desc_hint)) },
                modifier = Modifier.fillMaxWidth(), minLines = 2
            )
            Spacer(Modifier.height(10.dp))
            Text(stringResource(R.string.priority), style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    0 to stringResource(R.string.priority_low),
                    1 to stringResource(R.string.priority_medium),
                    2 to stringResource(R.string.priority_high)
                ).forEach { (p, label) ->
                    FilterChip(
                        selected = priority == p,
                        onClick = { priority = p },
                        label = { Text(label, color = if (priority == p) priorityColors[p] else MaterialTheme.colorScheme.onSurface) }
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDate = true }
            ) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Flag, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        due?.let { fmt.date(it, withWeekday = false) } ?: stringResource(R.string.no_due),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(Modifier.weight(1f))
                    if (due != null) {
                        TextButton(onClick = { due = null }) { Text(stringResource(R.string.close)) }
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
            Button(
                onClick = {
                    if (title.isNotBlank()) onSave(initial.copy(title = title.trim(), description = desc.trim(), priority = priority, dueEpochDay = due))
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
            calendarType = LocalAppFormat.current.calendarType,
            initialEpochDay = due ?: com.mahirex.nexoraplus.core.calendar.CalendarConverter.todayEpochDay(),
            onConfirm = { due = it; showDate = false },
            onDismiss = { showDate = false }
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

// ─────────────────────────── Goals ───────────────────────────
@Composable
private fun GoalsTab(vm: TasksViewModel) {
    val goals by vm.goals.collectAsState()
    val fmt = LocalAppFormat.current
    var editor by remember { mutableStateOf<GoalEntity?>(null) }
    var toDelete by remember { mutableStateOf<Long?>(null) }
    val haptic = LocalHapticFeedback.current

    Box(Modifier.fillMaxSize()) {
        if (goals.isEmpty()) {
            EmptyState(Icons.Filled.Flag, stringResource(R.string.goals_empty), stringResource(R.string.empty_message))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp, 8.dp, 16.dp, 120.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(goals, key = { it.id }) { g ->
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 1.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                editor = g
                            }
                    ) {
                        Column(Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (g.isPinned) { PinBadge(); Spacer(Modifier.width(5.dp)) }
                                Text(
                                    g.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = { vm.pinGoal(g) }) {
                                    Icon(
                                        if (g.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                                        null,
                                        tint = if (g.isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(17.dp)
                                    )
                                }
                                IconButton(onClick = { toDelete = g.id }) {
                                    Icon(Icons.Filled.Delete, stringResource(R.string.delete), tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(17.dp))
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp))
                            ) {
                                Box(
                                    Modifier
                                        .fillMaxWidth(g.progress / 100f)
                                        .height(8.dp)
                                        .background(colorFromHex(g.colorHex), RoundedCornerShape(4.dp))
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                            Row {
                                Text(
                                    stringResource(R.string.goal_progress) + " " + fmt.num(g.progress) + "٪",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.weight(1f))
                                g.targetEpochDay?.let {
                                    Text(
                                        fmt.date(it, withWeekday = false),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        androidx.compose.material3.ExtendedFloatingActionButton(
            onClick = { editor = GoalEntity(accountId = 0, title = "", colorHex = "#059669") },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 100.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(Icons.Filled.Add, null)
            Spacer(Modifier.width(6.dp))
            Text(stringResource(R.string.add))
        }
    }

    editor?.let { g ->
        GoalEditorSheet(
            initial = g,
            isNew = goals.none { it.id == g.id },
            onSave = { vm.saveGoal(it); editor = null },
            onDelete = { toDelete = it; editor = null },
            onDismiss = { editor = null }
        )
    }
    toDelete?.let { id ->
        ConfirmDeleteDialog(
            itemName = goals.firstOrNull { it.id == id }?.title ?: "…",
            onConfirm = { vm.deleteGoal(id); toDelete = null },
            onDismiss = { toDelete = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GoalEditorSheet(
    initial: GoalEntity,
    isNew: Boolean,
    onSave: (GoalEntity) -> Unit,
    onDelete: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val fmt = LocalAppFormat.current
    var title by remember { mutableStateOf(initial.title) }
    var desc by remember { mutableStateOf(initial.description) }
    var progress by remember { mutableStateOf(initial.progress.toFloat()) }
    var target by remember { mutableStateOf(initial.targetEpochDay) }
    var color by remember { mutableStateOf(initial.colorHex) }
    var showDate by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            SheetHeader(stringResource(if (isNew) R.string.add else R.string.edit), onDismiss)
            OutlinedTextField(
                value = title, onValueChange = { title = it },
                label = { Text(stringResource(R.string.goal_hint)) },
                singleLine = true, modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = desc, onValueChange = { desc = it },
                label = { Text(stringResource(R.string.task_desc_hint)) },
                modifier = Modifier.fillMaxWidth(), minLines = 2
            )
            Spacer(Modifier.height(10.dp))
            Text(
                stringResource(R.string.goal_progress) + ": " + LocalAppFormat.current.num(progress.toInt()) + "٪",
                style = MaterialTheme.typography.labelLarge
            )
            Slider(
                value = progress,
                onValueChange = { progress = it },
                valueRange = 0f..100f,
                colors = androidx.compose.material3.SliderDefaults.colors(
                    thumbColor = colorFromHex(color),
                    activeTrackColor = colorFromHex(color)
                )
            )
            Spacer(Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDate = true }
            ) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Flag, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        target?.let { fmt.date(it, withWeekday = false) } ?: stringResource(R.string.goal_target),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(stringResource(R.string.account_color), style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(6.dp))
            com.mahirex.nexoraplus.ui.components.ColorSwatches(selected = color, onSelect = { color = it }, colors = AccountColors)
            Spacer(Modifier.height(14.dp))
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(initial.copy(title = title.trim(), description = desc.trim(), progress = progress.toInt(), targetEpochDay = target, colorHex = color))
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
            calendarType = LocalAppFormat.current.calendarType,
            initialEpochDay = target ?: com.mahirex.nexoraplus.core.calendar.CalendarConverter.todayEpochDay(),
            onConfirm = { target = it; showDate = false },
            onDismiss = { showDate = false }
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

// ─────────────────────────── Habits ───────────────────────────
@Composable
private fun HabitsTab(vm: TasksViewModel) {
    val habits by vm.habits.collectAsState()
    val logs by vm.logs.collectAsState()
    val fmt = LocalAppFormat.current
    val haptic = LocalHapticFeedback.current
    var editor by remember { mutableStateOf<HabitEntity?>(null) }
    var toDelete by remember { mutableStateOf<HabitEntity?>(null) }

    Box(Modifier.fillMaxSize()) {
        if (habits.isEmpty()) {
            EmptyState(Icons.Filled.Repeat, stringResource(R.string.habits_empty), stringResource(R.string.empty_message))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp, 8.dp, 16.dp, 120.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(habits, key = { it.id }) { h ->
                    val streak = logs.filter { it.habitId == h.id }.map { it.epochDay }.toHashSet()
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 1.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                editor = h
                            }
                    ) {
                        Column(Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    Modifier
                                        .size(34.dp)
                                        .background(colorFromHex(h.colorHex).copy(alpha = 0.16f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.Repeat, null, tint = colorFromHex(h.colorHex), modifier = Modifier.size(17.dp))
                                }
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    h.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    "🔥 " + fmt.num(vm.streakOf(h.id)),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                                IconButton(onClick = { toDelete = h }) {
                                    Icon(Icons.Filled.Delete, stringResource(R.string.delete), tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(17.dp))
                                }
                            }
                            Spacer(Modifier.height(10.dp))
                            // last 7 days dots
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                (0..6).reversed().forEach { back ->
                                    val day = vm.today - back
                                    val done = day in streak
                                    Box(
                                        modifier = Modifier
                                            .size(26.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (done) colorFromHex(h.colorHex)
                                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                            )
                                            .clickable {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                vm.toggleHabitDay(h.id, day)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            // Saturday-first Persian week index (شنبه=۱ … جمعه=۷),
                                            // consistent with every other week surface in the app.
                                            fmt.num(CalendarConverter.persianWeekIndex(day) + 1),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (done) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        androidx.compose.material3.ExtendedFloatingActionButton(
            onClick = { editor = HabitEntity(accountId = 0, title = "", colorHex = "#0891B2") },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 100.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(Icons.Filled.Add, null)
            Spacer(Modifier.width(6.dp))
            Text(stringResource(R.string.add))
        }
    }

    editor?.let { h ->
        HabitEditorSheet(
            initial = h,
            isNew = habits.none { it.id == h.id },
            onSave = { vm.saveHabit(it); editor = null },
            onDismiss = { editor = null }
        )
    }
    toDelete?.let { h ->
        ConfirmDeleteDialog(
            itemName = h.title,
            onConfirm = { vm.deleteHabit(h); toDelete = null },
            onDismiss = { toDelete = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HabitEditorSheet(
    initial: HabitEntity,
    isNew: Boolean,
    onSave: (HabitEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf(initial.title) }
    var color by remember { mutableStateOf(initial.colorHex) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(horizontal = 20.dp)) {
            SheetHeader(stringResource(if (isNew) R.string.add else R.string.edit), onDismiss)
            OutlinedTextField(
                value = title, onValueChange = { title = it },
                label = { Text(stringResource(R.string.habit_hint)) },
                singleLine = true, modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            com.mahirex.nexoraplus.ui.components.ColorSwatches(selected = color, onSelect = { color = it })
            Spacer(Modifier.height(14.dp))
            Button(
                onClick = { if (title.isNotBlank()) onSave(initial.copy(title = title.trim(), colorHex = color)) },
                enabled = title.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) { Text(stringResource(R.string.save)) }
            Spacer(Modifier.height(28.dp))
        }
    }
}
