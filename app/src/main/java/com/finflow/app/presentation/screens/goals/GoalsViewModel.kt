package com.finflow.app.presentation.screens.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finflow.app.domain.model.Goal
import com.finflow.app.domain.repository.GoalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Phase 4 goals: CRUD + progress tracking via deposits
 * (savedAmount is denormalized on the goal for instant UI).
 */
@HiltViewModel
class GoalsViewModel @Inject constructor(
    private val repository: GoalRepository
) : ViewModel() {

    val goals: StateFlow<List<Goal>> = repository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Creates or updates a goal; blank title / non-positive target are rejected. */
    fun saveGoal(
        existing: Goal?,
        title: String,
        target: Double,
        deadlineEpochDay: Long?,
        note: String,
        onDone: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (title.isBlank()) {
            onError("Title cannot be empty")
            return
        }
        if (target <= 0) {
            onError("Target must be greater than 0")
            return
        }
        viewModelScope.launch {
            repository.upsert(
                (existing ?: Goal(title = title.trim(), targetAmount = target)).copy(
                    title = title.trim(),
                    targetAmount = target,
                    deadlineEpochDay = deadlineEpochDay,
                    note = note.trim()
                )
            )
            onDone()
        }
    }

    /** Adds a deposit towards [goal]; non-positive amounts are rejected. */
    fun deposit(goal: Goal, amount: Double, onDone: () -> Unit, onError: (String) -> Unit) {
        if (amount <= 0) {
            onError("Deposit must be greater than 0")
            return
        }
        viewModelScope.launch {
            repository.upsert(goal.copy(savedAmount = goal.savedAmount + amount))
            onDone()
        }
    }

    fun delete(goal: Goal) {
        viewModelScope.launch { repository.deleteById(goal.id) }
    }
}
