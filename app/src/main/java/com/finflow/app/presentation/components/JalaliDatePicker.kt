package com.finflow.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.finflow.app.core.util.PersianCalendar
import java.time.LocalDate

private val WEEKDAYS_FA = listOf("ش", "ی", "د", "س", "چ", "پ", "ج") // Saturday .. Friday

/**
 * Jalali (Shamsi) month-grid date picker, used wherever a date is edited
 * while the app language is Persian — Android's built-in Material3
 * [androidx.compose.material3.DatePicker] only understands the Gregorian
 * calendar, so this fills that gap rather than showing the "wrong" calendar
 * to a Persian-reading user. Storage stays Gregorian epoch-day either way.
 */
@Composable
fun JalaliDatePickerDialog(
    initialEpochDay: Long,
    onConfirm: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val initDate = LocalDate.ofEpochDay(initialEpochDay)
    val (initJy, initJm, initJd) = remember(initialEpochDay) {
        PersianCalendar.toJalali(initDate.year, initDate.monthValue, initDate.dayOfMonth)
    }
    var viewYear by remember { mutableIntStateOf(initJy) }
    var viewMonth by remember { mutableIntStateOf(initJm) }
    var selectedYear by remember { mutableIntStateOf(initJy) }
    var selectedMonth by remember { mutableIntStateOf(initJm) }
    var selectedDay by remember { mutableIntStateOf(initJd) }

    fun goMonth(delta: Int) {
        var y = viewYear
        var m = viewMonth + delta
        if (m < 1) { m = 12; y-- }
        if (m > 12) { m = 1; y++ }
        viewYear = y
        viewMonth = m
    }

    // Weekday (Sat=0..Fri=6) that the 1st of the viewed month falls on.
    val firstWeekday = remember(viewYear, viewMonth) {
        val (gy, gm, gd) = PersianCalendar.toGregorian(viewYear, viewMonth, 1)
        val dow = LocalDate.of(gy, gm, gd).dayOfWeek.value // MON=1..SUN=7
        (dow + 1) % 7
    }
    val dayCount = remember(viewYear, viewMonth) { PersianCalendar.daysInMonth(viewYear, viewMonth) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { goMonth(-1) }) {
                    Icon(Icons.Filled.ChevronRight, contentDescription = "قبلی")
                }
                Text(
                    "${PersianCalendar.monthNamesFa[viewMonth - 1]} $viewYear",
                    style = MaterialTheme.typography.titleMedium
                )
                IconButton(onClick = { goMonth(1) }) {
                    Icon(Icons.Filled.ChevronLeft, contentDescription = "بعدی")
                }
            }
        },
        text = {
            Column {
                Row(Modifier.fillMaxWidth()) {
                    WEEKDAYS_FA.forEach { label ->
                        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            Text(label, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
                val totalCells = firstWeekday + dayCount
                val rows = (totalCells + 6) / 7
                for (row in 0 until rows) {
                    Row(Modifier.fillMaxWidth()) {
                        for (col in 0 until 7) {
                            val day = row * 7 + col - firstWeekday + 1
                            Box(
                                modifier = Modifier.weight(1f).aspectRatio(1f).padding(2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (day in 1..dayCount) {
                                    val isSelected = day == selectedDay &&
                                        viewMonth == selectedMonth && viewYear == selectedYear
                                    val bg = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                                    val fg = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .aspectRatio(1f)
                                            .clip(CircleShape)
                                            .background(bg)
                                            .selectable(
                                                selected = isSelected,
                                                role = Role.Button,
                                                onClick = {
                                                    selectedDay = day
                                                    selectedMonth = viewMonth
                                                    selectedYear = viewYear
                                                }
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(day.toString(), color = fg, style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val (gy, gm, gd) = PersianCalendar.toGregorian(selectedYear, selectedMonth, selectedDay)
                onConfirm(LocalDate.of(gy, gm, gd).toEpochDay())
            }) { Text("تأیید") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("لغو") }
        }
    )
}
