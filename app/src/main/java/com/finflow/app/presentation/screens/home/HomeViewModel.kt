package com.finflow.app.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finflow.app.domain.repository.CategoryRepository
import com.finflow.app.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Home view-model: exposes reactive balance/income/expense totals
 * and triggers default-category seeding on first launch.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    transactions: TransactionRepository,
    private val categories: CategoryRepository
) : ViewModel() {

    val balance = transactions.observeBalance()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    val incomeTotal = transactions.observeIncomeTotal()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    val expenseTotal = transactions.observeExpenseTotal()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    init {
        viewModelScope.launch { categories.seedDefaultsIfEmpty() }
    }
}
