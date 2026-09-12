package com.finflow.app.core.util

import com.finflow.app.domain.model.Transaction
import com.finflow.app.domain.model.TransactionType
import java.time.LocalDate
import java.time.YearMonth

/** One row of the category-breakdown table. */
data class CategoryTotal(
    val categoryId: Long,
    val total: Double,
    val count: Int
)

/** Income/expense totals for one calendar month (trend chart input). */
data class MonthPoint(
    val month: YearMonth,
    val income: Double,
    val expense: Double
)

/** Headline totals for the selected period. */
data class PeriodSummary(
    val income: Double,
    val expense: Double,
    val count: Int
) {
    val net: Double get() = income - expense
}

/**
 * Pure aggregation helpers for the Phase 3 reports screen.
 * Kept free of Android/Room types so they are unit-testable on the JVM.
 */
object ReportUtils {

    fun inMonth(transactions: List<Transaction>, month: YearMonth): List<Transaction> =
        transactions.filter { YearMonth.from(LocalDate.ofEpochDay(it.dateEpochDay)) == month }

    fun inYear(transactions: List<Transaction>, year: Int): List<Transaction> =
        transactions.filter { LocalDate.ofEpochDay(it.dateEpochDay).year == year }

    fun summarize(transactions: List<Transaction>): PeriodSummary {
        var income = 0.0
        var expense = 0.0
        transactions.forEach {
            if (it.type == TransactionType.INCOME) income += it.amount else expense += it.amount
        }
        return PeriodSummary(income, expense, transactions.size)
    }

    /** Category totals for [type] (null = both), sorted largest-first. */
    fun categoryTotals(
        transactions: List<Transaction>,
        type: TransactionType? = null
    ): List<CategoryTotal> =
        transactions
            .filter { type == null || it.type == type }
            .groupBy { it.categoryId }
            .map { (id, list) -> CategoryTotal(id, list.sumOf { it.amount }, list.size) }
            .sortedByDescending { it.total }

    /**
     * Trailing [months] months ending at [endMonth] (inclusive),
     * oldest-first, for the trend chart.
     */
    fun monthlySeries(
        transactions: List<Transaction>,
        endMonth: YearMonth,
        months: Int = 6
    ): List<MonthPoint> {
        val start = endMonth.minusMonths((months - 1).toLong())
        return (0 until months).map { offset ->
            val month = start.plusMonths(offset.toLong())
            val inScope = inMonth(transactions, month)
            MonthPoint(
                month = month,
                income = inScope.filter { it.type == TransactionType.INCOME }.sumOf { it.amount },
                expense = inScope.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
            )
        }
    }

    /**
     * Percentage change from [previous] to [current], or null when
     * [previous] is zero (avoids divide-by-zero; caller renders "new").
     */
    fun percentChange(current: Double, previous: Double): Double? =
        if (previous == 0.0) null else (current - previous) / previous * 100.0
}
