package com.finflow.app.presentation.screens.recurring

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finflow.app.domain.model.Category
import com.finflow.app.domain.model.RecurrenceInterval
import com.finflow.app.domain.model.RecurringRule
import com.finflow.app.domain.model.TransactionType
import com.finflow.app.domain.repository.CategoryRepository
import com.finflow.app.domain.repository.RecurringRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Phase 4 recurring rules: CRUD + active toggles. Materialization itself
 * lives in [com.finflow.app.data.work.RecurringScheduler] (daily worker
 * + manual "Run now" via WorkManager one-shot).
 */
@HiltViewModel
class RecurringViewModel @Inject constructor(
    private val repository: RecurringRepository,
    categories: CategoryRepository
) : ViewModel() {

    val rules: StateFlow<List<RecurringRule>> = repository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** All categories for the rule editor (filtered by type in the UI). */
    val categories: StateFlow<List<Category>> = categories.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun saveRule(
        existing: RecurringRule?,
        amount: Double,
        type: TransactionType,
        categoryId: Long,
        interval: RecurrenceInterval,
        startEpochDay: Long,
        endEpochDay: Long?,
        note: String,
        paymentMethod: String?,
        onDone: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (amount <= 0) {
            onError("Amount must be greater than 0")
            return
        }
        if (categoryId <= 0) {
            onError("Pick a category")
            return
        }
        if (endEpochDay != null && endEpochDay < startEpochDay) {
            onError("End date cannot be before the start date")
            return
        }
        viewModelScope.launch {
            repository.upsert(
                (existing ?: RecurringRule(
                    amount = amount,
                    type = type,
                    categoryId = categoryId,
                    interval = interval,
                    startEpochDay = startEpochDay
                )).copy(
                    amount = amount,
                    type = type,
                    categoryId = categoryId,
                    interval = interval,
                    startEpochDay = startEpochDay,
                    endEpochDay = endEpochDay,
                    note = note.trim(),
                    paymentMethod = paymentMethod?.trim().ifNullOrBlank()
                )
            )
            onDone()
        }
    }

    fun setActive(rule: RecurringRule, active: Boolean) {
        viewModelScope.launch { repository.upsert(rule.copy(isActive = active)) }
    }

    fun delete(rule: RecurringRule) {
        viewModelScope.launch { repository.deleteById(rule.id) }
    }
}

private fun String?.ifNullOrBlank(): String? =
    if (this == null || this.isBlank()) null else this
