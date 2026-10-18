package com.mahirex.nexoraplus.ui.screens.fortune

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.mahirex.nexoraplus.R
import com.mahirex.nexoraplus.di.AppContainer
import com.mahirex.nexoraplus.ui.AppViewModelFactory
import com.mahirex.nexoraplus.ui.components.LocalAppFormat
import com.mahirex.nexoraplus.ui.components.NexoraTopBar
import com.mahirex.nexoraplus.ui.components.SheetHeader
import com.mahirex.nexoraplus.ui.theme.NexoraCyanDark
import com.mahirex.nexoraplus.ui.theme.NexoraGradientStart
import com.mahirex.nexoraplus.ui.theme.NexoraPurpleDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FortuneScreen(navController: NavHostController, factory: AppViewModelFactory) {
    val vm: FortuneViewModel = viewModel(factory = factory)
    val fmt = LocalAppFormat.current
    val fortune by vm.fortune.collectAsState()
    val history by vm.history.collectAsState()
    var showHistory by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { vm.recordToday() }

    val stateLabel = stringResource(
        when (fortune.state) {
            0 -> R.string.fortune_state_excellent
            1 -> R.string.fortune_state_good
            2 -> R.string.fortune_state_medium
            else -> R.string.fortune_state_caution
        }
    )
    val stateColor = when (fortune.state) {
        0 -> Color(0xFF059669)
        1 -> Color(0xFF0891B2)
        2 -> Color(0xFFF59E0B)
        else -> Color(0xFFDC2626)
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 40.dp)
    ) {
        NexoraTopBar(
            navController,
            stringResource(R.string.fortune_title),
            subtitle = fmt.date(vm.today, withWeekday = true),
            trailing = {
                Icon(
                    Icons.Filled.History,
                    stringResource(R.string.fortune_history),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .clickable { showHistory = true }
                )
            }
        )

        // ── Verse hero card ──
        Box(
            Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(listOf(NexoraGradientStart, NexoraPurpleDark, NexoraCyanDark)),
                    RoundedCornerShape(28.dp)
                )
                .padding(22.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text("🔮", style = MaterialTheme.typography.displaySmall)
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(R.string.fortune_of_hafez),
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = 0.85f)
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    fortune.verse,
                    style = MaterialTheme.typography.titleMedium.copy(lineHeight = MaterialTheme.typography.titleMedium.lineHeight * 1.5f),
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    stringResource(R.string.fortune_hafez_poet),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.75f)
                )
                Spacer(Modifier.height(12.dp))
                Surface(shape = RoundedCornerShape(50), color = Color.White.copy(alpha = 0.2f)) {
                    Text(
                        stateLabel,
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        Text(
            fortune.tafseer,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 28.dp)
        )

        Spacer(Modifier.height(18.dp))

        // ── Five domains ──
        val domains = listOf(
            Triple(Icons.Filled.Favorite, stringResource(R.string.fortune_domain_love), Color(0xFFDB2777)),
            Triple(Icons.Filled.School, stringResource(R.string.fortune_domain_work), Color(0xFF5B3FE0)),
            Triple(Icons.Filled.FitnessCenter, stringResource(R.string.fortune_domain_health), Color(0xFF059669)),
            Triple(Icons.Filled.Payments, stringResource(R.string.fortune_domain_money), Color(0xFFD97706)),
            Triple(Icons.Filled.Bolt, stringResource(R.string.fortune_domain_energy), Color(0xFF0891B2))
        )
        Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            domains.forEachIndexed { i, (icon, label, color) ->
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp
                ) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                        Box(
                            Modifier
                                .size(36.dp)
                                .background(color.copy(alpha = 0.14f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(label, style = MaterialTheme.typography.titleSmall, color = color)
                            Spacer(Modifier.height(2.dp))
                            Text(
                                fortune.guidance.getOrElse(i) { "" },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(18.dp))

        // ── Quote & tip ──
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.55f),
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.FormatQuote, null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.fortune_quote_title), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onTertiaryContainer)
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    fortune.quote,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Bolt, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.fortune_tip_title), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    fortune.tip,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
        Spacer(Modifier.height(30.dp))
    }

    if (showHistory) {
        ModalBottomSheet(onDismissRequest = { showHistory = false }) {
            // Scrollable so the full 30-entry history stays reachable in the sheet.
            Column(
                Modifier
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                SheetHeader(stringResource(R.string.fortune_history)) { showHistory = false }
                if (history.isEmpty()) {
                    Text(
                        stringResource(R.string.fortune_history_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                history.take(30).forEach { h ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text(
                                fmt.date(h.epochDay, withWeekday = true),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                h.verse.replace("\n", " ❖ "),
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 2
                            )
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}
