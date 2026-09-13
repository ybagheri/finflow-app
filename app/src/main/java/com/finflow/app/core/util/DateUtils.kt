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

    /**
     * Locale-aware display used everywhere a date is shown to the user:
     * Jalali (Shamsi) for the Persian UI language, Gregorian otherwise.
     * Storage always stays Gregorian (epoch day); only this text changes.
     */
    fun formatForDisplay(epochDay: Long, languageCode: String): String =
        if (languageCode == "fa") {
            val d = LocalDate.ofEpochDay(epochDay)
            val (jy, jm, jd) = PersianCalendar.toJalali(d.year, d.monthValue, d.dayOfMonth)
            "$jd ${PersianCalendar.monthNamesFa[jm - 1]} $jy"
        } else {
            formatEpochDay(epochDay)
        }

    /** Same as [formatForDisplay] but for a year+month only (report headers). */
    fun formatMonthForDisplay(epochDay: Long, languageCode: String): String =
        if (languageCode == "fa") {
            val d = LocalDate.ofEpochDay(epochDay)
            val (jy, jm, _) = PersianCalendar.toJalali(d.year, d.monthValue, d.dayOfMonth)
            "${PersianCalendar.monthNamesFa[jm - 1]} $jy"
        } else {
            formatEpochDay(epochDay, "MMMM yyyy")
        }
}
