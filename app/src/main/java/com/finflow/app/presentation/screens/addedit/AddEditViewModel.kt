package com.finflow.app.presentation.screens.addedit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finflow.app.core.util.DateUtils
import com.finflow.app.domain.model.Transaction
import com.finflow.app.domain.model.TransactionType
import com.finflow.app.domain.repository.CategoryRepository
import com.finflow.app.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Add/edit view-model. Phase 1 supports amount + note against the first
 * available category; Phase 2 adds pickers, validation and edit loading.
 */
@HiltViewModel
class AddEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val transactions: TransactionRepository,
    private val categories: CategoryRepository
) : ViewModel() {

    private val editingId: Long = savedStateHandle.get<Long>("id") ?: 0L
    private val initialType: TransactionType =
        runCatching {
            TransactionType.valueOf(savedStateHandle.get<String>("type") ?: "EXPENSE")
        }.getOrDefault(TransactionType.EXPENSE)

    private val _amount = MutableStateFlow("")
    val amount: StateFlow<String> = _amount

    private val _note = MutableStateFlow("")
    val note: StateFlow<String> = _note

    fun onAmountChange(value: String) { _amount.value = value }
    fun onNoteChange(value: String) { _note.value = value }

    /** Validates minimally and persists; returns silently on invalid input (Phase 2 shows errors). */
    fun save() {
        val parsed = _amount.value.toDoubleOrNull() ?: return
        if (parsed <= 0) return
        viewModelScope.launch {
            categories.seedDefaultsIfEmpty()
            // Phase 1: resolve a category of matching type; full picker in Phase 2.
            val first = categories.observeByType(initialType).first().firstOrNull()
                ?: return@launch
            transactions.upsert(
                Transaction(
                    id = editingId,
                    amount = parsed,
                    type = initialType,
                    categoryId = first.id,
                    dateEpochDay = DateUtils.todayEpochDay(),
                    note = _note.value.trim()
                )
            )
        }
    }
}
