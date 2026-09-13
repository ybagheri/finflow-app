package com.finflow.app.core.util

import com.finflow.app.domain.model.Transaction
import com.finflow.app.domain.model.TransactionType
import java.time.LocalDate

/** Biggest mover between two monthly expense lists. */
data class CategoryDelta(
    val categoryId: Long,
    val current: Double,
    val previous: Double,
    /** Null when the category is new this month (previous == 0). */
    val percentChange: Double?
)

/**
 * Pure smart-insights math (Phase 4). Android-free so it runs on JVM tests.
 */
object InsightsUtils {

    /**
     * Expense deltas per category between [current] and [previous] month lists,
     * ordered by current-month spend descending.
     */
    fun monthDeltas(
        current: List<Transaction>,
        previous: List<Transaction>
    ): List<CategoryDelta> {
        val cur = current.filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.categoryId }.mapValues { (_, v) -> v.sumOf { it.amount } }
        val prev = previous.filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.categoryId }.mapValues { (_, v) -> v.sumOf { it.amount } }
        return cur.map { (id, amount) ->
            val before = prev[id] ?: 0.0
            CategoryDelta(id, amount, before, ReportUtils.percentChange(amount, before))
        }.sortedByDescending { it.current }
    }

    /** Mean daily expense over the [days] ending at [end] (inclusive). */
    fun dailyAverage(
        transactions: List<Transaction>,
        days: Int = 30,
        end: LocalDate = LocalDate.now()
    ): Double {
        if (days <= 0) return 0.0
        val start = end.minusDays(days.toLong())
        val total = transactions
            .filter { it.type == TransactionType.EXPENSE }
            .filter {
                val d = LocalDate.ofEpochDay(it.dateEpochDay)
                !d.isBefore(start) && !d.isAfter(end)
            }.sumOf { it.amount }
        return total / days
    }

    /**
     * Consecutive days with at least one transaction, counting back from
     * [today] (a missing today still counts if yesterday has activity —
     * the streak is "alive" until a full day passes with nothing logged).
     */
    fun loggingStreak(
        transactions: List<Transaction>,
        today: LocalDate = LocalDate.now()
    ): Int {
        if (transactions.isEmpty()) return 0
        val activeDays = transactions.map { LocalDate.ofEpochDay(it.dateEpochDay) }.toSet()
        var cursor = if (today in activeDays) today else today.minusDays(1)
        if (cursor !in activeDays) return 0
        var streak = 0
        while (cursor in activeDays) {
            streak += 1
            cursor = cursor.minusDays(1)
        }
        return streak
    }
}
