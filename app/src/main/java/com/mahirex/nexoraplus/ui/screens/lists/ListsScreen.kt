package com.mahirex.nexoraplus.ui.screens.lists

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.mahirex.nexoraplus.data.local.entity.ListItemEntity
import com.mahirex.nexoraplus.di.AppContainer
import com.mahirex.nexoraplus.ui.AppViewModelFactory
import com.mahirex.nexoraplus.ui.components.ConfirmDeleteDialog
import com.mahirex.nexoraplus.ui.components.EmptyState
import com.mahirex.nexoraplus.ui.components.LocalAppFormat
import com.mahirex.nexoraplus.ui.components.NexoraTopBar
import com.mahirex.nexoraplus.ui.components.SheetHeader
import com.mahirex.nexoraplus.ui.theme.AccountColors
import com.mahirex.nexoraplus.ui.theme.colorFromHex

private val listEmojis = listOf("📝", "🛒", "🎯", "📚", "💼", "🏠", "🎁", "✈️", "🎬", "🍳", "💪", "🎨")

@Composable
fun ListsScreen(navController: NavHostController, factory: AppViewModelFactory) {
    val vm: ListsViewModel = viewModel(factory = factory)
    val lists by vm.lists.collectAsState()
    val expandedId by vm.expanded.collectAsState()
    val items by vm.items.collectAsState()
    val fmt = LocalAppFormat.current
    val haptic = LocalHapticFeedback.current

    var editor by remember { mutableStateOf<ListItemEntity?>(null) }
    var toDelete by remember { mutableStateOf<Long?>(null) }

    Column(Modifier.fillMaxSize()) {
        NexoraTopBar(navController, stringResource(R.string.lists_title))
        Box(Modifier.weight(1f)) {
            if (lists.isEmpty()) {
                EmptyState(Icons.Filled.Folder, stringResource(R.string.lists_empty))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 120.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(lists, key = { it.id }) { list ->
                        val isOpen = expandedId == list.id
                        val listItems = if (isOpen) items else emptyList()
                        val doneCount = listItems.count { it.isDone }
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = 1.dp,
                            shadowElevation = 1.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    vm.toggleExpand(list.id)
                                }
                        ) {
                            Column(Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        Modifier
                                            .size(38.dp)
                                            .background(colorFromHex(list.colorHex).copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                                        contentAlignment = Alignment.Center
                                    ) { Text(list.emoji, style = MaterialTheme.typography.titleMedium) }
                                    Spacer(Modifier.width(10.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            list.title,
                                            style = MaterialTheme.typography.titleSmall,
                                            maxLines = 1, overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    IconButton(onClick = { editor = list }) {
                                        Icon(Icons.Filled.ExpandMore, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    IconButton(onClick = { toDelete = list.id }) {
                                        Icon(Icons.Filled.Delete, stringResource(R.string.delete), tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(17.dp))
                                    }
                                }

                                if (isOpen) {
                                    Spacer(Modifier.height(6.dp))
                                    listItems.forEach { item ->
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Checkbox(
                                                checked = item.isDone,
                                                onCheckedChange = { vm.toggleItem(item) },
                                                colors = CheckboxDefaults.colors(checkedColor = colorFromHex(list.colorHex))
                                            )
                                            Text(
                                                item.title,
                                                style = MaterialTheme.typography.bodyMedium,
                                                textDecoration = if (item.isDone) androidx.compose.ui.text.style.TextDecoration.LineThrough else null,
                                                color = if (item.isDone) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.weight(1f)
                                            )
                                            IconButton(onClick = { vm.deleteItem(item.id) }) {
                                                Icon(Icons.Filled.Close, stringResource(R.string.delete), tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(15.dp))
                                            }
                                        }
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        var newItem by remember(list.id) { mutableStateOf("") }
                                        OutlinedTextField(
                                            value = newItem,
                                            onValueChange = { newItem = it },
                                            placeholder = { Text(stringResource(R.string.item_hint), style = MaterialTheme.typography.bodySmall) },
                                            modifier = Modifier.weight(1f),
                                            singleLine = true
                                        )
                                        IconButton(
                                            onClick = {
                                                if (newItem.isNotBlank()) {
                                                    vm.addItem(list.id, newItem.trim())
                                                    newItem = ""
                                                }
                                            }
                                        ) {
                                            Icon(Icons.Filled.Send, stringResource(R.string.add), tint = colorFromHex(list.colorHex))
                                        }
                                    }
                                    if (listItems.isNotEmpty()) {
                                        Spacer(Modifier.height(6.dp))
                                        Text(
                                            stringResource(R.string.list_progress, doneCount, listItems.size),
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

            androidx.compose.material3.ExtendedFloatingActionButton(
                onClick = { editor = ListItemEntity(accountId = 0, title = "") },
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

    editor?.let { list ->
        ListEditorSheet(
            initial = list,
            isNew = lists.none { it.id == list.id },
            onSave = { vm.save(it); editor = null },
            onDismiss = { editor = null }
        )
    }
    toDelete?.let { id ->
        ConfirmDeleteDialog(
            itemName = lists.firstOrNull { it.id == id }?.title ?: "…",
            onConfirm = { vm.deleteList(id); toDelete = null },
            onDismiss = { toDelete = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ListEditorSheet(
    initial: ListItemEntity,
    isNew: Boolean,
    onSave: (ListItemEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf(initial.title) }
    var emoji by remember { mutableStateOf(initial.emoji) }
    var color by remember { mutableStateOf(initial.colorHex) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(horizontal = 20.dp)) {
            SheetHeader(stringResource(if (isNew) R.string.add else R.string.edit), onDismiss)
            OutlinedTextField(
                value = title, onValueChange = { title = it },
                label = { Text(stringResource(R.string.list_hint)) },
                singleLine = true, modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))
            Text(stringResource(R.string.avatar), style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(6.dp))
            Row(
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listEmojis.forEach { e ->
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                if (emoji == e) MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            )
                            .clickable { emoji = e },
                        contentAlignment = Alignment.Center
                    ) { Text(e) }
                }
            }
            Spacer(Modifier.height(10.dp))
            com.mahirex.nexoraplus.ui.components.ColorSwatches(selected = color, onSelect = { color = it }, colors = AccountColors)
            Spacer(Modifier.height(14.dp))
            Button(
                onClick = { if (title.isNotBlank()) onSave(initial.copy(title = title.trim(), emoji = emoji, colorHex = color)) },
                enabled = title.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) { Text(stringResource(R.string.save)) }
            Spacer(Modifier.height(28.dp))
        }
    }
}

