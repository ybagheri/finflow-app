package com.finflow.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Savings-goal entity. */
@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val targetAmount: Double,
    val savedAmount: Double = 0.0,
    val currencyCode: String = "IRR",
    val deadlineEpochDay: Long? = null,
    val note: String = ""
)
