package com.finflow.app.presentation.screens.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finflow.app.domain.model.Category
import com.finflow.app.domain.model.Transaction
import com.finflow.app.domain.model.TransactionSort
import com.finflow.app.domain.model.TransactionSortField
import com.finflow.app.domain.model.TransactionType
import com.finflow.app.domain.repository.CategoryRepository
import com.finflow.app.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Phase 2 list state: sort order, search query and type/category filters.
 * The transaction stream re-queries Room on every change.
 */
@HiltViewModel
class TransactionsViewModel @Inject constructor(
    private val repository: TransactionRepository,
    categoriesRepo: CategoryRepository
) : ViewModel() {

    private val _sort = MutableStateFlow(TransactionSort(TransactionSortField.DATE, false))
    val sort: StateFlow<TransactionSort> = _sort.asStateFlow()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _typeFilter = MutableStateFlow<TransactionType?>(null)
    val typeFilter: StateFlow<TransactionType?> = _typeFilter.asStateFlow()

    private val _categoryFilter = MutableStateFlow<Long?>(null)
    val categoryFilter: StateFlow<Long?> = _categoryFilter.asStateFlow()

    val categories: StateFlow<List<Category>> = categoriesRepo.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Reactive transaction list honoring current sort/filter state. */
    @OptIn(ExperimentalCoroutinesApi::class)
    val transactions: StateFlow<List<Transaction>> =
        combine(_sort, _query, _typeFilter, _categoryFilter) { s, q, t, c ->
            Quad(s, q, t, c)
        }.flatMapLatest { (s, q, t, c) ->
            repository.observeTransactions(s, q, t, c)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Switch sort field/direction from the Phase 2 sort menu. */
    fun setSort(field: TransactionSortField, ascending: Boolean) {
        _sort.value = TransactionSort(field, ascending)
    }

    /** Update free-text search query. */
    fun setQuery(value: String) { _query.value = value }

    /** Filter by income/expense (null = both). */
    fun setTypeFilter(type: TransactionType?) { _typeFilter.value = type }

    /** Filter by category (null = all). */
    fun setCategoryFilter(id: Long?) { _categoryFilter.value = id }

    fun clearFilters() {
        _query.value = ""
        _typeFilter.value = null
        _categoryFilter.value = null
    }

    /** Swipe-to-delete handler. */
    fun delete(id: Long) {
        viewModelScope.launch { repository.deleteById(id) }
    }

    private data class Quad(
        val sort: TransactionSort,
        val query: String,
        val type: TransactionType?,
        val categoryId: Long?
    )
}
