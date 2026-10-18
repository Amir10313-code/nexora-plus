package com.mahirex.nexoraplus.ui.pickers

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.mahirex.nexoraplus.R
import com.mahirex.nexoraplus.core.calendar.CalendarConverter
import com.mahirex.nexoraplus.ui.components.LocalAppFormat
import java.time.LocalDate
import kotlin.math.abs

/**
 * Luxurious, calendar-aware date picker.
 * - Jalali mode shows a real Jalali grid; Gregorian mode a real Gregorian grid.
 * - Tapping the year opens a scrollable year list; tapping the month name opens a month sheet.
 */
@Composable
fun LuxDatePickerDialog(
    calendarType: Int,
    initialEpochDay: Long,
    onConfirm: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val fmt = LocalAppFormat.current
    val haptic = LocalHapticFeedback.current
    val today = CalendarConverter.todayEpochDay()

    var selected by remember { mutableStateOf(initialEpochDay) }
    var viewYear by remember { mutableIntStateOf(0) }
    var viewMonth by remember { mutableIntStateOf(0) }
    var showYearPicker by remember { mutableStateOf(false) }
    var showMonthPicker by remember { mutableStateOf(false) }

    LaunchedEffect(calendarType, initialEpochDay) {
        if (calendarType == 0) {
            val j = CalendarConverter.gregorianToJalali(LocalDate.ofEpochDay(initialEpochDay))
            viewYear = j.year; viewMonth = j.month
        } else {
            val g = LocalDate.ofEpochDay(initialEpochDay)
            viewYear = g.year; viewMonth = g.monthValue
        }
    }

    fun monthName(month: Int): String =
        if (calendarType == 0) CalendarConverter.jalaliMonthName(month, fmt.persianLocale())
        else CalendarConverter.gregorianMonthName(month, fmt.persianLocale())

    fun firstWeekIndex(year: Int, month: Int): Int =
        if (calendarType == 0) {
            CalendarConverter.persianWeekIndex(CalendarConverter.jalaliToGregorian(year, month, 1).dayOfWeek)
        } else {
            CalendarConverter.persianWeekIndex(LocalDate.of(year, month, 1).dayOfWeek)
        }

    fun daysInMonth(year: Int, month: Int): Int =
        if (calendarType == 0) CalendarConverter.jalaliMonthLength(year, month)
        else LocalDate.of(year, month, 1).lengthOfMonth()

    fun dayToEpoch(year: Int, month: Int, day: Int): Long =
        if (calendarType == 0) CalendarConverter.jalaliToGregorian(year, month, day).toEpochDay()
        else LocalDate.of(year, month, day).toEpochDay()

    fun viewOf(epochDay: Long): Pair<Int, Int> =
        if (calendarType == 0) {
            val j = CalendarConverter.gregorianToJalali(LocalDate.ofEpochDay(epochDay))
            j.year to j.month
        } else {
            val g = LocalDate.ofEpochDay(epochDay)
            g.year to g.month
        }

    fun stepMonth(delta: Int) {
        if (calendarType == 0) {
            var m = viewMonth + delta
            var y = viewYear
            if (m > 12) { m = 1; y++ }
            if (m < 1) { m = 12; y-- }
            viewMonth = m; viewYear = y
        } else {
            val base = LocalDate.of(viewYear, viewMonth, 1).plusMonths(delta.toLong())
            viewYear = base.year; viewMonth = base.monthValue
        }
    }

    if (viewMonth == 0) return

    val selView = viewOf(selected)
    val isSelVisible = selView.first == viewYear && selView.second == viewMonth
    val selectedDay: Int =
        if (isSelVisible) {
            if (calendarType == 0) {
                val j = CalendarConverter.gregorianToJalali(LocalDate.ofEpochDay(selected))
                j.day
            } else LocalDate.ofEpochDay(selected).dayOfMonth
        } else -1

    val todayView = viewOf(today)

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        confirmButton = {
            TextButton(onClick = { onConfirm(selected) }) { Text(stringResource(R.string.ok)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        },
        title = {
            Column {
                Text(stringResource(R.string.select_date), style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(4.dp))
                Text(
                    fmt.date(selected, withWeekday = true),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // ── Year & month selectors ──
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                            modifier = Modifier.clickable { showMonthPicker = true }
                        ) {
                            Text(
                                monthName(viewMonth),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                        Spacer(Modifier.size(6.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                            modifier = Modifier.clickable { showYearPicker = true }
                        ) {
                            Text(
                                fmt.num(viewYear),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                    Row {
                        IconButton(onClick = { haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove); stepMonth(1) }) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove); stepMonth(-1) }) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                // ── Weekday header ──
                Row(Modifier.fillMaxWidth()) {
                    listOf(
                        R.string.day_sat_short, R.string.day_sun_short, R.string.day_mon_short,
                        R.string.day_tue_short, R.string.day_wed_short, R.string.day_thu_short,
                        R.string.day_fri_short
                    ).forEach { res ->
                        Text(
                            stringResource(res),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))

                // ── Day grid ──
                val first = firstWeekIndex(viewYear, viewMonth)
                val days = daysInMonth(viewYear, viewMonth)
                AnimatedContent(
                    targetState = Triple(viewYear, viewMonth, 0),
                    transitionSpec = {
                        (slideInVertically(tween(220)) { it / 6 } + fadeIn(tween(220)))
                            .togetherWith(slideOutVertically(tween(220)) { -it / 6 } + fadeOut(tween(150)))
                    },
                    label = "monthGrid"
                ) { _ ->
                    Column {
                        var cell = 0
                        repeat(6) { row ->
                            Row(Modifier.fillMaxWidth()) {
                                repeat(7) { col ->
                                    val day = cell - first + 1
                                    cell++
                                    Box(
                                        modifier = Modifier.weight(1f).padding(2.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (day in 1..days) {
                                            val epoch = dayToEpoch(viewYear, viewMonth, day)
                                            val isSel = epoch == selected
                                            val isToday = epoch == today
                                            Box(
                                                modifier = Modifier
                                                    .size(38.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        when {
                                                            isSel -> MaterialTheme.colorScheme.primary
                                                            else -> Color.Transparent
                                                        }
                                                    )
                                                    .border(
                                                        width = if (isToday && !isSel) 1.5.dp else 0.dp,
                                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.55f),
                                                        shape = CircleShape
                                                    )
                                                    .clickable {
                                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                        selected = epoch
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    fmt.num(day),
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = if (isSel) MaterialTheme.colorScheme.onPrimary
                                                    else MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // ── Today chip ──
                if (todayView != viewOf(selected)) {
                    Spacer(Modifier.height(4.dp))
                    TextButton(onClick = {
                        selected = today
                        viewOf(today).let { viewYear = it.first; viewMonth = it.second }
                    }) {
                        Text(stringResource(R.string.today), color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    )

    // ── Year picker ──
    if (showYearPicker) {
        val years: List<Int> =
            if (calendarType == 0) (1300..1500).toList() else (1920..2120).toList()
        val listState = rememberLazyListState(
            initialFirstVisibleItemIndex = (years.indexOf(viewYear) - 4).coerceAtLeast(0)
        )
        AlertDialog(
            onDismissRequest = { showYearPicker = false },
            confirmButton = {},
            dismissButton = {},
            title = { Text(stringResource(R.string.picker_year)) },
            text = {
                Box(Modifier.height(320.dp)) {
                    LazyColumn(state = listState) {
                        itemsIndexed(years) { _, y ->
                            val isCurrent = y == viewYear
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (isCurrent) MaterialTheme.colorScheme.primary else Color.Transparent,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewYear = y
                                        showYearPicker = false
                                    }
                                    .padding(vertical = 3.dp, horizontal = 4.dp)
                            ) {
                                Text(
                                    fmt.num(y),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (isCurrent) MaterialTheme.colorScheme.onPrimary
                                    else MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }

    // ── Month picker ──
    if (showMonthPicker) {
        AlertDialog(
            onDismissRequest = { showMonthPicker = false },
            confirmButton = {},
            dismissButton = {},
            title = { Text(stringResource(R.string.picker_month)) },
            text = {
                Column {
                    (1..12).chunked(3).forEach { row ->
                        Row(Modifier.fillMaxWidth()) {
                            row.forEach { m ->
                                val isCurrent = m == viewMonth
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (isCurrent) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(3.dp)
                                        .clickable {
                                            viewMonth = m
                                            showMonthPicker = false
                                        }
                                ) {
                                    Text(
                                        monthName(m),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = if (isCurrent) MaterialTheme.colorScheme.onPrimary
                                        else MaterialTheme.colorScheme.onSurface,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 10.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }
}
