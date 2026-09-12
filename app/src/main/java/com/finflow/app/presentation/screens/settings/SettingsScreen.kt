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

/**
 * Phase 5 settings: theme mode + dynamic color, display currency with an
 * editable IRR/USD rate, and the biometric app lock toggle.
 */
@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val themeMode by viewModel.themeMode.collectAsState()
    val dynamicColor by viewModel.dynamicColor.collectAsState()
    val displayCurrency by viewModel.displayCurrency.collectAsState()
    val irrPerUsd by viewModel.irrPerUsd.collectAsState()
    val biometricLock by viewModel.biometricLock.collectAsState()
    val context = LocalContext.current

    val biometricAvailable = remember {
        BiometricManager.from(context).canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG
        ) == BiometricManager.BIOMETRIC_SUCCESS
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineSmall)

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Appearance", style = MaterialTheme.typography.titleMedium)
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
                    Text("Dynamic color (Android 12+)")
                    Switch(
                        checked = dynamicColor,
                        onCheckedChange = { viewModel.setDynamicColor(it) }
                    )
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Currency", style = MaterialTheme.typography.titleMedium)
                viewModel.currencyOptions.forEach { option ->
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
                var rate by remember(irrPerUsd) { mutableStateOf(irrPerUsd.toString()) }
                var rateError by remember { mutableStateOf<String?>(null) }
                var savedTick by remember { mutableStateOf(false) }
                OutlinedTextField(
                    value = rate,
                    onValueChange = { rate = it; rateError = null; savedTick = false },
                    label = { Text("IRR per 1 USD") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = rateError != null,
                    supportingText = {
                        Text(rateError ?: if (savedTick) "Saved" else "Used to convert totals")
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                TextButton(onClick = {
                    viewModel.setRate(
                        rate,
                        onDone = { savedTick = true },
                        onError = { rateError = it }
                    )
                }) { Text("Save rate") }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Security", style = MaterialTheme.typography.titleMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Biometric app lock")
                    Switch(
                        checked = biometricLock,
                        enabled = biometricAvailable,
                        onCheckedChange = { viewModel.setBiometricLock(it) }
                    )
                }
                if (!biometricAvailable) {
                    Text(
                        "No biometrics enrolled on this device.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
