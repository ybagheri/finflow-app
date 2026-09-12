package com.finflow.app.domain.model

/**
 * Recurrence cadence for recurring transactions (Phase 4 worker materializes instances).
 */
enum class RecurrenceInterval {
    DAILY,
    WEEKLY,
    MONTHLY
}

/**
 * Rule that spawns concrete [Transaction] rows on a schedule.
 *
 * Phase 1 only persists the rule; scheduling (WorkManager) lands in Phase 4.
 */
data class RecurringRule(
    val id: Long = 0,
    val amount: Double,
    val type: TransactionType,
    val categoryId: Long,
    val interval: RecurrenceInterval,
    val startEpochDay: Long,
    val endEpochDay: Long? = null,
    val note: String = "",
    val paymentMethod: String? = null,
    val currencyCode: String = "IRR",
    val isActive: Boolean = true
)
