package com.mahirex.nexoraplus.ui.screens.more

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwitchAccount
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.mahirex.nexoraplus.R
import com.mahirex.nexoraplus.data.local.entity.AccountEntity
import com.mahirex.nexoraplus.di.AppContainer
import com.mahirex.nexoraplus.ui.AppViewModelFactory
import com.mahirex.nexoraplus.ui.components.LocalAppFormat
import com.mahirex.nexoraplus.ui.components.NexoraTopBar
import com.mahirex.nexoraplus.ui.components.PersonaAvatar
import com.mahirex.nexoraplus.ui.nav.Routes
import com.mahirex.nexoraplus.ui.screens.accounts.AccountsViewModel
import com.mahirex.nexoraplus.ui.theme.colorFromHex

@Composable
fun MoreScreen(navController: NavHostController, factory: AppViewModelFactory) {
    val accountsVm: AccountsViewModel = viewModel(factory = factory)
    val account by accountsVm.active.collectAsState()
    val haptic = LocalHapticFeedback.current

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 120.dp)
    ) {
        NexoraTopBar(navController, stringResource(R.string.more_title))

        // Active account hero
        Box(Modifier.padding(horizontal = 16.dp)) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { navController.navigate(Routes.SWITCH_ACCOUNT) }
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    PersonaAvatar(account?.avatarKey ?: "🌟", account?.colorHex ?: "#5B3FE0", 52)
                    Spacer(Modifier.size(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            account?.name ?: "",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            stringResource(R.string.account_active),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Icon(
                        Icons.Filled.SwapHoriz,
                        stringResource(R.string.switch_title),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        val items = listOf(
            Triple(Icons.Filled.Cake, stringResource(R.string.more_fortune), Color(0xFFDB2777)) to Routes.FORTUNE,
            Triple(Icons.Filled.CalendarMonth, stringResource(R.string.more_calendar), Color(0xFF059669)) to Routes.CALENDAR,
            Triple(Icons.Filled.ManageAccounts, stringResource(R.string.more_accounts), Color(0xFF5B3FE0)) to Routes.ACCOUNTS,
            Triple(Icons.Filled.Search, stringResource(R.string.more_search), Color(0xFF0891B2)) to Routes.SEARCH,
            Triple(Icons.Filled.Bolt, stringResource(R.string.more_focus), Color(0xFFD97706)) to Routes.FOCUS,
            Triple(Icons.Filled.QueryStats, stringResource(R.string.more_stats), Color(0xFF7C5CFF)) to Routes.STATS,
            Triple(Icons.Filled.CloudSync, stringResource(R.string.more_backup), Color(0xFF0D9488)) to Routes.SETTINGS,
            Triple(Icons.Filled.Tune, stringResource(R.string.more_settings), Color(0xFF475569)) to Routes.SETTINGS,
            Triple(Icons.Filled.Info, stringResource(R.string.more_about), Color(0xFF6D28D9)) to Routes.ABOUT
        )

        Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items.chunked(2).forEach { rowItems ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    rowItems.forEach { (item, route) ->
                        MoreCard(
                            modifier = Modifier.weight(1f),
                            icon = item.first,
                            label = item.second,
                            color = item.third
                        ) {
                            navController.navigate(route)
                        }
                    }
                    if (rowItems.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun MoreCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        shadowElevation = 2.dp,
        modifier = modifier
            .height(96.dp)
            .clip(RoundedCornerShape(22.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = androidx.compose.material3.ripple()
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            }
    ) {
        Column(
            Modifier.padding(14.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(
                        Brush.linearGradient(listOf(color, color.copy(alpha = 0.7f))),
                        RoundedCornerShape(13.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.height(8.dp))
            Text(label, style = MaterialTheme.typography.titleSmall, maxLines = 1)
        }
    }
}
