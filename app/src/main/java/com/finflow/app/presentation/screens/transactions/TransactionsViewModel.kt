package com.finflow.app.presentation.screens.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finflow.app.domain.model.Transaction
import com.finflow.app.domain.model.TransactionSort
import com.finflow.app.domain.model.TransactionSortField
import com.finflow.app.domain.model.TransactionType
import com.finflow.app.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

/**
 * Holds list-screen state: sort order, search query and type/category filters.
 * The transaction stream re-queries Room on every change.
 */
@HiltViewModel
class TransactionsViewModel @Inject constructor(
    private val repository: TransactionRepository
) : ViewModel() {

    private val sort = MutableStateFlow(TransactionSort(TransactionSortField.DATE, false))
    private val query = MutableStateFlow("")
    private val typeFilter = MutableStateFlow<TransactionType?>(null)
    private val categoryFilter = MutableStateFlow<Long?>(null)

    /** Reactive transaction list honoring current sort/filter state. */
    @OptIn(ExperimentalCoroutinesApi::class)
    val transactions: StateFlow<List<Transaction>> =
        combine(sort, query, typeFilter, categoryFilter) { s, q, t, c ->
            Quad(s, q, t, c)
        }.flatMapLatest { (s, q, t, c) ->
            repository.observeTransactions(s, q, t, c)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Switch sort field/direction from the Phase 2 sort menu. */
    fun setSort(field: TransactionSortField, ascending: Boolean) {
        sort.value = TransactionSort(field, ascending)
    }

    /** Update free-text search query. */
    fun setQuery(value: String) { query.value = value }

    /** Filter by income/expense (null = both). */
    fun setTypeFilter(type: TransactionType?) { typeFilter.value = type }

    /** Filter by category (null = all). */
    fun setCategoryFilter(id: Long?) { categoryFilter.value = id }

    private data class Quad(
        val sort: TransactionSort,
        val query: String,
        val type: TransactionType?,
        val categoryId: Long?
    )
}
