package com.finflow.app.presentation.screens.transactions

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.finflow.app.core.util.CurrencyUtils
import com.finflow.app.presentation.components.EmptyState

/**
 * Phase 1 transaction list skeleton: reactive Room data with
 * sorting/filtering/search fully wired in the view-model.
 * Polished rows, swipe actions and filters UI land in Phase 2.
 */
@Composable
fun TransactionsScreen(
    onAddClick: () -> Unit,
    onEditClick: (Long) -> Unit,
    viewModel: TransactionsViewModel = hiltViewModel()
) {
    val items by viewModel.transactions.collectAsState()

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Transactions", style = androidx.compose.material3.MaterialTheme.typography.headlineSmall)
        if (items.isEmpty()) {
            EmptyState(
                title = "No transactions yet",
                subtitle = "Tap + to add your first income or expense.",
                modifier = Modifier.fillMaxWidth().weight(1f)
            )
        } else {
            LazyColumn {
                items(items, key = { it.id }) { tx ->
                    ListItem(
                        headlineContent = { Text(tx.note.ifBlank { tx.type.name }) },
                        supportingContent = { Text("${tx.type.name} • ${tx.currencyCode}") },
                        trailingContent = { Text(CurrencyUtils.format(tx.amount, tx.currencyCode)) }
                    )
                }
            }
        }
    }
}
