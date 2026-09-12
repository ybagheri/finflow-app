package com.finflow.app.presentation.screens.reports

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.finflow.app.presentation.components.EmptyState

/**
 * Placeholder for the Phase 3 reports dashboard
 * (pie + line/bar charts, monthly/yearly summary, CSV/PDF export).
 */
@Composable
fun ReportsPlaceholderScreen() {
    Text(
        "Reports — arriving in Phase 3",
        style = MaterialTheme.typography.headlineSmall,
        modifier = Modifier.padding(16.dp)
    )
    EmptyState(
        title = "Charts & reports",
        subtitle = "Category breakdown, trends and export (CSV/PDF) land in Phase 3.",
        modifier = Modifier.fillMaxSize().padding(16.dp)
    )
}
