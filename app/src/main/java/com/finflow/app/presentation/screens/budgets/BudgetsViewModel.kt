package com.finflow.app.presentation.screens.budgets

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finflow.app.core.util.Notifications
import com.finflow.app.core.util.ReportUtils
import com.finflow.app.data.prefs.UserPreferences
import com.finflow.app.domain.model.Budget
import com.finflow.app.domain.model.Category
import com.finflow.app.domain.model.TransactionSort
import com.finflow.app.domain.model.TransactionSortField
import com.finflow.app.domain.model.TransactionType
import com.finflow.app.domain.repository.BudgetRepository
import com.finflow.app.domain.repository.CategoryRepository
import com.finflow.app.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.YearMonth
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** One expense category with its optional cap and month-to-date spend. */
data class BudgetRow(
    val category: Category,
    val budget: Budget?,
    val spent: Double
) {
    val limit: Double? get() = budget?.limitAmount
    /** Null when no cap is set. */
    val progress: Float? get() = limit?.let {
        if (it <= 0) null else (spent / it).toFloat()
    }
    val overspent: Boolean get() = limit != null && spent > limit!!
}

/**
 * Phase 4 budgets: monthly per-category caps, progress, and one-shot
 * overspend notifications (deduplicated per budget+month in DataStore).
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class BudgetsViewModel @Inject constructor(
    private val budgets: BudgetRepository,
    transactions: TransactionRepository,
    categories: CategoryRepository,
    private val prefs: UserPreferences,
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    private val _month = MutableStateFlow(YearMonth.now())
    val month: StateFlow<YearMonth> = _month.asStateFlow()

    private val monthKey: String get() = _month.value.toString()

    private val monthBudgets = _month.flatMapLatest { month ->
        budgets.observeForMonth(month.toString())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val allTransactions = transactions.observeTransactions(
        TransactionSort(TransactionSortField.DATE, ascending = false)
    ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val expenseCategories = categories.observeByType(TransactionType.EXPENSE)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val rows: StateFlow<List<BudgetRow>> =
        combine(monthBudgets, allTransactions, expenseCategories, _month) { caps, txs, cats, month ->
            val inScope = ReportUtils.inMonth(txs, month)
                .filter { it.type == TransactionType.EXPENSE }
                .groupBy { it.categoryId }
            cats.map { cat ->
                BudgetRow(
                    category = cat,
                    budget = caps.firstOrNull { it.categoryId == cat.id },
                    spent = inScope[cat.id]?.sumOf { it.amount } ?: 0.0
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        // Fire each overspend alert at most once per budget+month.
        viewModelScope.launch {
            rows.collect { list ->
                val notified = prefs.notifiedBudgets.first()
                list.filter { it.overspent && it.budget != null }.forEach { row ->
                    val key = "$monthKey:${row.budget!!.id}"
                    if (key !in notified && notificationsAllowed()) {
                        Notifications.notifyOverspend(
                            appContext,
                            row.budget.id.toInt(),
                            row.category.name,
                            row.spent,
                            row.limit!!
                        )
                        prefs.markBudgetNotified(key)
                    }
                }
            }
        }
    }

    private fun notificationsAllowed(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return appContext.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
    }

    fun shiftMonth(delta: Long) { _month.value = _month.value.plusMonths(delta) }
    fun goToCurrentMonth() { _month.value = YearMonth.now() }

    /** Creates or updates the cap for a category (amount must be > 0). */
    fun setCap(category: Category, amount: Double, onDone: () -> Unit) {
        if (amount <= 0) return
        viewModelScope.launch {
            budgets.upsert(
                Budget(
                    id = rows.value.firstOrNull { it.category.id == category.id }?.budget?.id ?: 0,
                    categoryId = category.id,
                    limitAmount = amount,
                    monthKey = monthKey
                )
            )
            onDone()
        }
    }

    fun removeCap(row: BudgetRow) {
        val id = row.budget?.id ?: return
        viewModelScope.launch { budgets.deleteById(id) }
    }
}
