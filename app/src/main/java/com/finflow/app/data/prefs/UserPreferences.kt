package com.finflow.app.data.prefs

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Typed wrapper over the app DataStore.
 * Phase 4 keys: recurring-worker bookkeeping + fired budget alerts.
 * (Phase 5 adds theme/lock/onboarding/currency keys in its own commit.)
 */
@Singleton
class UserPreferences @Inject constructor(
    private val store: DataStore<Preferences>
) {
    private object Keys {
        val LAST_RECURRING_RUN = longPreferencesKey("recurring_last_run_epoch")
        val NOTIFIED_BUDGETS = stringSetPreferencesKey("notified_budgets")
    }

    /** Last epoch-day the recurring worker materialized (null = never). */
    val lastRecurringRun: Flow<Long?> = store.data.map { it[Keys.LAST_RECURRING_RUN] }

    suspend fun setLastRecurringRun(epochDay: Long) {
        store.edit { it[Keys.LAST_RECURRING_RUN] = epochDay }
    }

    /** Keys of fired overspend alerts ("yyyy-MM:budgetId"). */
    val notifiedBudgets: Flow<Set<String>> =
        store.data.map { it[Keys.NOTIFIED_BUDGETS] ?: emptySet() }

    suspend fun markBudgetNotified(key: String) {
        store.edit { it[Keys.NOTIFIED_BUDGETS] = (it[Keys.NOTIFIED_BUDGETS] ?: emptySet()) + key }
    }
}
