package com.finflow.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity for income/expense rows.
 *
 * Mirrors [com.finflow.app.domain.model.Transaction] 1:1 so mapping stays trivial.
 * `type` is stored as the enum name ("INCOME"/"EXPENSE").
 */
@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("categoryId"), Index("dateEpochDay"), Index("type")]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double,
    val type: String,
    val categoryId: Long,
    val dateEpochDay: Long,
    val note: String = "",
    val paymentMethod: String? = null,
    val currencyCode: String = "IRR",
    val createdAtMillis: Long = System.currentTimeMillis()
)
