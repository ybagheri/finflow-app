package com.finflow.app.presentation.screens.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finflow.app.core.util.CurrencyCatalog
import com.finflow.app.core.util.LANGUAGE_PERSIAN
import com.finflow.app.core.util.LanguageCatalog
import com.finflow.app.data.prefs.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Onboarding view-model: also owns the one-time language + currency choice
 * (Phase 6), since both must be picked before the rest of the app renders
 * any text or amount.
 */
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val prefs: UserPreferences
) : ViewModel() {

    /** Device locale as a starting guess; the user can change it right away. */
    val deviceDefaultLanguage: String =
        if (Locale.getDefault().language == LANGUAGE_PERSIAN) LANGUAGE_PERSIAN else "en"

    val appLanguage: StateFlow<String?> = prefs.appLanguage
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val displayCurrency: StateFlow<String> = prefs.displayCurrency
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "IRR")

    fun setLanguage(code: String) {
        viewModelScope.launch { prefs.setAppLanguage(code) }
    }

    fun setCurrency(code: String) {
        viewModelScope.launch { prefs.setDisplayCurrency(code) }
    }

    fun complete(onDone: () -> Unit) {
        viewModelScope.launch {
            prefs.setOnboardingDone(true)
            onDone()
        }
    }
}

private data class OnboardingPoint(
    val titleEn: String,
    val subtitleEn: String,
    val titleFa: String,
    val subtitleFa: String,
    val icon: ImageVector
)

private val ONBOARDING_POINTS = listOf(
    OnboardingPoint(
        "Track every rial",
        "Log income and expenses in seconds, offline-first.",
        "هر ریال را ثبت کن",
        "درآمد و هزینه‌ها را در چند ثانیه و بدون نیاز به اینترنت ثبت کن.",
        Icons.Filled.AccountBalanceWallet
    ),
    OnboardingPoint(
        "Budgets that warn you",
        "Monthly caps per category with overspend alerts.",
        "بودجه‌ای که هشدار می‌دهد",
        "سقف ماهانه برای هر دسته، همراه با هشدار در صورت خرج زیاد.",
        Icons.Filled.Savings
    ),
    OnboardingPoint(
        "Insights that motivate",
        "Streaks, daily averages and month-over-month movers.",
        "تحلیل‌هایی که انگیزه می‌دهند",
        "روند مصرف روزانه، پیوستگی ثبت‌ها و مقایسه ماه به ماه.",
        Icons.Filled.Insights
    )
)

/**
 * Phase 5 first-launch onboarding: a mandatory language + currency picker
 * (Phase 6), followed by three value props and a get-started button. Shown
 * once (flag in DataStore); the language/currency choice remains editable
 * later from Settings.
 */
@Composable
fun OnboardingScreen(
    onDone: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val language by viewModel.appLanguage.collectAsState()
    val currency by viewModel.displayCurrency.collectAsState()
    val context = LocalContext.current
    var pickerDone by remember { mutableStateOf(false) }

    if (language == null) {
        LaunchedEffect(Unit) { viewModel.setLanguage(viewModel.deviceDefaultLanguage) }
    }

    val fa = language == LANGUAGE_PERSIAN

    if (!pickerDone) {
        LanguageAndCurrencyStep(
            language = language ?: "en",
            currency = currency,
            onLanguageSelected = { code ->
                viewModel.setLanguage(code)
                // Locale change takes effect app-wide once the Activity is recreated.
                (context as? android.app.Activity)?.recreate()
            },
            onCurrencySelected = viewModel::setCurrency,
            onContinue = { pickerDone = true }
        )
        return
    }

    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            if (fa) "به FinFlow خوش آمدید" else "Welcome to FinFlow",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(Modifier.height(16.dp))
        ONBOARDING_POINTS.forEach { point ->
            ListItem(
                headlineContent = { Text(if (fa) point.titleFa else point.titleEn) },
                supportingContent = { Text(if (fa) point.subtitleFa else point.subtitleEn) },
                leadingContent = { Icon(point.icon, contentDescription = null) }
            )
        }
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { viewModel.complete(onDone) },
            modifier = Modifier.fillMaxWidth()
        ) { Text(if (fa) "شروع کن" else "Get started") }
    }
}

/**
 * First-run choice of app language + display currency. Shown bilingually
 * (both languages at once) since we don't yet know which one to pick.
 */
@Composable
private fun LanguageAndCurrencyStep(
    language: String,
    currency: String,
    onLanguageSelected: (String) -> Unit,
    onCurrencySelected: (String) -> Unit,
    onContinue: () -> Unit
) {
    Column(
        Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Select language / انتخاب زبان",
            style = MaterialTheme.typography.headlineSmall
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                LanguageCatalog.options.forEach { option ->
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .selectable(
                                selected = language == option.code,
                                role = Role.RadioButton,
                                onClick = { onLanguageSelected(option.code) }
                            )
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = language == option.code,
                            onClick = { onLanguageSelected(option.code) }
                        )
                        Text(option.label)
                    }
                }
            }
        }

        Text(
            if (language == LANGUAGE_PERSIAN) "واحد پول" else "Currency",
            style = MaterialTheme.typography.headlineSmall
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                CurrencyCatalog.options.forEach { option ->
                    val label = CurrencyCatalog.label(option.code, language)
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .selectable(
                                selected = currency == option.code,
                                role = Role.RadioButton,
                                onClick = { onCurrencySelected(option.code) }
                            )
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currency == option.code,
                            onClick = { onCurrencySelected(option.code) }
                        )
                        Text(label)
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        Button(onClick = onContinue, modifier = Modifier.fillMaxWidth()) {
            Text(if (language == LANGUAGE_PERSIAN) "ادامه" else "Continue")
        }
    }
}
