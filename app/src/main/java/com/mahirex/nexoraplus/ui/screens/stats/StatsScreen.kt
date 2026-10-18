package com.mahirex.nexoraplus.ui.screens.stats

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.mahirex.nexoraplus.R
import com.mahirex.nexoraplus.di.AppContainer
import com.mahirex.nexoraplus.ui.AppViewModelFactory
import com.mahirex.nexoraplus.ui.components.LocalAppFormat
import com.mahirex.nexoraplus.ui.components.NexoraTopBar
import com.mahirex.nexoraplus.ui.components.SectionCard

@Composable
fun StatsScreen(navController: NavHostController, factory: AppViewModelFactory) {
    val vm: StatsViewModel = viewModel(factory = factory)
    val stats by vm.stats.collectAsState()
    val fmt = LocalAppFormat.current

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        NexoraTopBar(navController, stringResource(R.string.stats_title))

        Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    Modifier.weight(1f),
                    icon = Icons.Filled.TaskAlt,
                    value = if (stats.tasksTotal > 0) fmt.num(stats.tasksDone * 100 / stats.tasksTotal) + "٪" else "—",
                    label = stringResource(R.string.stats_done_pct),
                    color = Color(0xFF059669)
                )
                StatCard(
                    Modifier.weight(1f),
                    icon = Icons.Filled.Event,
                    value = fmt.num(stats.eventsTotal),
                    label = stringResource(R.string.stats_events_total),
                    color = Color(0xFF5B3FE0)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    Modifier.weight(1f),
                    icon = Icons.Filled.Bolt,
                    value = fmt.num(stats.focusMinutes),
                    label = stringResource(R.string.stats_focus_total),
                    color = Color(0xFF0891B2)
                )
                StatCard(
                    Modifier.weight(1f),
                    icon = Icons.Filled.Folder,
                    value = fmt.num(stats.listsTotal),
                    label = stringResource(R.string.stats_lists_total),
                    color = Color(0xFFDB2777)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    Modifier.weight(1f),
                    icon = Icons.Filled.Flag,
                    value = fmt.num(stats.goalsTotal),
                    label = stringResource(R.string.stats_goals_active),
                    color = Color(0xFFD97706)
                )
                StatCard(
                    Modifier.weight(1f),
                    icon = Icons.Filled.Repeat,
                    value = if (stats.bestStreak > 0) "🔥" + fmt.num(stats.bestStreak) else "—",
                    label = stats.bestHabit.ifBlank { stringResource(R.string.stats_habit_best) },
                    color = Color(0xFF7C5CFF)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Week bars ──
        Box(Modifier.padding(horizontal = 16.dp)) {
            SectionCard(title = stringResource(R.string.stats_week_progress)) {
                val maxVal = (stats.weekTasks.maxOrNull() ?: 1).coerceAtLeast(1)
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    stats.weekTasks.forEachIndexed { i, v ->
                        val isToday = i == stats.weekTasks.lastIndex
                        Column(
                            Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                fmt.num(v),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(4.dp))
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .height((v.toFloat() / maxVal * 90f).dp.coerceAtLeast(6.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(
                                                if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary.copy(alpha = 0.75f),
                                                if (isToday) MaterialTheme.colorScheme.primary.copy(alpha = 0.55f) else MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f)
                                            )
                                        ),
                                        RoundedCornerShape(8.dp)
                                    )
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                stringResource(
                                    listOf(
                                        R.string.day_sat_short, R.string.day_sun_short, R.string.day_mon_short,
                                        R.string.day_tue_short, R.string.day_wed_short, R.string.day_thu_short,
                                        R.string.day_fri_short
                                    )[(com.mahirex.nexoraplus.core.calendar.CalendarConverter.persianWeekIndex(vm.today - (stats.weekTasks.lastIndex - i)))]
                                ),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(30.dp))
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    value: String,
    label: String,
    color: Color
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = modifier
    ) {
        Column(Modifier.padding(14.dp)) {
            Box(
                Modifier
                    .size(34.dp)
                    .background(color.copy(alpha = 0.14f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(17.dp))
            }
            Spacer(Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.titleLarge)
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
