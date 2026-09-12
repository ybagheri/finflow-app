package com.finflow.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Recurring-transaction rule entity. Materialization via WorkManager lands in Phase 4.
 */
@Entity(
    tableName = "recurring_rules",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("categoryId"), Index("isActive")]
)
data class RecurringRuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double,
    val type: String,
    val categoryId: Long,
    val interval: String,
    val startEpochDay: Long,
    val endEpochDay: Long? = null,
    val note: String = "",
    val paymentMethod: String? = null,
    val currencyCode: String = "IRR",
    val isActive: Boolean = true
)
