package com.finflow.app.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finflow.app.core.util.CategoryDelta
import com.finflow.app.core.util.InsightsUtils
import com.finflow.app.core.util.ReportUtils
import com.finflow.app.data.prefs.UserPreferences
import com.finflow.app.domain.model.Transaction
import com.finflow.app.domain.model.TransactionSort
import com.finflow.app.domain.model.TransactionSortField
import com.finflow.app.domain.repository.CategoryRepository
import com.finflow.app.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.YearMonth
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Smart-insights snapshot for the home card (Phase 4): 30-day daily
 * average, logging streak, and the biggest month-over-month mover.
 */
data class HomeInsights(
    val dailyAverage: Double = 0.0,
    val streakDays: Int = 0,
    val topDelta: CategoryDelta? = null,
    val topDeltaCategoryName: String? = null
)

/**
 * Home view-model: exposes reactive balance/income/expense totals,
 * a recent-transactions preview, smart insights, and triggers
 * default-category seeding on first launch.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    transactions: TransactionRepository,
    private val categoriesRepo: CategoryRepository,
    prefs: UserPreferences
) : ViewModel() {

    val balance = transactions.observeBalance()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    val incomeTotal = transactions.observeIncomeTotal()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    val expenseTotal = transactions.observeExpenseTotal()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    /** Most recent 5 transactions (date desc) for the Phase 2 home preview. */
    val recentTransactions: StateFlow<List<Transaction>> =
        transactions.observeTransactions(
            TransactionSort(TransactionSortField.DATE, ascending = false)
        ).map { it.take(5) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Category lookup for resolving names/colors in the preview rows. */
    val categoryList = categoriesRepo.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Phase 5 display currency + rate for converting home totals. */
    val displayCurrency: StateFlow<String> = prefs.displayCurrency
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "IRR")
    val irrPerUsd: StateFlow<Double> = prefs.irrPerUsd
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 42_000.0)

    /** Full transaction list backing the smart-insights card. */
    private val allTransactions: StateFlow<List<Transaction>> =
        transactions.observeTransactions(
            TransactionSort(TransactionSortField.DATE, ascending = false)
        ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Phase 4 smart insights: daily average, streak, biggest MoM mover. */
    val insights: StateFlow<HomeInsights> =
        combine(allTransactions, categoryList) { txs, cats ->
            val now = YearMonth.now()
            val deltas = InsightsUtils.monthDeltas(
                ReportUtils.inMonth(txs, now),
                ReportUtils.inMonth(txs, now.minusMonths(1))
            )
            val top = deltas.firstOrNull()
            HomeInsights(
                dailyAverage = InsightsUtils.dailyAverage(txs),
                streakDays = InsightsUtils.loggingStreak(txs),
                topDelta = top,
                topDeltaCategoryName = top?.let { d -> cats.firstOrNull { it.id == d.categoryId }?.name }
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeInsights())

    init {
        viewModelScope.launch { categoriesRepo.seedDefaultsIfEmpty() }
    }
}
