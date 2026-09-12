package com.finflow.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** Room entity for transaction categories. */
@Entity(
    tableName = "categories",
    indices = [Index("type"), Index(value = ["name", "type"], unique = true)]
)
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: String,
    val iconKey: String = "category",
    val colorArgb: Int = 0,
    val isDefault: Boolean = false
)
