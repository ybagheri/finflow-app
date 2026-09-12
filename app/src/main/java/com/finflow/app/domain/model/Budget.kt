package com.finflow.app.domain.model

/**
 * Monthly spending cap for one expense category.
 *
 * @param categoryId FK to [Category]
 * @param limitAmount cap in [currencyCode]
 * @param monthKey "yyyy-MM" the budget applies to (e.g. "2026-09").
 */
data class Budget(
    val id: Long = 0,
    val categoryId: Long,
    val limitAmount: Double,
    val monthKey: String,
    val currencyCode: String = "IRR"
)
