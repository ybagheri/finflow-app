package com.finflow.app.core.util

import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

/**
 * Small date helpers shared by viewmodels and (later) report/export code.
 */
object DateUtils {
    fun todayEpochDay(): Long = LocalDate.now().toEpochDay()

    fun monthKey(epochDay: Long = todayEpochDay()): String =
        YearMonth.from(LocalDate.ofEpochDay(epochDay)).toString() // "yyyy-MM"

    fun formatEpochDay(epochDay: Long, pattern: String = "MMM d, yyyy"): String =
        LocalDate.ofEpochDay(epochDay).format(DateTimeFormatter.ofPattern(pattern))
}
