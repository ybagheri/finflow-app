package com.finflow.app.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finflow.app.core.util.CurrencyCatalog
import com.finflow.app.core.util.LanguageCatalog
import com.finflow.app.data.prefs.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Radio-row option used by the settings screen. */
data class SettingsOption(val value: String, val label: String)

/**
 * Phase 5 settings: theme, dynamic color, display currency + rate,
 * biometric lock. Phase 6 adds app language. All values persist in DataStore.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: UserPreferences
) : ViewModel() {

    val themeOptions = listOf(
        SettingsOption("SYSTEM", "System default"),
        SettingsOption("LIGHT", "Light"),
        SettingsOption("DARK", "Dark")
    )

    val languageOptions = LanguageCatalog.options.map { SettingsOption(it.code, it.label) }

    val themeMode: StateFlow<String> = prefs.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "SYSTEM")

    val dynamicColor: StateFlow<Boolean> = prefs.dynamicColor
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    val appLanguage: StateFlow<String> = prefs.appLanguage
        .map { it ?: "en" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "en")

    val displayCurrency: StateFlow<String> = prefs.displayCurrency
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "IRR")

    /** Currency options, labeled in whichever language is currently active. */
    fun currencyOptions(languageCode: String) =
        CurrencyCatalog.options.map { SettingsOption(it.code, CurrencyCatalog.label(it.code, languageCode)) }

    /** IRR-per-unit rate flow for [currency] (unused for IRR itself). */
    fun rateFor(currency: String): kotlinx.coroutines.flow.Flow<Double> = when (currency) {
        "USD" -> prefs.irrPerUsd
        "EUR" -> prefs.irrPerEur
        "GBP" -> prefs.irrPerGbp
        else -> prefs.irrPerUsd
    }

    val biometricLock: StateFlow<Boolean> = prefs.biometricLock
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    fun setThemeMode(mode: String) {
        viewModelScope.launch { prefs.setThemeMode(mode) }
    }

    fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch { prefs.setDynamicColor(enabled) }
    }

    /** Persists the language; the caller recreates the Activity to apply it. */
    fun setLanguage(code: String, onDone: () -> Unit) {
        viewModelScope.launch {
            prefs.setAppLanguage(code)
            onDone()
        }
    }

    fun setDisplayCurrency(code: String) {
        viewModelScope.launch { prefs.setDisplayCurrency(code) }
    }

    /** Validates the rate text; non-positive input is rejected with [onError]. */
    fun setRate(currency: String, text: String, onDone: () -> Unit, onError: (String) -> Unit) {
        val parsed = text.trim().toDoubleOrNull()
        if (parsed == null || parsed <= 0) {
            onError("Enter a rate greater than 0")
            return
        }
        viewModelScope.launch {
            prefs.setRateFor(currency, parsed)
            onDone()
        }
    }

    fun setBiometricLock(enabled: Boolean) {
        viewModelScope.launch { prefs.setBiometricLock(enabled) }
    }
}
