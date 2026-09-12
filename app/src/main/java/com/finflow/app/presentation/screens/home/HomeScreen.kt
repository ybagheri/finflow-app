package com.finflow.app.presentation.screens.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.finflow.app.core.util.CurrencyUtils
import com.finflow.app.presentation.components.EmptyState
import com.finflow.app.presentation.components.TransactionRow

/**
 * Phase 2 home: balance card fed by Room totals plus a recent-transactions
 * preview. Charts teaser and insights land in Phases 3-4.
 */
@Composable
fun HomeScreen(
    onAddClick: () -> Unit,
    onSeeAllClick: (() -> Unit)? = null,
    onEditClick: ((Long) -> Unit)? = null,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val balance by viewModel.balance.collectAsState()
    val income by viewModel.incomeTotal.collectAsState()
    val expense by viewModel.expenseTotal.collectAsState()
    val recent by viewModel.recentTransactions.collectAsState()
    val categories by viewModel.categoryList.collectAsState()
    val categoryById = categories.associateBy { it.id }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(Modifier.padding(20.dp)) {
                Text("Total balance", style = MaterialTheme.typography.labelLarge)
                Text(
                    CurrencyUtils.format(balance, "IRR"),
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Income: ${CurrencyUtils.format(income, "IRR")}")
                    Text("Expense: ${CurrencyUtils.format(expense, "IRR")}")
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Recent activity", style = MaterialTheme.typography.titleMedium)
            if (recent.isNotEmpty() && onSeeAllClick != null) {
                TextButton(onClick = onSeeAllClick) { Text("See all") }
            }
        }
        if (recent.isEmpty()) {
            EmptyState(
                title = "No transactions yet",
                subtitle = "Tap + to add your first income or expense.",
                modifier = Modifier.fillMaxWidth().weight(1f)
            )
        } else {
            Card(modifier = Modifier.fillMaxWidth()) {
                LazyColumn {
                    items(recent, key = { it.id }) { tx ->
                        TransactionRow(
                            transaction = tx,
                            category = categoryById[tx.categoryId],
                            modifier = Modifier.clickable(enabled = onEditClick != null) {
                                onEditClick?.invoke(tx.id)
                            }
                        )
                        if (tx.id != recent.last().id) {
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
        if (recent.isEmpty()) {
            Button(onClick = onAddClick, modifier = Modifier.fillMaxWidth()) {
                Text("Add transaction")
            }
        }
    }
}
