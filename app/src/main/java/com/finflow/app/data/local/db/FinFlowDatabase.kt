package com.finflow.app.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.finflow.app.data.local.dao.BudgetDao
import com.finflow.app.data.local.dao.CategoryDao
import com.finflow.app.data.local.dao.GoalDao
import com.finflow.app.data.local.dao.RecurringRuleDao
import com.finflow.app.data.local.dao.TransactionDao
import com.finflow.app.data.local.entity.BudgetEntity
import com.finflow.app.data.local.entity.CategoryEntity
import com.finflow.app.data.local.entity.GoalEntity
import com.finflow.app.data.local.entity.RecurringRuleEntity
import com.finflow.app.data.local.entity.TransactionEntity

/**
 * Offline-first Room database for FinFlow.
 *
 * Version 1 covers Phase 1 entities. Later phases add columns/tables
 * via explicit migrations (never destructive in release builds).
 */
@Database(
    entities = [
        TransactionEntity::class,
        CategoryEntity::class,
        BudgetEntity::class,
        GoalEntity::class,
        RecurringRuleEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class FinFlowDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun budgetDao(): BudgetDao
    abstract fun goalDao(): GoalDao
    abstract fun recurringRuleDao(): RecurringRuleDao

    companion object {
        const val NAME = "finflow.db"
    }
}
