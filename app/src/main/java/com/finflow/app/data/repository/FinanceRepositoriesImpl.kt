package com.finflow.app.data.repository

import com.finflow.app.data.local.dao.BudgetDao
import com.finflow.app.data.local.dao.CategoryDao
import com.finflow.app.data.local.dao.GoalDao
import com.finflow.app.data.local.dao.RecurringRuleDao
import com.finflow.app.data.mapper.toDomain
import com.finflow.app.data.mapper.toEntity
import com.finflow.app.domain.model.Budget
import com.finflow.app.domain.model.Category
import com.finflow.app.domain.model.Goal
import com.finflow.app.domain.model.RecurringRule
import com.finflow.app.domain.model.TransactionType
import com.finflow.app.domain.repository.BudgetRepository
import com.finflow.app.domain.repository.CategoryRepository
import com.finflow.app.domain.repository.GoalRepository
import com.finflow.app.domain.repository.RecurringRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Room-backed category repository with first-launch seeding.
 */
class CategoryRepositoryImpl @Inject constructor(
    private val dao: CategoryDao
) : CategoryRepository {
    override fun observeAll() = dao.observeAll().map { it.map { e -> e.toDomain() } }
    override fun observeByType(type: TransactionType) =
        dao.observeByType(type.name).map { it.map { e -> e.toDomain() } }

    override suspend fun getById(id: Long): Category? = dao.getById(id)?.toDomain()
    override suspend fun upsert(category: Category): Long = dao.upsert(category.toEntity())
    override suspend fun deleteById(id: Long) = dao.deleteById(id)

    override suspend fun seedDefaultsIfEmpty() {
        if (dao.count() > 0) return
        DefaultCategories.list.forEach { dao.upsert(it.toEntity()) }
    }
}

/** Built-in categories seeded on first launch. */
internal object DefaultCategories {
    val list: List<Category> = listOf(
        Category(name = "Salary", type = TransactionType.INCOME, iconKey = "work", isDefault = true),
        Category(name = "Business", type = TransactionType.INCOME, iconKey = "business", isDefault = true),
        Category(name = "Other Income", type = TransactionType.INCOME, iconKey = "add", isDefault = true),
        Category(name = "Food", type = TransactionType.EXPENSE, iconKey = "food", isDefault = true),
        Category(name = "Transport", type = TransactionType.EXPENSE, iconKey = "transport", isDefault = true),
        Category(name = "Housing", type = TransactionType.EXPENSE, iconKey = "home", isDefault = true),
        Category(name = "Shopping", type = TransactionType.EXPENSE, iconKey = "shopping", isDefault = true),
        Category(name = "Health", type = TransactionType.EXPENSE, iconKey = "health", isDefault = true),
        Category(name = "Entertainment", type = TransactionType.EXPENSE, iconKey = "entertainment", isDefault = true),
        Category(name = "Other Expense", type = TransactionType.EXPENSE, iconKey = "remove", isDefault = true)
    )
}

/** Room-backed budget repository (UI in Phase 4). */
class BudgetRepositoryImpl @Inject constructor(
    private val dao: BudgetDao
) : BudgetRepository {
    override fun observeForMonth(monthKey: String): Flow<List<Budget>> =
        dao.observeForMonth(monthKey).map { it.map { e -> e.toDomain() } }

    override suspend fun upsert(budget: Budget): Long = dao.upsert(budget.toEntity())
    override suspend fun deleteById(id: Long) = dao.deleteById(id)
}

/** Room-backed goal repository (UI in Phase 4). */
class GoalRepositoryImpl @Inject constructor(
    private val dao: GoalDao
) : GoalRepository {
    override fun observeAll(): Flow<List<Goal>> =
        dao.observeAll().map { it.map { e -> e.toDomain() } }

    override suspend fun upsert(goal: Goal): Long = dao.upsert(goal.toEntity())
    override suspend fun deleteById(id: Long) = dao.deleteById(id)
}

/** Room-backed recurring-rule repository (scheduling in Phase 4). */
class RecurringRepositoryImpl @Inject constructor(
    private val dao: RecurringRuleDao
) : RecurringRepository {
    override fun observeActive(): Flow<List<RecurringRule>> =
        dao.observeActive().map { it.map { e -> e.toDomain() } }

    override suspend fun upsert(rule: RecurringRule): Long = dao.upsert(rule.toEntity())
    override suspend fun deleteById(id: Long) = dao.deleteById(id)
}
