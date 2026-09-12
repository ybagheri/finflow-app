package com.finflow.app.data.prefs

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Typed wrapper over the app DataStore.
 * Phase 4 keys: recurring-worker bookkeeping + fired budget alerts.
 * Phase 5 keys: theme, display currency, biometric lock, onboarding.
 */
@Singleton
class UserPreferences @Inject constructor(
    private val store: DataStore<Preferences>
) {
    private object Keys {
        val LAST_RECURRING_RUN = longPreferencesKey("recurring_last_run_epoch")
        val NOTIFIED_BUDGETS = stringSetPreferencesKey("notified_budgets")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val DISPLAY_CURRENCY = stringPreferencesKey("display_currency")
        val IRR_PER_USD = doublePreferencesKey("irr_per_usd")
        val BIOMETRIC_LOCK = booleanPreferencesKey("biometric_lock")
        val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
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

    /** Theme choice, defaulting to system. */
    val themeMode: Flow<String> = store.data.map { it[Keys.THEME_MODE] ?: "SYSTEM" }
    suspend fun setThemeMode(mode: String) {
        store.edit { it[Keys.THEME_MODE] = mode }
    }

    /** Material You dynamic color (Android 12+), enabled by default. */
    val dynamicColor: Flow<Boolean> = store.data.map { it[Keys.DYNAMIC_COLOR] ?: true }
    suspend fun setDynamicColor(enabled: Boolean) {
        store.edit { it[Keys.DYNAMIC_COLOR] = enabled }
    }

    /** Display currency for home totals ("IRR" or "USD"). */
    val displayCurrency: Flow<String> = store.data.map { it[Keys.DISPLAY_CURRENCY] ?: "IRR" }
    suspend fun setDisplayCurrency(code: String) {
        store.edit { it[Keys.DISPLAY_CURRENCY] = code }
    }

    /** User-editable conversion rate (IRR per 1 USD). */
    val irrPerUsd: Flow<Double> = store.data.map { it[Keys.IRR_PER_USD] ?: 42_000.0 }
    suspend fun setIrrPerUsd(rate: Double) {
        if (rate > 0) store.edit { it[Keys.IRR_PER_USD] = rate }
    }

    /** Whether the app asks for biometrics on launch. */
    val biometricLock: Flow<Boolean> = store.data.map { it[Keys.BIOMETRIC_LOCK] ?: false }
    suspend fun setBiometricLock(enabled: Boolean) {
        store.edit { it[Keys.BIOMETRIC_LOCK] = enabled }
    }

    /** Null while loading so callers can hold a splash; true once onboarded. */
    val onboardingDone: Flow<Boolean> = store.data.map { it[Keys.ONBOARDING_DONE] ?: false }
    suspend fun setOnboardingDone(done: Boolean) {
        store.edit { it[Keys.ONBOARDING_DONE] = done }
    }
}
