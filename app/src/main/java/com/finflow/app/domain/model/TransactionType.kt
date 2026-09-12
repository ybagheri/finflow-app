package com.finflow.app.domain.model

/**
 * Income vs expense discriminator used across domain, data and UI layers.
 */
enum class TransactionType {
    INCOME,
    EXPENSE;

    companion object {
        fun fromName(name: String): TransactionType =
            entries.firstOrNull { it.name == name } ?: EXPENSE
    }
}
