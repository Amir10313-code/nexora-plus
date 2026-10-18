package com.mahirex.nexoraplus.ui.screens.focus

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DoNotDisturbOn
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.mahirex.nexoraplus.R
import com.mahirex.nexoraplus.di.AppContainer
import com.mahirex.nexoraplus.ui.AppViewModelFactory
import com.mahirex.nexoraplus.ui.components.LocalAppFormat
import com.mahirex.nexoraplus.ui.components.NexoraTopBar
import com.mahirex.nexoraplus.ui.components.SectionCard
import com.mahirex.nexoraplus.ui.theme.NexoraCyanDark
import com.mahirex.nexoraplus.ui.theme.NexoraGradientStart
import com.mahirex.nexoraplus.ui.theme.NexoraPurpleDark
import androidx.compose.foundation.Canvas

@Composable
fun FocusScreen(navController: NavHostController, factory: AppViewModelFactory) {
    val vm: FocusViewModel = viewModel(factory = factory)
    val fmt = LocalAppFormat.current
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    val running by vm.running.collectAsState()
    val remaining by vm.remainingSeconds.collectAsState()
    val selected by vm.selectedMinutes.collectAsState()
    val dnd by vm.dndEnabled.collectAsState()
    val sessions by vm.sessionsToday.collectAsState()
    val minutes by vm.minutesToday.collectAsState()

    LaunchedEffect(Unit) { vm.selectMinutes(selected) }

    // Re-sync the DND switch when the user returns from the system access screen,
    // so the toggle reflects reality immediately (no double toggle needed).
    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                vm.onResumed(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val total = selected * 60f
    val progress = if (running || remaining < total) 1f - remaining / total else 0f
    val animated by animateFloatAsState(progress, tween(400), label = "focusProgress")

    val mm = remaining / 60
    val ss = remaining % 60
    val timeText = String.format("%02d:%02d", mm, ss)
    val display = if (fmt.persianDigits) com.mahirex.nexoraplus.core.util.PersianDigits.toFa(timeText) else timeText

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        NexoraTopBar(navController, stringResource(R.string.focus_title))

        // ── Ring ──
        Box(
            Modifier
                .padding(24.dp)
                .fillMaxWidth()
                .height(280.dp)
                .background(
                    androidx.compose.ui.graphics.Brush.linearGradient(
                        listOf(NexoraGradientStart, NexoraPurpleDark, NexoraCyanDark)
                    ),
                    RoundedCornerShape(30.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(contentAlignment = Alignment.Center) {
                Canvas(Modifier.size(210.dp)) {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.15f),
                        radius = size.minDimension / 2f - 8f,
                        style = Stroke(width = 14f, cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = Color.White,
                        startAngle = -90f,
                        sweepAngle = 360f * animated,
                        useCenter = false,
                        style = Stroke(width = 14f, cap = StrokeCap.Round)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Filled.Timer, null,
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(26.dp)
                    )
                    Text(
                        display,
                        style = MaterialTheme.typography.displaySmall,
                        color = Color.White,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        if (running) stringResource(R.string.focus_title) else stringResource(R.string.focus_desc),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.padding(horizontal = 40.dp)
                    )
                }
            }
        }

        Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // durations
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                vm.durations.forEach { d ->
                    FilterChip(
                        selected = selected == d,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            vm.selectMinutes(d)
                        },
                        enabled = !running,
                        label = { Text(fmt.num(d)) }
                    )
                }
            }

            // DND toggle
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            ) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.DoNotDisturbOn, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.size(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(R.string.focus_dnd), style = MaterialTheme.typography.titleSmall)
                        Text(
                            stringResource(R.string.focus_dnd_hint),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = dnd,
                        onCheckedChange = {
                            if (it && !vm.isDndAccessGranted(context)) {
                                vm.requestDndAccess(context)
                            } else {
                                vm.dndEnabled.value = it
                            }
                        },
                        colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.secondary)
                    )
                }
            }

            if (running) {
                Button(
                    onClick = {
                        vm.stop(context)
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Stop, null)
                    Spacer(Modifier.size(8.dp))
                    Text(stringResource(R.string.focus_stop))
                }
            } else {
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        if (vm.dndEnabled.value && !vm.isDndAccessGranted(context)) {
                            // DND is requested but not granted — go grant it first.
                            // Starting the timer silently without DND would be surprising.
                            vm.requestDndAccess(context)
                        } else {
                            vm.start(context) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                android.widget.Toast.makeText(
                                    context,
                                    context.getString(R.string.focus_finished),
                                    android.widget.Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Bolt, null)
                    Spacer(Modifier.size(8.dp))
                    Text(stringResource(R.string.focus_start))
                }
            }

            SectionCard(title = stringResource(R.string.stats_title)) {
                Text(
                    stringResource(R.string.focus_today, sessions) + "  •  " +
                        stringResource(R.string.focus_total_min, minutes),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(30.dp))
        }
    }
}
