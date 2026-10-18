package com.mahirex.nexoraplus.ui.nav

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mahirex.nexoraplus.R
import com.mahirex.nexoraplus.data.repo.AppSettings
import com.mahirex.nexoraplus.di.AppContainer
import com.mahirex.nexoraplus.ui.AppViewModelFactory
import com.mahirex.nexoraplus.ui.components.LocalAppFormat
import com.mahirex.nexoraplus.ui.components.AppFormat
import com.mahirex.nexoraplus.ui.screens.about.AboutScreen
import com.mahirex.nexoraplus.ui.screens.accounts.AccountsScreen
import com.mahirex.nexoraplus.ui.screens.accounts.PersonalizeScreen
import com.mahirex.nexoraplus.ui.screens.accounts.SwitchAccountScreen
import com.mahirex.nexoraplus.ui.screens.calendar.CalendarScreen
import com.mahirex.nexoraplus.ui.screens.fortune.FortuneScreen
import com.mahirex.nexoraplus.ui.screens.focus.FocusScreen
import com.mahirex.nexoraplus.ui.screens.home.HomeScreen
import com.mahirex.nexoraplus.ui.screens.lists.ListsScreen
import com.mahirex.nexoraplus.ui.screens.more.MoreScreen
import com.mahirex.nexoraplus.ui.screens.search.SearchScreen
import com.mahirex.nexoraplus.ui.screens.settings.SettingsScreen
import com.mahirex.nexoraplus.ui.screens.stats.StatsScreen
import com.mahirex.nexoraplus.ui.screens.tasks.TasksScreen
import com.mahirex.nexoraplus.ui.screens.weekly.WeeklyScreen

object Routes {
    const val HOME = "home"
    const val WEEKLY = "weekly"
    const val TASKS = "tasks"
    const val LISTS = "lists"
    const val MORE = "more"
    const val FORTUNE = "fortune"
    const val CALENDAR = "calendar"
    const val SETTINGS = "settings"
    const val ABOUT = "about"
    const val ACCOUNTS = "accounts"
    const val SWITCH_ACCOUNT = "switchAccount"
    const val PERSONALIZE = "personalize/{accountId}"
    const val SEARCH = "search"
    const val STATS = "stats"
    const val FOCUS = "focus"

    fun personalize(accountId: Long) = "personalize/$accountId"
}

private val topLevel = listOf(Routes.HOME, Routes.WEEKLY, Routes.TASKS, Routes.LISTS, Routes.MORE)

private data class BottomItem(val route: String, val labelRes: Int, val icon: ImageVector, val iconActive: ImageVector)

@Composable
fun NexoraApp(settings: AppSettings, container: AppContainer) {
    val navController = rememberNavController()
    val factory = remember { AppViewModelFactory(container) }
    // remember(...) so a back-stack change doesn't rebuild AppFormat and invalidate
    // every LocalAppFormat reader in the tree on each recomposition.
    val fmt = remember(settings.calendarType, settings.time24, settings.languageTag) {
        AppFormat(
            calendarType = settings.calendarType,
            time24 = settings.time24,
            persianDigits = AppFormat.isFa(settings.languageTag),
            languageTag = settings.languageTag
        )
    }

    androidx.compose.runtime.CompositionLocalProvider(LocalAppFormat provides fmt) {
        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = backStackEntry?.destination?.route
        val showBottom = currentRoute in topLevel

        Surface(color = MaterialTheme.colorScheme.background) {
            Box(Modifier.fillMaxSize()) {
                NavHost(
                    navController = navController,
                    startDestination = Routes.HOME,
                    modifier = Modifier.fillMaxSize()
                ) {
                    composable(Routes.HOME) { HomeScreen(navController, factory) }
                    composable(Routes.WEEKLY) { WeeklyScreen(navController, factory) }
                    composable(Routes.TASKS) { TasksScreen(navController, factory) }
                    composable(Routes.LISTS) { ListsScreen(navController, factory) }
                    composable(Routes.MORE) { MoreScreen(navController, factory) }
                    composable(Routes.FORTUNE) { FortuneScreen(navController, factory) }
                    composable(Routes.CALENDAR) { CalendarScreen(navController, factory) }
                    composable(Routes.SETTINGS) { SettingsScreen(navController, factory) }
                    composable(Routes.ABOUT) { AboutScreen(navController, factory) }
                    composable(Routes.ACCOUNTS) { AccountsScreen(navController, factory) }
                    composable(Routes.SWITCH_ACCOUNT) { SwitchAccountScreen(navController, factory) }
                    composable(Routes.PERSONALIZE) { entry ->
                        val id = entry.arguments?.getString("accountId")?.toLongOrNull() ?: 0L
                        PersonalizeScreen(navController, factory, id)
                    }
                    composable(Routes.SEARCH) { SearchScreen(navController, factory) }
                    composable(Routes.STATS) { StatsScreen(navController, factory) }
                    composable(Routes.FOCUS) { FocusScreen(navController, factory) }
                }

                AnimatedVisibility(
                    visible = showBottom,
                    modifier = Modifier.align(Alignment.BottomCenter),
                    enter = slideInVertically(tween(250)) { it },
                    exit = slideOutVertically(tween(250)) { it }
                ) {
                    NexoraBottomBar(navController, currentRoute)
                }
            }
        }
    }
}

@Composable
private fun NexoraBottomBar(navController: NavHostController, currentRoute: String?) {
    val haptic = LocalHapticFeedback.current
    val items = listOf(
        BottomItem(Routes.HOME, R.string.nav_home, Icons.Outlined.Home, Icons.Filled.Home),
        BottomItem(Routes.WEEKLY, R.string.nav_weekly, Icons.Outlined.CalendarMonth, Icons.Filled.CalendarMonth),
        BottomItem(Routes.TASKS, R.string.nav_tasks, Icons.Outlined.TaskAlt, Icons.Filled.TaskAlt),
        BottomItem(Routes.LISTS, R.string.nav_lists, Icons.Outlined.Checklist, Icons.Filled.Checklist),
        BottomItem(Routes.MORE, R.string.nav_more, Icons.Outlined.MoreHoriz, Icons.Filled.MoreHoriz)
    )
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 12.dp),
        shape = RoundedCornerShape(26.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        shadowElevation = 10.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(WindowInsets.navigationBars.asPaddingValues())
                .padding(horizontal = 6.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val selected = currentRoute == item.route
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(18.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = androidx.compose.material3.ripple()
                        ) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            navController.navigate(item.route) {
                                popUpTo(Routes.HOME) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                        .padding(vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                if (selected) Brush.linearGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.secondary
                                    )
                                )
                                else androidx.compose.ui.graphics.Color.Transparent,
                                RoundedCornerShape(14.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (selected) item.iconActive else item.icon,
                            contentDescription = stringResource(item.labelRes),
                            tint = if (selected) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(21.dp)
                        )
                    }
                    Text(
                        text = stringResource(item.labelRes),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (selected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
