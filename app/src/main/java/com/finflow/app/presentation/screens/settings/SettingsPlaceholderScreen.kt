package com.finflow.app.presentation.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Placeholder for settings (theme, currency, biometric lock).
 * Full implementation lands in Phase 5.
 */
@Composable
fun SettingsPlaceholderScreen() {
    Column(Modifier.padding(16.dp)) {
        Text("Settings", style = MaterialTheme.typography.headlineSmall)
        Text("Theme, currency (IRR/USD), biometric lock — Phase 5.")
    }
}
