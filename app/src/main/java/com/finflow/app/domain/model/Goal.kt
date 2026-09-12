package com.finflow.app.domain.model

/**
 * Savings goal with progress derived from linked deposits (Phase 4).
 *
 * @param targetAmount goal amount in [currencyCode]
 * @param savedAmount amount saved so far (denormalized for fast UI)
 * @param deadlineEpochDay optional deadline as epoch day, null = open-ended
 */
data class Goal(
    val id: Long = 0,
    val title: String,
    val targetAmount: Double,
    val savedAmount: Double = 0.0,
    val currencyCode: String = "IRR",
    val deadlineEpochDay: Long? = null,
    val note: String = ""
) {
    /** 0..1 progress fraction, coerced to a valid range. */
    val progress: Float
        get() = if (targetAmount <= 0) 0f
        else (savedAmount / targetAmount).toFloat().coerceIn(0f, 1f)
}
