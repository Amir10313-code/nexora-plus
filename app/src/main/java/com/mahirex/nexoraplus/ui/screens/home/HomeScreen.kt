package com.mahirex.nexoraplus.ui.screens.home

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.mahirex.nexoraplus.R
import com.mahirex.nexoraplus.core.calendar.CalendarConverter
import com.mahirex.nexoraplus.data.local.entity.OccasionEntity
import com.mahirex.nexoraplus.di.AppContainer
import com.mahirex.nexoraplus.ui.AppViewModelFactory
import com.mahirex.nexoraplus.ui.components.LocalAppFormat
import com.mahirex.nexoraplus.ui.components.SectionCard
import com.mahirex.nexoraplus.ui.nav.Routes
import com.mahirex.nexoraplus.ui.theme.NexoraGradientEnd
import com.mahirex.nexoraplus.ui.theme.NexoraGradientStart
import com.mahirex.nexoraplus.ui.theme.colorFromHex
import java.util.Calendar

@Composable
fun HomeScreen(navController: NavHostController, factory: AppViewModelFactory) {
    val vm: HomeViewModel = viewModel(factory = factory)
    val fmt = LocalAppFormat.current
    val haptic = LocalHapticFeedback.current

    val account by vm.account.collectAsState()
    val fortune by vm.fortune.collectAsState()
    val occasions by vm.occasions.collectAsState()
    val events by vm.eventsToday.collectAsState()
    val tasks by vm.tasksToday.collectAsState()
    val countdown by vm.nextCountdown.collectAsState()

    val greeting = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
        in 5..11 -> stringResource(R.string.greeting_morning)
        in 12..15 -> stringResource(R.string.greeting_noon)
        in 16..18 -> stringResource(R.string.greeting_afternoon)
        else -> stringResource(R.string.greeting_evening)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 130.dp)
    ) {
        // ─────────────── Header ───────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            NexoraGradientStart,
                            NexoraGradientStart.copy(alpha = 0.85f),
                            NexoraGradientEnd
                        )
                    ),
                    RoundedCornerShape(bottomStart = 34.dp, bottomEnd = 34.dp)
                )
                .padding(22.dp)
        ) {
            // decorative circles
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .size(140.dp)
                    .background(Color.White.copy(alpha = 0.08f), CircleShape)
            )
            Box(
                Modifier
                    .align(Alignment.BottomStart)
                    .size(90.dp)
                    .background(Color.White.copy(alpha = 0.06f), CircleShape)
            )

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Brand — tapping always returns Home
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = androidx.compose.material3.ripple(bounded = true, color = Color.White.copy(alpha = 0.3f))
                            ) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                navController.navigate(Routes.HOME) {
                                    popUpTo(0) { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                            .padding(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .background(Color.White, RoundedCornerShape(13.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "N",
                                style = MaterialTheme.typography.titleLarge,
                                color = NexoraGradientStart,
                                fontWeight = FontWeight.Black
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(
                            stringResource(R.string.app_name),
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color.White.copy(alpha = 0.18f)
                    ) {
                        Row(
                            Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(account?.avatarKey ?: "🌟", style = MaterialTheme.typography.bodyMedium)
                            Spacer(Modifier.width(6.dp))
                            Text(
                                account?.name ?: "",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(end = 2.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(22.dp))
                Text(
                    "$greeting ${account?.name ?: ""} ${stringResource(R.string.home_wave)}",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White
                )
                Text(
                    stringResource(R.string.home_welcome),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.85f)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    fmt.date(vm.today, withWeekday = true),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )

                // ── فال حافظ در هدر ──
                Spacer(Modifier.height(16.dp))
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White.copy(alpha = 0.14f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navController.navigate(Routes.FORTUNE) }
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.FormatQuote, null,
                                tint = Color.White.copy(alpha = 0.9f),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                stringResource(R.string.fortune_your_fortune),
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White.copy(alpha = 0.92f)
                            )
                            Spacer(Modifier.weight(1f))
                            Text(
                                stringResource(R.string.fortune_hafez_poet),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.75f)
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            fortune.verse.replace("\n", "  ❖  "),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(18.dp))

        // ─────────────── Shortcuts ───────────────
        Column(Modifier.padding(horizontal = 16.dp)) {
            Text(
                stringResource(R.string.home_shortcuts_title),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ShortcutCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.Checklist,
                    label = stringResource(R.string.shortcut_lists),
                    gradient = listOf(Color(0xFF5B3FE0), Color(0xFF7C5CFF))
                ) { navController.navigate(Routes.LISTS) }
                ShortcutCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.CalendarMonth,
                    label = stringResource(R.string.shortcut_calendar),
                    gradient = listOf(Color(0xFF059669), Color(0xFF34D399))
                ) { navController.navigate(Routes.CALENDAR) }
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ShortcutCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.Cake,
                    label = stringResource(R.string.shortcut_fortune),
                    gradient = listOf(Color(0xFFDB2777), Color(0xFFEC4899))
                ) { navController.navigate(Routes.FORTUNE) }
                ShortcutCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.SelfImprovement,
                    label = stringResource(R.string.shortcut_focus),
                    gradient = listOf(Color(0xFF0891B2), Color(0xFF22D3EE))
                ) { navController.navigate(Routes.FOCUS) }
            }
        }

        Spacer(Modifier.height(18.dp))

        // ─────────────── Countdown ───────────────
        countdown?.let { ev ->
            Box(Modifier.padding(horizontal = 16.dp)) {
                val days = (ev.epochDay - vm.today).coerceAtLeast(0)
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navController.navigate(Routes.CALENDAR) }
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.HourglassTop, null,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                stringResource(R.string.home_countdown),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                            )
                            Text(
                                ev.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                        Text(
                            if (days <= 1) stringResource(R.string.one_day_left)
                            else stringResource(R.string.days_left, days),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.tertiary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Spacer(Modifier.height(18.dp))
        }

        // ─────────────── امروز در تقویم ───────────────
        Box(Modifier.padding(horizontal = 16.dp)) {
            SectionCard(
                title = stringResource(R.string.home_today_calendar),
                titleIcon = Icons.Filled.Event,
                accent = MaterialTheme.colorScheme.primary
            ) {
                if (occasions.isEmpty()) {
                    Text(
                        stringResource(R.string.home_occasions_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        occasions.forEach { occ -> OccasionRow(occ) }
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        stringResource(R.string.home_view_all),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { navController.navigate(Routes.CALENDAR) }
                            .padding(4.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(18.dp))

        // ─────────────── برنامه امروز ───────────────
        Box(Modifier.padding(horizontal = 16.dp)) {
            SectionCard(
                title = stringResource(R.string.home_today_plan),
                titleIcon = Icons.Filled.Bolt,
                accent = MaterialTheme.colorScheme.secondary
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (events.isEmpty() && tasks.isEmpty()) {
                        Text(
                            stringResource(R.string.home_no_events) + "\n" + stringResource(R.string.home_no_tasks),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    events.forEach { ev ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier
                                    .size(10.dp)
                                    .background(colorFromHex(ev.colorHex), CircleShape)
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                ev.title,
                                style = MaterialTheme.typography.bodyLarge,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            ev.timeMinutes?.let {
                                Spacer(Modifier.weight(1f))
                                Text(
                                    fmt.time(it),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                    tasks.forEach { t ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { vm.toggleTask(t) }
                        ) {
                            Checkbox(
                                checked = t.isDone,
                                onCheckedChange = { vm.toggleTask(t) },
                                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.tertiary)
                            )
                            Text(
                                t.title,
                                style = MaterialTheme.typography.bodyLarge,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textDecoration = if (t.isDone) androidx.compose.ui.text.style.TextDecoration.LineThrough else null,
                                color = if (t.isDone) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ShortcutCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    gradient: List<Color>,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 3.dp,
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            }
    ) {
        Column(Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(Brush.linearGradient(gradient), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = Color.White, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.height(10.dp))
            Text(label, style = MaterialTheme.typography.titleSmall)
        }
    }
}

@Composable
fun OccasionRow(occ: OccasionEntity) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(colorFromHex(occ.colorHex).copy(alpha = 0.14f), RoundedCornerShape(13.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(occ.emoji, style = MaterialTheme.typography.titleMedium)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    occ.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (occ.isHoliday) {
                    Spacer(Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
                    ) {
                        Text(
                            stringResource(R.string.occ_holiday),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            if (occ.description.isNotBlank()) {
                Text(
                    occ.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.animateContentSize()
                )
            }
        }
    }
}
