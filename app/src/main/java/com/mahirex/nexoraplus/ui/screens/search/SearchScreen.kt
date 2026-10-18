package com.mahirex.nexoraplus.ui.screens.search

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.mahirex.nexoraplus.R
import com.mahirex.nexoraplus.di.AppContainer
import com.mahirex.nexoraplus.ui.AppViewModelFactory
import com.mahirex.nexoraplus.ui.components.EmptyState
import com.mahirex.nexoraplus.ui.components.LocalAppFormat
import com.mahirex.nexoraplus.ui.components.NexoraTopBar

@Composable
fun SearchScreen(navController: NavHostController, factory: AppViewModelFactory) {
    val vm: SearchViewModel = viewModel(factory = factory)
    val results by vm.results.collectAsState()
    val query by vm.query.collectAsState()
    val fmt = LocalAppFormat.current

    Column(Modifier.fillMaxSize()) {
        NexoraTopBar(navController, stringResource(R.string.search))
        OutlinedTextField(
            value = query,
            onValueChange = { vm.query.value = it },
            placeholder = { Text(stringResource(R.string.search_hint)) },
            leadingIcon = { Icon(Icons.Filled.Search, null) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )
        Spacer(Modifier.height(10.dp))

        if (query.trim().length < 2) {
            EmptyState(Icons.Filled.Search, stringResource(R.string.search_hint))
        } else if (results.isEmpty) {
            EmptyState(Icons.Filled.Search, stringResource(R.string.search_empty))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp, 4.dp, 16.dp, 30.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                fun androidx.compose.foundation.lazy.LazyListScope.section(title: String, rows: List<Pair<String, String>>) {
                    if (rows.isEmpty()) return
                    item {
                        Text(
                            title,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    }
                    rows.forEach { (main, sub) ->
                        item {
                            Surface(
                                shape = MaterialTheme.shapes.small,
                                color = MaterialTheme.colorScheme.surface,
                                tonalElevation = 1.dp,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Column(Modifier.weight(1f)) {
                                        Text(main, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        if (sub.isNotBlank()) {
                                            Text(sub, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                section(stringResource(R.string.sec_events), results.events.map { it.title to fmt.date(it.epochDay, withWeekday = false) })
                section(stringResource(R.string.sec_tasks), results.tasks.map { it.title to (it.dueEpochDay?.let { d -> fmt.date(d, withWeekday = false) } ?: "") })
                section(stringResource(R.string.sec_goals), results.goals.map { it.title to "" })
                section(stringResource(R.string.sec_habits), results.habits.map { it.title to "" })
                section(stringResource(R.string.sec_notes), results.notes.map { it.title to it.content })
                section(stringResource(R.string.sec_lists), results.lists.map { it.title to "" })
            }
        }
    }
}
