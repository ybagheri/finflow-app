package com.finflow.app.data.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.finflow.app.data.prefs.UserPreferences
import com.finflow.app.domain.model.RecurrenceInterval
import com.finflow.app.domain.model.RecurringRule
import com.finflow.app.domain.model.Transaction
import com.finflow.app.domain.repository.RecurringRepository
import com.finflow.app.domain.repository.TransactionRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.time.LocalDate
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.first

/**
 * Phase 4 recurring-transaction materialization.
 *
 * Pure date math ([occurrencesBetween]) is JVM-tested; [materializeDueNow]
 * is shared by the daily [RecurringWorker] and the UI "Run now" button.
 * No schema changes: per-rule bookkeeping is unnecessary because a single
 * global last-run day + deterministic stepping makes runs idempotent —
 * re-running for the same day range regenerates the same due dates, so the
 * worker advances the watermark only after inserting.
 */
object RecurringScheduler {
    const val PERIODIC_WORK_NAME = "finflow-recurring-daily"

    /**
     * Due dates of [rule] in (fromExclusive, toInclusive], honouring
     * start/end bounds. Iteration is capped for safety.
     */
    fun occurrencesBetween(
        rule: RecurringRule,
        fromExclusive: LocalDate,
        toInclusive: LocalDate
    ): List<LocalDate> {
        if (toInclusive.isBefore(ruleStart(rule))) return emptyList()
        val end = rule.endEpochDay?.let { LocalDate.ofEpochDay(it) }?.let {
            if (it.isBefore(toInclusive)) it else toInclusive
        } ?: toInclusive
        val due = mutableListOf<LocalDate>()
        var cursor = ruleStart(rule)
        var steps = 0
        while (!cursor.isAfter(end) && steps < 366 * 5) {
            if (cursor.isAfter(fromExclusive)) due.add(cursor)
            cursor = when (rule.interval) {
                RecurrenceInterval.DAILY -> cursor.plusDays(1)
                RecurrenceInterval.WEEKLY -> cursor.plusWeeks(1)
                RecurrenceInterval.MONTHLY -> cursor.plusMonths(1)
            }
            steps += 1
        }
        return due
    }

    private fun ruleStart(rule: RecurringRule): LocalDate =
        LocalDate.ofEpochDay(rule.startEpochDay)

    /** Next due date after [today], or null when the rule has ended. */
    fun nextDueAfter(rule: RecurringRule, today: LocalDate = LocalDate.now()): LocalDate? {
        var cursor = ruleStart(rule)
        var steps = 0
        while (steps < 366 * 5) {
            if (cursor.isAfter(today)) {
                val end = rule.endEpochDay?.let { LocalDate.ofEpochDay(it) }
                if (end != null && cursor.isAfter(end)) return null
                return cursor
            }
            cursor = when (rule.interval) {
                RecurrenceInterval.DAILY -> cursor.plusDays(1)
                RecurrenceInterval.WEEKLY -> cursor.plusWeeks(1)
                RecurrenceInterval.MONTHLY -> cursor.plusMonths(1)
            }
            steps += 1
        }
        return null
    }

    /**
     * Inserts one transaction per due rule-date since the last run.
     * Returns the number of transactions created.
     */
    suspend fun materializeDueNow(
        transactions: TransactionRepository,
        recurring: RecurringRepository,
        prefs: UserPreferences,
        today: LocalDate = LocalDate.now()
    ): Int {
        val rules = recurring.observeActive().first().filter { it.isActive }
        if (rules.isEmpty()) {
            prefs.setLastRecurringRun(today.toEpochDay())
            return 0
        }
        val lastRun = prefs.lastRecurringRun.first()?.let { LocalDate.ofEpochDay(it) }
            ?: today.minusDays(1)
        if (!today.isAfter(lastRun)) return 0
        var created = 0
        rules.forEach { rule ->
            occurrencesBetween(rule, lastRun, today).forEach { date ->
                transactions.upsert(
                    Transaction(
                        amount = rule.amount,
                        type = rule.type,
                        categoryId = rule.categoryId,
                        dateEpochDay = date.toEpochDay(),
                        note = rule.note,
                        paymentMethod = rule.paymentMethod,
                        currencyCode = rule.currencyCode
                    )
                )
                created += 1
            }
        }
        prefs.setLastRecurringRun(today.toEpochDay())
        return created
    }

    /** Daily periodic work (kept across app starts). */
    fun schedule(context: Context) {
        val request = PeriodicWorkRequestBuilder<RecurringWorker>(1, TimeUnit.DAYS)
            .addTag(PERIODIC_WORK_NAME)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            PERIODIC_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    /** One-shot run, e.g. from the Recurring screen "Run now" button. */
    fun runOnce(context: Context) {
        val request = OneTimeWorkRequestBuilder<RecurringWorker>()
            .addTag(PERIODIC_WORK_NAME)
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            "$PERIODIC_WORK_NAME-once",
            ExistingWorkPolicy.REPLACE,
            request
        )
    }
}

/** Hilt entry point so the plain [Worker] can reach repositories. */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface RecurringEntryPoint {
    fun transactions(): TransactionRepository
    fun recurring(): RecurringRepository
    fun prefs(): UserPreferences
}

/** Daily worker; failures retry with backoff, success advances the watermark. */
class RecurringWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val entry = EntryPointAccessors.fromApplication(
            applicationContext,
            RecurringEntryPoint::class.java
        )
        return runCatching {
            RecurringScheduler.materializeDueNow(
                entry.transactions(),
                entry.recurring(),
                entry.prefs()
            )
        }.fold(
            onSuccess = { Result.success() },
            onFailure = { Result.retry() }
        )
    }
}
