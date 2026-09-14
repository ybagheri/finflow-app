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
import com.finflow.app.core.util.LANGUAGE_PERSIAN
import com.finflow.app.core.util.LocalAppLanguage
import com.finflow.app.presentation.components.EmptyState
import com.finflow.app.presentation.components.TransactionRow

/**
 * Phase 2 home: balance card fed by Room totals plus a recent-transactions
 * preview. Phase 4 adds the smart-insights card (daily average, streak,
 * biggest month-over-month mover).
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
    val insights by viewModel.insights.collectAsState()
    val displayCurrency by viewModel.displayCurrency.collectAsState()
    val ratesToIrr by viewModel.ratesToIrr.collectAsState()
    val categoryById = categories.associateBy { it.id }
    val fa = LocalAppLanguage.current == LANGUAGE_PERSIAN
    // Totals are stored in IRR; convert once for display (Phase 5/6 currency).
    fun shown(amount: Double): String = CurrencyUtils.format(
        CurrencyUtils.convertWithRates(amount, "IRR", displayCurrency, ratesToIrr),
        displayCurrency
    )

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(Modifier.padding(20.dp)) {
                Text(if (fa) "موجودی کل" else "Total balance", style = MaterialTheme.typography.labelLarge)
                Text(
                    shown(balance),
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("${if (fa) "درآمد" else "Income"}: ${shown(income)}")
                    Text("${if (fa) "هزینه" else "Expense"}: ${shown(expense)}")
                }
            }
        }
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(if (fa) "تحلیل‌ها" else "Insights", style = MaterialTheme.typography.titleMedium)
                Text(
                    "${if (fa) "میانگین روزانه" else "Daily average"}: ${shown(insights.dailyAverage)} (30d)",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    if (insights.streakDays > 0) {
                        if (fa) "${insights.streakDays} روز پیاپی ثبت شده" else "Logging streak: ${insights.streakDays}d in a row"
                    } else {
                        if (fa) "برای شروع یک تراکنش ثبت کن" else "Log a transaction to start a streak"
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
                val delta = insights.topDelta
                if (delta != null) {
                    val name = insights.topDeltaCategoryName ?: (if (fa) "دسته برتر" else "Top category")
                    val change = delta.percentChange
                    Text(
                        if (change == null) {
                            if (fa) "جدید در این ماه: $name" else "New this month: $name"
                        } else if (change >= 0) {
                            if (fa) "${change.toInt()}٪ بیشتر در $name نسبت به ماه قبل" else "${change.toInt()}% more on $name vs last month"
                        } else {
                            if (fa) "${(-change).toInt()}٪ کمتر در $name نسبت به ماه قبل" else "${(-change).toInt()}% less on $name vs last month"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(if (fa) "فعالیت اخیر" else "Recent activity", style = MaterialTheme.typography.titleMedium)
            if (recent.isNotEmpty() && onSeeAllClick != null) {
                TextButton(onClick = onSeeAllClick) { Text(if (fa) "مشاهده همه" else "See all") }
            }
        }
        if (recent.isEmpty()) {
            EmptyState(
                title = if (fa) "هنوز تراکنشی نیست" else "No transactions yet",
                subtitle = if (fa) "برای ثبت اولین درآمد یا هزینه، + را بزن." else "Tap + to add your first income or expense.",
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
                Text(if (fa) "افزودن تراکنش" else "Add transaction")
            }
        }
    }
}
