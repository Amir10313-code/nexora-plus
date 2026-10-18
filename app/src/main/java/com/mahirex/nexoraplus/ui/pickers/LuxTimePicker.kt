package com.mahirex.nexoraplus.ui.pickers

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mahirex.nexoraplus.R
import com.mahirex.nexoraplus.core.util.Bidi
import com.mahirex.nexoraplus.core.util.PersianDigits
import com.mahirex.nexoraplus.ui.components.LocalAppFormat
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Luxurious clock-dial time picker.
 * - Honors the 24-hour setting (with a real 24h double-ring dial)
 * - AM/PM selector in 12h mode
 * - Direct HH:MM typing with validation
 */
@Composable
fun LuxTimePickerDialog(
    time24: Boolean,
    initialMinutes: Int,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val fmt = LocalAppFormat.current
    val haptic = LocalHapticFeedback.current
    val primary = MaterialTheme.colorScheme.primary
    val surfaceVar = MaterialTheme.colorScheme.surfaceVariant
    val onSurface = MaterialTheme.colorScheme.onSurface
    // Hoisted for use inside the Canvas DrawScope (non-composable lambda).
    val dialCenterDot = MaterialTheme.colorScheme.surface

    var hour by rememberSaveable { mutableIntStateOf(if (initialMinutes in 0..1439) initialMinutes / 60 else 8) }
    var minute by rememberSaveable { mutableIntStateOf(if (initialMinutes in 0..1439) initialMinutes % 60 else 0) }
    var pickingMinute by rememberSaveable { mutableStateOf(false) }
    var typedHour by rememberSaveable { mutableStateOf((if (initialMinutes in 0..1439) initialMinutes / 60 else 8).toString()) }
    var typedMinute by rememberSaveable { mutableStateOf((if (initialMinutes in 0..1439) initialMinutes % 60 else 0).toString()) }

    val hourAnim by animateFloatAsState(
        targetValue = if (pickingMinute) minute.toFloat() else hour.toFloat(),
        animationSpec = tween(260), label = "dialHand"
    )

    fun currentMinutes(): Int = hour * 60 + minute

    fun digitalPart(isHour: Boolean): String {
        val v = if (isHour) hour else minute
        val s = if (isHour && !time24) {
            val h12 = if (hour % 12 == 0) 12 else hour % 12
            h12.toString()
        } else if (v < 10) "0$v" else v.toString()
        return if (fmt.persianDigits) PersianDigits.toFa(s) else s
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val h = typedHour.toIntOrNull()
                val m = typedMinute.toIntOrNull()
                if (h != null && m != null && h in 0..23 && m in 0..59) {
                    onConfirm(h * 60 + m)
                } else {
                    onConfirm(currentMinutes())
                }
            }) { Text(stringResource(R.string.ok)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
        title = { Text(stringResource(R.string.select_time), style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                // ── Digital readout ──
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (!pickingMinute) primary.copy(alpha = 0.15f) else Color.Transparent,
                        modifier = Modifier.clickable { pickingMinute = false }
                    ) {
                        Text(
                            digitalPart(true),
                            style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                            color = if (!pickingMinute) primary else onSurface,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                    Text(":", style = MaterialTheme.typography.displaySmall, color = onSurface)
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (pickingMinute) primary.copy(alpha = 0.15f) else Color.Transparent,
                        modifier = Modifier.clickable { pickingMinute = true }
                    ) {
                        Text(
                            digitalPart(false),
                            style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                            color = if (pickingMinute) primary else onSurface,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                    if (!time24) {
                        Spacer(Modifier.width(10.dp))
                        Column {
                            listOf(true to R.string.am_label, false to R.string.pm_label).forEach { (isAm, res) ->
                                val selectedAm = hour < 12
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isAm == selectedAm) primary.copy(alpha = 0.16f) else Color.Transparent,
                                    modifier = Modifier
                                        .padding(1.dp)
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            val h12 = if (hour % 12 == 0) 12 else hour % 12
                                            hour = if (isAm) (if (h12 == 12) 0 else h12) else (if (h12 == 12) 12 else h12 + 12)
                                            // Keep the typed field in sync so OK doesn't revert the AM/PM change.
                                            typedHour = hour.toString()
                                        }
                                ) {
                                    Text(
                                        stringResource(res),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (isAm == selectedAm) primary else onSurface.copy(alpha = 0.7f),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                // ── Dial ──
                Box(
                    modifier = Modifier
                        .size(250.dp)
                        .clip(CircleShape)
                        .background(surfaceVar.copy(alpha = 0.25f))
                ) {
                    Canvas(
                        Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .pointerInput(pickingMinute, time24) {
                                detectTapGestures { offset ->
                                    val center = Offset(size.width / 2f, size.height / 2f)
                                    val dx = offset.x - center.x
                                    val dy = offset.y - center.y
                                    val distance = sqrt(dx * dx + dy * dy)
                                    val radius = size.width / 2f - 26.dp.toPx()
                                    val innerR = radius * 0.62f
                                    val bearing = ((Math.toDegrees(atan2(dx.toDouble(), -dy.toDouble())) + 360.0) % 360.0)
                                    val index = ((bearing / 30.0).roundToInt()) % 12
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    if (!pickingMinute) {
                                        if (time24 && distance > innerR) {
                                            hour = if (index == 0) 0 else index + 12
                                        } else if (time24) {
                                            // Inner ring of the 24h dial: 1..12 (morning hours).
                                            hour = if (index == 0) 12 else index
                                        } else {
                                            // 12h dial: preserve the currently selected half-day.
                                            val base = if (index == 0) 12 else index
                                            hour = if (hour >= 12) (if (base == 12) 12 else base + 12) else (if (base == 12) 0 else base)
                                        }
                                        pickingMinute = true
                                        typedHour = hour.toString()
                                    } else {
                                        val m = ((bearing / 6.0).roundToInt()).toInt() % 60
                                        minute = m
                                        typedMinute = m.toString()
                                    }
                                }
                            }
                    ) {
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val radius = size.width / 2f - 26.dp.toPx()
                        val innerR = radius * 0.62f

                        // rings
                        drawCircle(surfaceVar.copy(alpha = 0.35f), radius = radius, center = center, style = Stroke(1.5f))
                        if (!pickingMinute && time24) {
                            drawCircle(surfaceVar.copy(alpha = 0.35f), radius = innerR, center = center, style = Stroke(1.2f))
                        }

                        val value = hourAnim.roundToInt()

                        if (pickingMinute) {
                            for (m in 0..59) {
                                val angle = Math.toRadians(m * 6.0 - 90.0)
                                val isMajor = m % 5 == 0
                                val r1 = radius - if (isMajor) 8.dp.toPx() else 4.dp.toPx()
                                val p1 = center + Offset(cos(angle).toFloat() * r1, sin(angle).toFloat() * r1)
                                val p2 = center + Offset(cos(angle).toFloat() * radius, sin(angle).toFloat() * radius)
                                drawLine(
                                    color = if (m == minute) primary else onSurface.copy(alpha = if (isMajor) 0.5f else 0.22f),
                                    start = p1,
                                    end = p2,
                                    strokeWidth = if (m == minute) 5f else 2.5f,
                                    cap = StrokeCap.Round
                                )
                            }
                        }

                        // hand
                        val handAngle = if (pickingMinute) value * 6.0 - 90.0
                        else {
                            val h = value % 12
                            h * 30.0 - 90.0
                        }
                        val handR = if (pickingMinute) radius * 0.92f
                        else if (time24 && hour >= 12) radius * 0.95f else radius * 0.8f
                        val handEnd = center + Offset(
                            cos(Math.toRadians(handAngle)).toFloat() * handR,
                            sin(Math.toRadians(handAngle)).toFloat() * handR
                        )
                        drawLine(primary, center, handEnd, strokeWidth = 4f, cap = StrokeCap.Round)
                        drawCircle(primary, radius = 8f, center = handEnd)
                        drawCircle(dialCenterDot, radius = 5f, center = handEnd)
                        drawCircle(primary, radius = 4f, center = center)
                    }

                    // dial numbers
                    if (!pickingMinute) {
                        val dialR = 250.dp.toPx() / 2f - 24.dp.toPx()
                        val innerR = dialR * 0.62f
                        // Outer ring: 12h values, or the PM half (13..23 + 00) in 24h mode.
                        for (i in 0 until 12) {
                            val label = if (time24) {
                                if (i == 0) "00" else (i + 12).toString()
                            } else (if (i == 0) 12 else i).toString()
                            val angle = Math.toRadians(i * 30.0 - 90.0)
                            val pos = Offset(
                                (250.dp.toPx() / 2f) + (cos(angle) * dialR).toFloat() - 12.dp.toPx(),
                                (250.dp.toPx() / 2f) + (sin(angle) * dialR).toFloat() - 12.dp.toPx()
                            )
                            Text(
                                if (fmt.persianDigits) PersianDigits.toFa(label) else label,
                                style = MaterialTheme.typography.labelMedium,
                                color = onSurface.copy(alpha = if (time24) 0.75f else 0.9f),
                                modifier = Modifier
                                    .absoluteOffset { IntOffset(pos.x.roundToInt(), pos.y.roundToInt()) }
                                    .width(24.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                        // Inner ring (only in 24h mode): morning values 1..12.
                        if (time24) {
                            for (i in 0 until 12) {
                                val label = (if (i == 0) 12 else i).toString()
                                val angle = Math.toRadians(i * 30.0 - 90.0)
                                val pos = Offset(
                                    (250.dp.toPx() / 2f) + (cos(angle) * innerR).toFloat() - 12.dp.toPx(),
                                    (250.dp.toPx() / 2f) + (sin(angle) * innerR).toFloat() - 12.dp.toPx()
                                )
                                Text(
                                    if (fmt.persianDigits) PersianDigits.toFa(label) else label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = onSurface.copy(alpha = 0.6f),
                                    modifier = Modifier
                                        .absoluteOffset { IntOffset(pos.x.roundToInt(), pos.y.roundToInt()) }
                                        .width(24.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        for (i in 0 until 12) {
                            val m = if (i == 0) 0 else i * 5
                            val label = if (m < 10) "0$m" else m.toString()
                            val angle = Math.toRadians(i * 30.0 - 90.0)
                            val r = 250.dp.toPx() / 2f - 24.dp.toPx()
                            val pos = Offset(
                                (250.dp.toPx() / 2f) + (cos(angle) * r).toFloat() - 12.dp.toPx(),
                                (250.dp.toPx() / 2f) + (sin(angle) * r).toFloat() - 12.dp.toPx()
                            )
                            Text(
                                if (fmt.persianDigits) PersianDigits.toFa(label) else label,
                                style = MaterialTheme.typography.labelMedium,
                                color = onSurface.copy(alpha = 0.85f),
                                modifier = Modifier
                                    .absoluteOffset { IntOffset(pos.x.roundToInt(), pos.y.roundToInt()) }
                                    .width(24.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // ── Direct typing ──
                Text(
                    stringResource(R.string.type_time),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = typedHour,
                        onValueChange = { v ->
                            val lat = PersianDigits.toLatin(v).filter { it.isDigit() }.take(2)
                            typedHour = lat
                            lat.toIntOrNull()?.let { if (it in 0..23) hour = it }
                        },
                        label = { Text(stringResource(R.string.hour)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        isError = (typedHour.toIntOrNull() ?: 0) !in 0..23,
                        modifier = Modifier.width(96.dp)
                    )
                    Text("  :  ", style = MaterialTheme.typography.titleLarge)
                    OutlinedTextField(
                        value = typedMinute,
                        onValueChange = { v ->
                            val lat = PersianDigits.toLatin(v).filter { it.isDigit() }.take(2)
                            typedMinute = lat
                            lat.toIntOrNull()?.let { if (it in 0..59) minute = it }
                        },
                        label = { Text(stringResource(R.string.minute)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        isError = (typedMinute.toIntOrNull() ?: 0) !in 0..59,
                        modifier = Modifier.width(96.dp)
                    )
                }
                if ((typedHour.toIntOrNull() ?: 0) !in 0..23 || (typedMinute.toIntOrNull() ?: 0) !in 0..59) {
                    Text(
                        Bidi.safe(stringResource(R.string.time_invalid)),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }
        },
        shape = RoundedCornerShape(28.dp)
    )
}


