package com.finflow.app

import com.finflow.app.core.util.InsightsUtils
import com.finflow.app.domain.model.Transaction
import com.finflow.app.domain.model.TransactionType
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Phase 4 JVM tests for smart-insights math (no Android needed). */
class InsightsUtilsTest {

    private fun tx(
        amount: Double,
        type: TransactionType = TransactionType.EXPENSE,
        categoryId: Long = 1,
        date: LocalDate = LocalDate.of(2026, 9, 10)
    ) = Transaction(
        amount = amount,
        type = type,
        categoryId = categoryId,
        dateEpochDay = date.toEpochDay()
    )

    @Test
    fun `monthDeltas reports biggest mover first with percent change`() {
        val prev = listOf(
            tx(100.0, categoryId = 1, date = LocalDate.of(2026, 8, 5)),
            tx(50.0, categoryId = 2, date = LocalDate.of(2026, 8, 6))
        )
        val cur = listOf(
            tx(132.0, categoryId = 1, date = LocalDate.of(2026, 9, 5)),
            tx(10.0, categoryId = 2, date = LocalDate.of(2026, 9, 6))
        )
        val deltas = InsightsUtils.monthDeltas(cur, prev)
        assertEquals(2, deltas.size)
        assertEquals(1L, deltas[0].categoryId)
        assertEquals(32.0, deltas[0].percentChange!!, 0.001)
        assertEquals(-80.0, deltas[1].percentChange!!, 0.001)
    }

    @Test
    fun `monthDeltas ignores income and nulls percent for new categories`() {
        val deltas = InsightsUtils.monthDeltas(
            listOf(
                tx(25.0, TransactionType.INCOME, categoryId = 9),
                tx(25.0, categoryId = 3)
            ),
            emptyList()
        )
        assertEquals(1, deltas.size)
        assertEquals(3L, deltas[0].categoryId)
        assertNull(deltas[0].percentChange)
    }

    @Test
    fun `dailyAverage covers trailing window only`() {
        val end = LocalDate.of(2026, 9, 12)
        val txs = listOf(
            tx(30.0, date = LocalDate.of(2026, 9, 12)),
            tx(30.0, date = LocalDate.of(2026, 8, 13)), // 30 days back: still in window
            tx(999.0, date = LocalDate.of(2026, 8, 12)), // outside the 30d window
            tx(999.0, TransactionType.INCOME, date = LocalDate.of(2026, 9, 11))
        )
        assertEquals(2.0, InsightsUtils.dailyAverage(txs, days = 30, end = end), 0.001)
    }

    @Test
    fun `loggingStreak counts consecutive days and survives missing today`() {
        val today = LocalDate.of(2026, 9, 12)
        val alive = listOf(
            tx(1.0, date = LocalDate.of(2026, 9, 11)),
            tx(1.0, date = LocalDate.of(2026, 9, 10))
        )
        assertEquals(2, InsightsUtils.loggingStreak(alive, today))

        val broken = alive + tx(1.0, date = LocalDate.of(2026, 9, 8))
        assertEquals(2, InsightsUtils.loggingStreak(broken, today))

        assertEquals(0, InsightsUtils.loggingStreak(emptyList(), today))
        assertTrue(
            InsightsUtils.loggingStreak(
                listOf(tx(1.0, date = LocalDate.of(2026, 9, 12))),
                today
            ) == 1
        )
    }
}
