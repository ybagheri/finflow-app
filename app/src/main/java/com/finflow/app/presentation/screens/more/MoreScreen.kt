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
import com.finflow.app.core.util.LANGUAGE_PERSIAN
import com.finflow.app.core.util.LocalAppLanguage

private data class HubEntry(
    val titleEn: String,
    val subtitleEn: String,
    val titleFa: String,
    val subtitleFa: String,
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
    val fa = LocalAppLanguage.current == LANGUAGE_PERSIAN
    val entries = listOf(
        HubEntry(
            "Budgets", "Monthly caps with overspend alerts",
            "بودجه‌ها", "سقف ماهانه با هشدار خرج بیش از حد",
            Icons.Filled.AccountBalanceWallet,
            onBudgetsClick
        ),
        HubEntry(
            "Goals", "Savings targets and deposits",
            "اهداف", "اهداف پس‌انداز و واریزی‌ها",
            Icons.Filled.Savings,
            onGoalsClick
        ),
        HubEntry(
            "Recurring", "Automated transactions",
            "تکرارشونده", "تراکنش‌های خودکار",
            Icons.Filled.Repeat,
            onRecurringClick
        ),
        HubEntry(
            "Settings", "Theme, currency, app lock",
            "تنظیمات", "ظاهر، واحد پول، قفل برنامه",
            Icons.Filled.Settings,
            onSettingsClick
        )
    )
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(if (fa) "بیشتر" else "More", style = MaterialTheme.typography.headlineSmall)
        entries.forEach { entry ->
            ListItem(
                headlineContent = { Text(if (fa) entry.titleFa else entry.titleEn) },
                supportingContent = { Text(if (fa) entry.subtitleFa else entry.subtitleEn) },
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
