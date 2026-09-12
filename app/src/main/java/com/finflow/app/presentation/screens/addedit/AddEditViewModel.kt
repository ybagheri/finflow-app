package com.finflow.app.presentation.screens.addedit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finflow.app.core.util.DateUtils
import com.finflow.app.domain.model.Category
import com.finflow.app.domain.model.Transaction
import com.finflow.app.domain.model.TransactionType
import com.finflow.app.domain.repository.CategoryRepository
import com.finflow.app.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Payment method presets for the Phase 2 picker (free text still allowed). */
val PAYMENT_METHODS = listOf("Cash", "Card", "Bank transfer", "Wallet")

/**
 * Phase 2 add/edit view-model: type toggle, category picker, date picker,
 * amount validation, payment method and edit-mode loading.
 */
@HiltViewModel
class AddEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val transactions: TransactionRepository,
    private val categories: CategoryRepository
) : ViewModel() {

    private val editingId: Long = savedStateHandle.get<Long>("id") ?: 0L
    val isEditing: Boolean = editingId != 0L

    private val _type = MutableStateFlow(
        runCatching {
            TransactionType.valueOf(savedStateHandle.get<String>("type") ?: "EXPENSE")
        }.getOrDefault(TransactionType.EXPENSE)
    )
    val type: StateFlow<TransactionType> = _type

    private val _amount = MutableStateFlow("")
    val amount: StateFlow<String> = _amount

    private val _note = MutableStateFlow("")
    val note: StateFlow<String> = _note

    private val _categoryId = MutableStateFlow<Long?>(null)
    val categoryId: StateFlow<Long?> = _categoryId

    private val _dateEpochDay = MutableStateFlow(DateUtils.todayEpochDay())
    val dateEpochDay: StateFlow<Long> = _dateEpochDay

    private val _paymentMethod = MutableStateFlow("")
    val paymentMethod: StateFlow<String> = _paymentMethod

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _saving = MutableStateFlow(false)
    val saving: StateFlow<Boolean> = _saving

    /** Categories filtered by the currently selected type. */
    @OptIn(ExperimentalCoroutinesApi::class)
    val availableCategories: StateFlow<List<Category>> = _type.flatMapLatest { t ->
        categories.observeByType(t)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch {
            categories.seedDefaultsIfEmpty()
            if (isEditing) {
                transactions.getById(editingId)?.let { tx ->
                    _type.value = tx.type
                    _amount.value = tx.amount.toString()
                    _note.value = tx.note
                    _categoryId.value = tx.categoryId
                    _dateEpochDay.value = tx.dateEpochDay
                    _paymentMethod.value = tx.paymentMethod.orEmpty()
                }
            }
        }
    }

    fun onTypeChange(value: TransactionType) {
        _type.value = value
        // Reset category selection when type changes; UI auto-selects first valid.
        _categoryId.value = null
        _error.value = null
    }
    fun onAmountChange(value: String) { _amount.value = value; _error.value = null }
    fun onNoteChange(value: String) { _note.value = value }
    fun onCategoryChange(id: Long) { _categoryId.value = id; _error.value = null }
    fun onDateChange(epochDay: Long) { _dateEpochDay.value = epochDay }
    fun onPaymentMethodChange(value: String) { _paymentMethod.value = value }

    /**
     * Validates and persists. Returns true on success so the screen can pop.
     * Shows inline [error] instead of failing silently.
     */
    suspend fun save(): Boolean {
        val parsed = _amount.value.trim().toDoubleOrNull()
        if (parsed == null || parsed <= 0) {
            _error.value = "Enter an amount greater than 0"
            return false
        }
        // Resolve category: explicit pick wins, else first of matching type.
        var resolved = _categoryId.value
        if (resolved == null) {
            resolved = availableCategories.value.firstOrNull()?.id
                ?: run {
                    categories.seedDefaultsIfEmpty()
                    categories.observeByType(_type.value).first().firstOrNull()?.id
                }
            if (resolved == null) {
                _error.value = "No category available — please retry"
                return false
            }
        }
        _saving.value = true
        return try {
            transactions.upsert(
                Transaction(
                    id = editingId,
                    amount = parsed,
                    type = _type.value,
                    categoryId = resolved,
                    dateEpochDay = _dateEpochDay.value,
                    note = _note.value.trim(),
                    paymentMethod = _paymentMethod.value.trim().ifBlank { null }
                )
            )
            true
        } finally {
            _saving.value = false
        }
    }

    /** Backwards-compatible fire-and-forget overload (Phase 1 callers). */
    fun saveFireAndForget(onDone: () -> Unit) {
        viewModelScope.launch {
            if (save()) onDone()
        }
    }
}
