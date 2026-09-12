package com.finflow.app.domain.model

/**
 * Domain-level transaction. Amounts are stored in the smallest unit-agnostic
 * Double together with an ISO-4217 [currencyCode] (default IRR, secondary USD).
 *
 * @param id Room primary key (0 = not yet persisted)
 * @param amount positive amount in [currencyCode]
 * @param type income or expense
 * @param categoryId FK to [Category]
 * @param dateEpochDay days since epoch (LocalDate.toEpochDay)
 * @param note optional user note
 * @param paymentMethod optional label, e.g. "Cash", "Card", "Bank transfer"
 * @param currencyCode ISO-4217 code, e.g. "IRR" or "USD"
 * @param createdAtMillis wall-clock creation time for stable ordering
 */
data class Transaction(
    val id: Long = 0,
    val amount: Double,
    val type: TransactionType,
    val categoryId: Long,
    val dateEpochDay: Long,
    val note: String = "",
    val paymentMethod: String? = null,
    val currencyCode: String = "IRR",
    val createdAtMillis: Long = System.currentTimeMillis()
)
