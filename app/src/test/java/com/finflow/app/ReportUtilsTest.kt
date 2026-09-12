package com.finflow.app

import com.finflow.app.core.util.ReportUtils
import com.finflow.app.domain.model.Category
import com.finflow.app.domain.model.Transaction
import com.finflow.app.domain.model.TransactionType
import com.finflow.app.presentation.screens.reports.ReportExport
import java.time.LocalDate
import java.time.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Phase 3 JVM tests for report aggregation + CSV export (no Android needed). */
class ReportUtilsTest {

    private fun tx(
        amount: Double,
        type: TransactionType,
        categoryId: Long = 1,
        date: LocalDate = LocalDate.of(2026, 9, 10)
    ) = Transaction(
        amount = amount,
        type = type,
        categoryId = categoryId,
        dateEpochDay = date.toEpochDay()
    )

    @Test
    fun `summarize splits income and expense`() {
        val summary = ReportUtils.summarize(
            listOf(
                tx(100.0, TransactionType.INCOME),
                tx(40.0, TransactionType.EXPENSE),
                tx(10.0, TransactionType.EXPENSE)
            )
        )
        assertEquals(100.0, summary.income, 0.0)
        assertEquals(50.0, summary.expense, 0.0)
        assertEquals(50.0, summary.net, 0.0)
        assertEquals(3, summary.count)
    }

    @Test
    fun `inMonth filters by calendar month`() {
        val txs = listOf(
            tx(10.0, TransactionType.EXPENSE, date = LocalDate.of(2026, 9, 1)),
            tx(20.0, TransactionType.EXPENSE, date = LocalDate.of(2026, 8, 31))
        )
        assertEquals(1, ReportUtils.inMonth(txs, YearMonth.of(2026, 9)).size)
    }

    @Test
    fun `categoryTotals sorted largest first`() {
        val totals = ReportUtils.categoryTotals(
            listOf(
                tx(5.0, TransactionType.EXPENSE, categoryId = 1),
                tx(50.0, TransactionType.EXPENSE, categoryId = 2),
                tx(15.0, TransactionType.EXPENSE, categoryId = 1)
            ),
            TransactionType.EXPENSE
        )
        assertEquals(2, totals.size)
        assertEquals(2L, totals[0].categoryId)
        assertEquals(50.0, totals[0].total, 0.0)
    }

    @Test
    fun `monthlySeries returns trailing months oldest first`() {
        val txs = listOf(tx(30.0, TransactionType.EXPENSE, date = LocalDate.of(2026, 9, 5)))
        val series = ReportUtils.monthlySeries(txs, YearMonth.of(2026, 9), months = 3)
        assertEquals(3, series.size)
        assertEquals(YearMonth.of(2026, 7), series[0].month)
        assertEquals(YearMonth.of(2026, 9), series[2].month)
        assertEquals(30.0, series[2].expense, 0.0)
        assertEquals(0.0, series[0].expense, 0.0)
    }

    @Test
    fun `percentChange null when previous is zero`() {
        assertNull(ReportUtils.percentChange(10.0, 0.0))
        assertEquals(100.0, ReportUtils.percentChange(20.0, 10.0)!!, 0.001)
    }

    @Test
    fun `csv escapes commas and quotes`() {
        val csv = ReportExport.buildCsv(
            listOf(tx(12.5, TransactionType.EXPENSE).copy(id = 7, note = "lunch, \"big\"")),
            mapOf(1L to Category(id = 1, name = "Food", type = TransactionType.EXPENSE))
        )
        val lines = csv.trim().lines()
        assertEquals(2, lines.size)
        assertTrue(lines[0].startsWith("id,date,type,category,amount"))
        assertTrue(lines[1].contains("\"lunch, \"\"big\"\"\""))
        assertTrue(lines[1].contains("Food"))
    }
}
