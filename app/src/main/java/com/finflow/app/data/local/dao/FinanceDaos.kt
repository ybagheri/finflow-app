package com.finflow.app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.finflow.app.data.local.entity.BudgetEntity
import com.finflow.app.data.local.entity.GoalEntity
import com.finflow.app.data.local.entity.RecurringRuleEntity
import kotlinx.coroutines.flow.Flow

/** Budget queries (full feature UI in Phase 4). */
@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets WHERE monthKey = :monthKey")
    fun observeForMonth(monthKey: String): Flow<List<BudgetEntity>>

    @Upsert
    suspend fun upsert(entity: BudgetEntity): Long

    @Query("DELETE FROM budgets WHERE id = :id")
    suspend fun deleteById(id: Long)
}

/** Goal queries (full feature UI in Phase 4). */
@Dao
interface GoalDao {
    @Query("SELECT * FROM goals ORDER BY title ASC")
    fun observeAll(): Flow<List<GoalEntity>>

    @Upsert
    suspend fun upsert(entity: GoalEntity): Long

    @Query("DELETE FROM goals WHERE id = :id")
    suspend fun deleteById(id: Long)
}

/** Recurring-rule queries (scheduling in Phase 4). */
@Dao
interface RecurringRuleDao {
    @Query("SELECT * FROM recurring_rules WHERE isActive = 1 ORDER BY startEpochDay ASC")
    fun observeActive(): Flow<List<RecurringRuleEntity>>

    @Upsert
    suspend fun upsert(entity: RecurringRuleEntity): Long

    @Query("DELETE FROM recurring_rules WHERE id = :id")
    suspend fun deleteById(id: Long)
}
