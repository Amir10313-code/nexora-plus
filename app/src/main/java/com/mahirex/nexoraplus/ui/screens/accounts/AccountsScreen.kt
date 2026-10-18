package com.mahirex.nexoraplus.ui.screens.accounts

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.mahirex.nexoraplus.R
import com.mahirex.nexoraplus.data.local.entity.AccountEntity
import com.mahirex.nexoraplus.di.AppContainer
import com.mahirex.nexoraplus.ui.AppViewModelFactory
import com.mahirex.nexoraplus.ui.components.ConfirmDeleteDialog
import com.mahirex.nexoraplus.ui.components.NexoraTopBar
import com.mahirex.nexoraplus.ui.components.PersonaAvatar
import com.mahirex.nexoraplus.ui.nav.Routes
import com.mahirex.nexoraplus.ui.theme.AccountColors
import com.mahirex.nexoraplus.ui.theme.colorFromHex

private val avatarEmojis = listOf(
    "🌟", "🦉", "🌸", "🍀", "⚡", "🌙", "🚀", "🎯", "🐬", "🦋", "🍁", "💎", "🎓", "🏋️", "🎨", "🎧"
)

// ─────────────────────────── Accounts management ───────────────────────────
@Composable
fun AccountsScreen(navController: NavHostController, factory: AppViewModelFactory) {
    val vm: AccountsViewModel = viewModel(factory = factory)
    val accounts by vm.accounts.collectAsState()
    val activeId by vm.activeId.collectAsState()
    val context = LocalContext.current
    var toDelete by remember { mutableStateOf<AccountEntity?>(null) }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 40.dp)
    ) {
        NexoraTopBar(navController, stringResource(R.string.accounts_title))

        if (accounts.size >= 3) {
            Text(
                stringResource(R.string.account_limit),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        } else {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .clickable {
                        vm.create(
                            name = context.getString(R.string.default_account) + " " + (accounts.size + 1),
                            avatar = avatarEmojis[(accounts.size * 5 + 3) % avatarEmojis.size],
                            gender = "u",
                            color = com.mahirex.nexoraplus.ui.theme.colorToHex(AccountColors[accounts.size % AccountColors.size])
                        ) { }
                    }
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.GroupAdd, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(10.dp))
                    Text(stringResource(R.string.add_account), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        accounts.forEach { acc ->
            val isActive = acc.id == activeId
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = if (isActive) 3.dp else 1.dp,
                border = if (isActive) androidx.compose.foundation.BorderStroke(2.dp, colorFromHex(acc.colorHex)) else null,
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 5.dp)
                    .fillMaxWidth()
                    .animateContentSize()
            ) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    PersonaAvatar(acc.avatarKey, acc.colorHex, 48)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(acc.name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(
                            stringResource(
                                when (acc.gender) {
                                    "m" -> R.string.gender_male
                                    "f" -> R.string.gender_female
                                    else -> R.string.gender_unspecified
                                }
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (isActive) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.CheckCircle, null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(13.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    stringResource(R.string.account_active),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }
                    }
                    IconButton(onClick = { navController.navigate(Routes.personalize(acc.id)) }) {
                        Icon(Icons.Filled.Edit, stringResource(R.string.personalize), tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = { toDelete = acc }) {
                        Icon(Icons.Filled.Delete, stringResource(R.string.delete), tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }

    toDelete?.let { acc ->
        ConfirmDeleteDialog(
            itemName = acc.name,
            onConfirm = {
                vm.delete(acc)
                toDelete = null
            },
            onDismiss = { toDelete = null }
        )
    }
}

// ─────────────────────────── Fancy account switcher ───────────────────────────
@Composable
fun SwitchAccountScreen(navController: NavHostController, factory: AppViewModelFactory) {
    val vm: AccountsViewModel = viewModel(factory = factory)
    val accounts by vm.accounts.collectAsState()
    val activeId by vm.activeId.collectAsState()
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        NexoraTopBar(navController, stringResource(R.string.switch_title))

        Spacer(Modifier.height(8.dp))
        accounts.forEach { acc ->
            val isActive = acc.id == activeId
            val scale by animateFloatAsState(
                targetValue = if (isActive) 1f else 0.96f,
                animationSpec = spring(dampingRatio = 0.7f, stiffness = 300f),
                label = "accScale"
            )
            Surface(
                shape = RoundedCornerShape(26.dp),
                modifier = Modifier
                    .padding(horizontal = 18.dp, vertical = 8.dp)
                    .fillMaxWidth()
                    .scale(scale)
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        vm.switch(acc.id) { }
                    }
            ) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    colorFromHex(acc.colorHex),
                                    colorFromHex(acc.colorHex).copy(alpha = 0.62f)
                                )
                            ),
                            RoundedCornerShape(26.dp)
                        )
                        .padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier
                                .size(62.dp)
                                .background(Color.White.copy(alpha = 0.22f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(acc.avatarKey, style = MaterialTheme.typography.headlineSmall)
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                acc.name,
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                stringResource(
                                    when (acc.gender) {
                                        "m" -> R.string.gender_male
                                        "f" -> R.string.gender_female
                                        else -> R.string.gender_unspecified
                                    }
                                ),
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                            if (isActive) {
                                Spacer(Modifier.height(4.dp))
                                Surface(shape = RoundedCornerShape(50), color = Color.White.copy(alpha = 0.25f)) {
                                    Row(
                                        Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Filled.CheckCircle, null, tint = Color.White, modifier = Modifier.size(13.dp))
                                        Spacer(Modifier.width(5.dp))
                                        Text(
                                            stringResource(R.string.account_active),
                                            style = MaterialTheme.typography.labelMedium,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                        if (!isActive) {
                            TextButton(onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                vm.switch(acc.id) { }
                            }) {
                                Text(stringResource(R.string.switch_to), color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        if (accounts.size < 3) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = androidx.compose.foundation.BorderStroke(
                    1.5.dp,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                ),
                modifier = Modifier
                    .padding(horizontal = 18.dp, vertical = 8.dp)
                    .fillMaxWidth()
                    .clickable { navController.navigate(Routes.ACCOUNTS) }
            ) {
                Column(
                    Modifier.padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Filled.GroupAdd, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(30.dp))
                    Spacer(Modifier.height(8.dp))
                    Text(stringResource(R.string.add_account), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                    Text(
                        stringResource(R.string.account_limit),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        Spacer(Modifier.height(30.dp))
    }
}

// ─────────────────────────── Personalize ───────────────────────────
@Composable
fun PersonalizeScreen(navController: NavHostController, factory: AppViewModelFactory, accountId: Long) {
    val vm: AccountsViewModel = viewModel(factory = factory)
    val accounts by vm.accounts.collectAsState()
    val account = accounts.firstOrNull { it.id == accountId }
    val context = LocalContext.current
    var toast by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(toast) {
        toast?.let {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_SHORT).show()
            toast = null
        }
    }

    account?.let { acc ->
        var name by remember(acc.id) { mutableStateOf(acc.name) }
        var avatar by remember(acc.id) { mutableStateOf(acc.avatarKey) }
        var gender by remember(acc.id) { mutableStateOf(acc.gender) }
        var color by remember(acc.id) { mutableStateOf(acc.colorHex) }

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            NexoraTopBar(navController, stringResource(R.string.personalize))

            // hero preview
            Box(
                Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(listOf(colorFromHex(color), colorFromHex(color).copy(alpha = 0.65f))),
                        RoundedCornerShape(26.dp)
                    )
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(64.dp)
                            .background(Color.White.copy(alpha = 0.22f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) { Text(avatar, style = MaterialTheme.typography.headlineSmall) }
                    Spacer(Modifier.width(14.dp))
                    Text(name.ifBlank { "…" }, style = MaterialTheme.typography.titleLarge, color = Color.White)
                }
            }

            Spacer(Modifier.height(16.dp))
            Column(Modifier.padding(horizontal = 16.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.name_hint)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                Text(stringResource(R.string.avatar), style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(8.dp))
                Row(
                    Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    avatarEmojis.forEach { e ->
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(
                                    if (avatar == e) colorFromHex(color).copy(alpha = 0.2f)
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                )
                                .clickable { avatar = e },
                            contentAlignment = Alignment.Center
                        ) { Text(e, style = MaterialTheme.typography.titleMedium) }
                    }
                }
                Spacer(Modifier.height(14.dp))
                Text(stringResource(R.string.gender), style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        "m" to stringResource(R.string.gender_male),
                        "f" to stringResource(R.string.gender_female),
                        "u" to stringResource(R.string.gender_unspecified)
                    ).forEach { (g, label) ->
                        FilterChip(selected = gender == g, onClick = { gender = g }, label = { Text(label) })
                    }
                }
                Spacer(Modifier.height(14.dp))
                Text(stringResource(R.string.account_color), style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(8.dp))
                Row(
                    Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AccountColors.forEach { c ->
                        val hex = com.mahirex.nexoraplus.ui.theme.colorToHex(c)
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(c)
                                .border(
                                    if (color == hex) 3.dp else 0.dp,
                                    MaterialTheme.colorScheme.onSurface,
                                    CircleShape
                                )
                                .clickable { color = hex }
                        )
                    }
                }
                Spacer(Modifier.height(18.dp))
                Button(
                    onClick = {
                        vm.update(acc.copy(name = name.trim().ifBlank { acc.name }, avatarKey = avatar, gender = gender, colorHex = color)) {
                            toast = context.getString(R.string.account_saved)
                        }
                    },
                    enabled = name.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) { Text(stringResource(R.string.save)) }
                Spacer(Modifier.height(30.dp))
            }
        }
    }
}
