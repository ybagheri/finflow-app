package com.finflow.app.domain.model

/**
 * User-defined or default category.
 *
 * @param iconKey stable key mapping to a Material icon in the UI layer
 *  (avoids persisting drawable res ids in the DB).
 * @param colorArgb ARGB int for the category chip/dot.
 * @param isDefault true for seed categories that cannot be deleted (only hidden).
 */
data class Category(
    val id: Long = 0,
    val name: String,
    val type: TransactionType,
    val iconKey: String = "category",
    val colorArgb: Int = 0xFF6750A4.toInt(),
    val isDefault: Boolean = false
)
