package com.finflow.app

import com.finflow.app.data.work.RecurringScheduler
import com.finflow.app.domain.model.RecurrenceInterval
import com.finflow.app.domain.model.RecurringRule
import com.finflow.app.domain.model.TransactionType
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Phase 4 JVM tests for recurring date math (no Android needed). */
class RecurringSchedulerTest {

    private fun rule(
        interval: RecurrenceInterval,
        start: LocalDate = LocalDate.of(2026, 9, 1),
        end: LocalDate? = null
    ) = RecurringRule(
        amount = 10.0,
        type = TransactionType.EXPENSE,
        categoryId = 1,
        interval = interval,
        startEpochDay = start.toEpochDay(),
        endEpochDay = end?.toEpochDay()
    )

    @Test
    fun `daily occurrences exclude fromExclusive and include toInclusive`() {
        val due = RecurringScheduler.occurrencesBetween(
            rule(RecurrenceInterval.DAILY),
            fromExclusive = LocalDate.of(2026, 9, 1),
            toInclusive = LocalDate.of(2026, 9, 3)
        )
        assertEquals(
            listOf(LocalDate.of(2026, 9, 2), LocalDate.of(2026, 9, 3)),
            due
        )
    }

    @Test
    fun `weekly occurrences step by week`() {
        val due = RecurringScheduler.occurrencesBetween(
            rule(RecurrenceInterval.WEEKLY),
            fromExclusive = LocalDate.of(2026, 8, 31),
            toInclusive = LocalDate.of(2026, 9, 15)
        )
        assertEquals(
            listOf(
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 9, 8),
                LocalDate.of(2026, 9, 15)
            ),
            due
        )
    }

    @Test
    fun `end date caps occurrences`() {
        val due = RecurringScheduler.occurrencesBetween(
            rule(RecurrenceInterval.MONTHLY, end = LocalDate.of(2026, 9, 1)),
            fromExclusive = LocalDate.of(2026, 8, 1),
            toInclusive = LocalDate.of(2026, 12, 31)
        )
        assertEquals(listOf(LocalDate.of(2026, 9, 1)), due)
    }

    @Test
    fun `nextDueAfter skips to future and returns null when ended`() {
        val monthly = rule(RecurrenceInterval.MONTHLY)
        assertEquals(
            LocalDate.of(2026, 10, 1),
            RecurringScheduler.nextDueAfter(monthly, LocalDate.of(2026, 9, 15))
        )
        assertNull(
            RecurringScheduler.nextDueAfter(
                rule(RecurrenceInterval.DAILY, end = LocalDate.of(2026, 9, 1)),
                LocalDate.of(2026, 9, 15)
            )
        )
    }
}
