package com.finflow.app.presentation.screens.settings

import androidx.biometric.BiometricManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.finflow.app.core.util.LANGUAGE_PERSIAN

/**
 * Phase 5 settings: theme mode + dynamic color, display currency with an
 * editable rate, the biometric app lock toggle. Phase 6 adds the app
 * language switch (same choice offered once at onboarding, editable here).
 */
@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val themeMode by viewModel.themeMode.collectAsState()
    val dynamicColor by viewModel.dynamicColor.collectAsState()
    val appLanguage by viewModel.appLanguage.collectAsState()
    val displayCurrency by viewModel.displayCurrency.collectAsState()
    val rate by viewModel.rateFor(displayCurrency).collectAsState(initial = 1.0)
    val biometricLock by viewModel.biometricLock.collectAsState()
    val context = LocalContext.current
    val fa = appLanguage == LANGUAGE_PERSIAN

    val biometricAvailable = remember {
        BiometricManager.from(context).canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG
        ) == BiometricManager.BIOMETRIC_SUCCESS
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(if (fa) "تنظیمات" else "Settings", style = MaterialTheme.typography.headlineSmall)

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(if (fa) "زبان" else "Language", style = MaterialTheme.typography.titleMedium)
                viewModel.languageOptions.forEach { option ->
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .selectable(
                                selected = appLanguage == option.value,
                                role = Role.RadioButton,
                                onClick = {
                                    viewModel.setLanguage(option.value) {
                                        (context as? android.app.Activity)?.recreate()
                                    }
                                }
                            )
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = appLanguage == option.value,
                            onClick = {
                                viewModel.setLanguage(option.value) {
                                    (context as? android.app.Activity)?.recreate()
                                }
                            }
                        )
                        Text(option.label)
                    }
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(if (fa) "ظاهر" else "Appearance", style = MaterialTheme.typography.titleMedium)
                viewModel.themeOptions.forEach { option ->
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .selectable(
                                selected = themeMode == option.value,
                                role = Role.RadioButton,
                                onClick = { viewModel.setThemeMode(option.value) }
                            )
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = themeMode == option.value,
                            onClick = { viewModel.setThemeMode(option.value) }
                        )
                        Text(option.label)
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(if (fa) "رنگ پویا (اندروید ۱۲+)" else "Dynamic color (Android 12+)")
                    Switch(
                        checked = dynamicColor,
                        onCheckedChange = { viewModel.setDynamicColor(it) }
                    )
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(if (fa) "واحد پول" else "Currency", style = MaterialTheme.typography.titleMedium)
                viewModel.currencyOptions(appLanguage).forEach { option ->
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .selectable(
                                selected = displayCurrency == option.value,
                                role = Role.RadioButton,
                                onClick = { viewModel.setDisplayCurrency(option.value) }
                            )
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = displayCurrency == option.value,
                            onClick = { viewModel.setDisplayCurrency(option.value) }
                        )
                        Text(option.label)
                    }
                }
                if (displayCurrency != "IRR") {
                    var rateText by remember(rate, displayCurrency) { mutableStateOf(rate.toString()) }
                    var rateError by remember(displayCurrency) { mutableStateOf<String?>(null) }
                    var savedTick by remember(displayCurrency) { mutableStateOf(false) }
                    OutlinedTextField(
                        value = rateText,
                        onValueChange = { rateText = it; rateError = null; savedTick = false },
                        label = { Text(if (fa) "ریال به ازای هر واحد $displayCurrency" else "IRR per 1 $displayCurrency") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        isError = rateError != null,
                        supportingText = {
                            Text(
                                rateError
                                    ?: if (savedTick) {
                                        if (fa) "ذخیره شد" else "Saved"
                                    } else {
                                        if (fa) "برای تبدیل مجموع‌ها استفاده می‌شود" else "Used to convert totals"
                                    }
                            )
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    TextButton(onClick = {
                        viewModel.setRate(
                            displayCurrency,
                            rateText,
                            onDone = { savedTick = true },
                            onError = { rateError = it }
                        )
                    }) { Text(if (fa) "ذخیره نرخ" else "Save rate") }
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(if (fa) "امنیت" else "Security", style = MaterialTheme.typography.titleMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(if (fa) "قفل بیومتریک برنامه" else "Biometric app lock")
                    Switch(
                        checked = biometricLock,
                        enabled = biometricAvailable,
                        onCheckedChange = { viewModel.setBiometricLock(it) }
                    )
                }
                if (!biometricAvailable) {
                    Text(
                        if (fa) "هیچ بیومتریکی روی این دستگاه ثبت نشده است." else "No biometrics enrolled on this device.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
