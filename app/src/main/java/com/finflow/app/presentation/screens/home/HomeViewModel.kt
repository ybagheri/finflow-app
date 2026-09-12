package com.finflow.app.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finflow.app.domain.model.Transaction
import com.finflow.app.domain.model.TransactionSort
import com.finflow.app.domain.model.TransactionSortField
import com.finflow.app.domain.repository.CategoryRepository
import com.finflow.app.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Home view-model: exposes reactive balance/income/expense totals,
 * a recent-transactions preview, and triggers default-category
 * seeding on first launch.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    transactions: TransactionRepository,
    private val categoriesRepo: CategoryRepository
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

    init {
        viewModelScope.launch { categoriesRepo.seedDefaultsIfEmpty() }
    }
}
