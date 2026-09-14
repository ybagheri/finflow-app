package com.finflow.app.core.util

/**
 * Gregorian <-> Jalali (Persian/Shamsi) calendar conversion.
 *
 * Algorithm attributed to Kazimierz M. Borkowski — the same base used by
 * the widely-deployed `jalaali-js` library. A Julian Day Number is used
 * as the pivot between the two systems, which keeps the math exact across
 * the full range of years this app will ever see (no drift, no lookup
 * tables to maintain).
 *
 * This is display-only: transactions are still stored as a Gregorian
 * epoch day ([java.time.LocalDate.toEpochDay]); only the on-screen text
 * changes with the user's language ([DateUtils.formatForDisplay]).
 */
internal object PersianCalendar {
    private fun div(a: Int, b: Int): Int = Math.floorDiv(a, b)
    private fun mod(a: Int, b: Int): Int = Math.floorMod(a, b)

    private fun g2d(gyIn: Int, gmIn: Int, gd: Int): Int {
        var gy = gyIn
        var gm = gmIn
        if (gm <= 2) {
            gy -= 1
            gm += 12
        }
        val a = div(gy, 100)
        val b = 2 - a + div(a, 4)
        return Math.floor(365.25 * (gy + 4716)).toInt() +
            Math.floor(30.6001 * (gm + 1)).toInt() + gd + b - 1524
    }

    // Cycle break points for the Jalali leap-year algorithm.
    private val breaks = intArrayOf(
        -61, 9, 38, 199, 426, 686, 756, 818, 1111, 1181, 1210, 1635,
        2060, 2097, 2192, 2262, 2324, 2394, 2456, 3178
    )

    private class JalCal(val gy: Int, val march: Int)

    private fun jalCal(jy: Int): JalCal {
        val bl = breaks.size
        val gy = jy + 621
        var leapJ = -14
        var jp = breaks[0]
        require(jy >= jp && jy < breaks[bl - 1]) { "Jalali year out of range: $jy" }
        var jump = 0
        var i = 1
        while (i < bl) {
            val jm = breaks[i]
            jump = jm - jp
            if (jy < jm) break
            leapJ += div(jump, 33) * 8 + div(mod(jump, 33), 4)
            jp = jm
            i++
        }
        var n = jy - jp
        leapJ += div(n, 33) * 8 + div(mod(n, 33) + 3, 4)
        if (mod(jump, 33) == 4 && jump - n == 4) leapJ += 1
        val leapG = div(gy, 4) - div((div(gy, 100) + 1) * 3, 4) - 150
        val march = 20 + leapJ - leapG
        return JalCal(gy, march)
    }

    private fun j2d(jy: Int, jm: Int, jd: Int): Int {
        val r = jalCal(jy)
        return g2d(r.gy, 3, r.march) + (jm - 1) * 30 + minOf(jm - 1, 6) + jd - 1
    }

    /** Gregorian calendar date -> Jalali (year, month 1-12, day). */
    fun toJalali(gy: Int, gm: Int, gd: Int): Triple<Int, Int, Int> {
        val jdn = g2d(gy, gm, gd)
        var jy = gy - 622
        var jdnStart = j2d(jy, 1, 1)
        var jdnNext = j2d(jy + 1, 1, 1)
        while (jdn >= jdnNext) {
            jy++; jdnStart = jdnNext; jdnNext = j2d(jy + 1, 1, 1)
        }
        while (jdn < jdnStart) {
            jy--; jdnNext = jdnStart; jdnStart = j2d(jy, 1, 1)
        }
        var j = jdn - jdnStart
        var jm = 0
        while (true) {
            val dm = if (jm < 6) 31 else 30
            if (j < dm) break
            j -= dm; jm++
        }
        return Triple(jy, jm + 1, j + 1)
    }

    private fun d2g(jdnIn: Int): Triple<Int, Int, Int> {
        val jdn = jdnIn
        val aa = Math.floor((jdn - 1867216.25) / 36524.25).toInt()
        val a = jdn + 1 + aa - div(aa, 4)
        val b = a + 1524
        val c = Math.floor((b - 122.1) / 365.25).toInt()
        val d = Math.floor(365.25 * c).toInt()
        val e = Math.floor((b - d) / 30.6001).toInt()
        val day = b - d - Math.floor(30.6001 * e).toInt()
        val month = if (e < 14) e - 1 else e - 13
        val year = if (month > 2) c - 4716 else c - 4715
        return Triple(year, month, day)
    }

    /** Jalali date -> Gregorian (year, month 1-12, day). */
    fun toGregorian(jy: Int, jm: Int, jd: Int): Triple<Int, Int, Int> = d2g(j2d(jy, jm, jd))

    /** Number of days in a given Jalali year+month (30/31, or 29/30 for month 12). */
    fun daysInMonth(jy: Int, jm: Int): Int {
        val startOfNext = if (jm == 12) j2d(jy + 1, 1, 1) else j2d(jy, jm + 1, 1)
        return startOfNext - j2d(jy, jm, 1)
    }

    val monthNamesFa = listOf(
        "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
    )
}
