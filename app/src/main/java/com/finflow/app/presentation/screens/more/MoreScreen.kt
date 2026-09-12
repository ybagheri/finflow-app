package com.finflow.app.presentation.screens.more

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

private data class HubEntry(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

/**
 * Phase 4 hub: entry points for budgets, goals, recurring rules and
 * settings, keeping the bottom bar at five tabs.
 */
@Composable
fun MoreScreen(
    onBudgetsClick: () -> Unit,
    onGoalsClick: () -> Unit,
    onRecurringClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val entries = listOf(
        HubEntry(
            "Budgets",
            "Monthly caps with overspend alerts",
            Icons.Filled.AccountBalanceWallet,
            onBudgetsClick
        ),
        HubEntry(
            "Goals",
            "Savings targets and deposits",
            Icons.Filled.Savings,
            onGoalsClick
        ),
        HubEntry(
            "Recurring",
            "Automated transactions",
            Icons.Filled.Repeat,
            onRecurringClick
        ),
        HubEntry(
            "Settings",
            "Theme, currency, app lock",
            Icons.Filled.Settings,
            onSettingsClick
        )
    )
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("More", style = MaterialTheme.typography.headlineSmall)
        entries.forEach { entry ->
            ListItem(
                headlineContent = { Text(entry.title) },
                supportingContent = { Text(entry.subtitle) },
                leadingContent = {
                    Icon(entry.icon, contentDescription = null)
                },
                trailingContent = {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                },
                modifier = Modifier.clickable(onClick = entry.onClick)
            )
        }
    }
}
