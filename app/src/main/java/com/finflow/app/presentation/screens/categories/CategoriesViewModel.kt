package com.finflow.app.presentation.screens.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finflow.app.domain.model.Category
import com.finflow.app.domain.model.TransactionType
import com.finflow.app.domain.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Preset chip colors for the Phase 2 category dialog. */
val CATEGORY_COLORS = listOf(
    0xFF6750A4.toInt(), 0xFF2E7D32.toInt(), 0xFF1565C0.toInt(),
    0xFFEF6C00.toInt(), 0xFFC62828.toInt(), 0xFF6A1B9A.toInt(),
    0xFF00838F.toInt(), 0xFF4E342E.toInt()
)

/** Phase 2 category CRUD: create/rename/recolor/delete (defaults are protected). */
@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val repository: CategoryRepository
) : ViewModel() {
    val categories: StateFlow<List<Category>> = repository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        viewModelScope.launch { repository.seedDefaultsIfEmpty() }
    }

    fun clearError() { _error.value = null }

    /** Creates or renames a category. Returns false when the name is blank. */
    fun saveCategory(
        existing: Category?,
        name: String,
        type: TransactionType,
        colorArgb: Int,
        onDone: () -> Unit
    ) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) {
            _error.value = "Category name cannot be empty"
            return
        }
        viewModelScope.launch {
            repository.upsert(
                (existing ?: Category(name = trimmed, type = type)).copy(
                    name = trimmed,
                    type = type,
                    colorArgb = colorArgb
                )
            )
            _error.value = null
            onDone()
        }
    }

    /** Deletes custom categories only; default seeds cannot be removed. */
    fun deleteCategory(category: Category) {
        if (category.isDefault) {
            _error.value = "Default categories cannot be deleted"
            return
        }
        viewModelScope.launch { repository.deleteById(category.id) }
    }
}
