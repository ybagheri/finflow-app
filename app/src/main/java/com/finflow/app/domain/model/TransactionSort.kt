package com.finflow.app.domain.model

/**
 * Supported sorting for the transaction list (Phase 2 UI exposes all options).
 */
enum class TransactionSortField {
    DATE,
    AMOUNT,
    CATEGORY
}

/**
 * @param field which field to sort by
 * @param ascending true for A-Z / oldest-first / smallest-first
 */
data class TransactionSort(
    val field: TransactionSortField = TransactionSortField.DATE,
    val ascending: Boolean = false
)
