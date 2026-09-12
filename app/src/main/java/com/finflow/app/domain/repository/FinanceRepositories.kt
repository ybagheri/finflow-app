package com.finflow.app.domain.repository

import com.finflow.app.domain.model.Budget
import com.finflow.app.domain.model.Goal
import com.finflow.app.domain.model.RecurringRule
import kotlinx.coroutines.flow.Flow

/** Contract for budgets (monthly caps per category). Full UI lands in Phase 4. */
interface BudgetRepository {
    fun observeForMonth(monthKey: String): Flow<List<Budget>>
    suspend fun upsert(budget: Budget): Long
    suspend fun deleteById(id: Long)
}

/** Contract for savings goals. Full UI lands in Phase 4. */
interface GoalRepository {
    fun observeAll(): Flow<List<Goal>>
    suspend fun upsert(goal: Goal): Long
    suspend fun deleteById(id: Long)
}

/** Contract for recurring-transaction rules. Scheduling lands in Phase 4. */
interface RecurringRepository {
    fun observeActive(): Flow<List<RecurringRule>>
    fun observeAll(): Flow<List<RecurringRule>>
    suspend fun upsert(rule: RecurringRule): Long
    suspend fun deleteById(id: Long)
}
