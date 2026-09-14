package com.finflow.app.presentation.screens.transactions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.finflow.app.core.util.LANGUAGE_PERSIAN
import com.finflow.app.core.util.LocalAppLanguage
import com.finflow.app.domain.model.TransactionSortField
import com.finflow.app.domain.model.TransactionType
import com.finflow.app.presentation.components.EmptyState
import com.finflow.app.presentation.components.TransactionRow

private fun sortLabel(field: TransactionSortField, ascending: Boolean, fa: Boolean): String =
    if (fa) when (field) {
        TransactionSortField.DATE -> if (ascending) "قدیمی‌ترین ابتدا" else "جدیدترین ابتدا"
        TransactionSortField.AMOUNT -> if (ascending) "مبلغ ↑" else "مبلغ ↓"
        TransactionSortField.CATEGORY -> if (ascending) "دسته الف-ی" else "دسته ی-الف"
    } else when (field) {
        TransactionSortField.DATE -> if (ascending) "Oldest first" else "Newest first"
        TransactionSortField.AMOUNT -> if (ascending) "Amount ↑" else "Amount ↓"
        TransactionSortField.CATEGORY -> if (ascending) "Category A–Z" else "Category Z–A"
    }

/**
 * Phase 2 transaction list: search, type + category filters, sorting
 * and swipe-to-delete. Rows tap through to the add/edit screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    onAddClick: () -> Unit,
    onEditClick: (Long) -> Unit,
    viewModel: TransactionsViewModel = hiltViewModel()
) {
    val items by viewModel.transactions.collectAsState()
    val query by viewModel.query.collectAsState()
    val sort by viewModel.sort.collectAsState()
    val typeFilter by viewModel.typeFilter.collectAsState()
    val categoryFilter by viewModel.categoryFilter.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val categoryById = categories.associateBy { it.id }
    var sortMenu by remember { mutableStateOf(false) }
    var categoryMenu by remember { mutableStateOf(false) }
    val hasActiveFilters = query.isNotBlank() || typeFilter != null || categoryFilter != null
    val fa = LocalAppLanguage.current == LANGUAGE_PERSIAN

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                if (fa) "تراکنش‌ها" else "Transactions",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { sortMenu = true }) {
                Icon(Icons.Filled.FilterList, contentDescription = if (fa) "مرتب‌سازی" else "Sort")
            }
            DropdownMenu(expanded = sortMenu, onDismissRequest = { sortMenu = false }) {
                listOf(
                    TransactionSortField.DATE to false,
                    TransactionSortField.DATE to true,
                    TransactionSortField.AMOUNT to false,
                    TransactionSortField.AMOUNT to true,
                    TransactionSortField.CATEGORY to true,
                    TransactionSortField.CATEGORY to false
                ).forEach { (field, asc) ->
                    val selected = sort.field == field && sort.ascending == asc
                    DropdownMenuItem(
                        text = { Text((if (selected) "✓ " else "") + sortLabel(field, asc, fa)) },
                        onClick = {
                            viewModel.setSort(field, asc)
                            sortMenu = false
                        }
                    )
                }
            }
        }
        OutlinedTextField(
            value = query,
            onValueChange = viewModel::setQuery,
            label = { Text(if (fa) "جستجو در یادداشت، روش پرداخت" else "Search notes, payment method") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                FilterChip(
                    selected = typeFilter == null,
                    onClick = { viewModel.setTypeFilter(null) },
                    label = { Text(if (fa) "همه" else "All") }
                )
            }
            item {
                FilterChip(
                    selected = typeFilter == TransactionType.INCOME,
                    onClick = {
                        viewModel.setTypeFilter(
                            if (typeFilter == TransactionType.INCOME) null
                            else TransactionType.INCOME
                        )
                    },
                    label = { Text(if (fa) "درآمد" else "Income") }
                )
            }
            item {
                FilterChip(
                    selected = typeFilter == TransactionType.EXPENSE,
                    onClick = {
                        viewModel.setTypeFilter(
                            if (typeFilter == TransactionType.EXPENSE) null
                            else TransactionType.EXPENSE
                        )
                    },
                    label = { Text(if (fa) "هزینه" else "Expense") }
                )
            }
            item {
                val activeName = categoryFilter?.let { categoryById[it]?.name } ?: (if (fa) "دسته" else "Category")
                FilterChip(
                    selected = categoryFilter != null,
                    onClick = { categoryMenu = true },
                    label = { Text(activeName) }
                )
            }
            if (hasActiveFilters) {
                item {
                    FilterChip(
                        selected = false,
                        onClick = { viewModel.clearFilters() },
                        label = { Text(if (fa) "پاک کردن" else "Clear") }
                    )
                }
            }
        }
        DropdownMenu(expanded = categoryMenu, onDismissRequest = { categoryMenu = false }) {
            DropdownMenuItem(
                text = { Text(if (fa) "همه دسته‌ها" else "All categories") },
                onClick = { viewModel.setCategoryFilter(null); categoryMenu = false }
            )
            categories.forEach { cat ->
                DropdownMenuItem(
                    text = { Text(cat.name) },
                    onClick = { viewModel.setCategoryFilter(cat.id); categoryMenu = false }
                )
            }
        }
        Text(
            if (fa) {
                "${items.size} ${if (items.size == 1) "نتیجه" else "نتیجه"} • ${sortLabel(sort.field, sort.ascending, fa)}"
            } else {
                "${items.size} result${if (items.size == 1) "" else "s"} • ${sortLabel(sort.field, sort.ascending, fa)}"
            },
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (items.isEmpty()) {
            EmptyState(
                title = if (hasActiveFilters) {
                    if (fa) "نتیجه‌ای پیدا نشد" else "No matches"
                } else {
                    if (fa) "هنوز تراکنشی نیست" else "No transactions yet"
                },
                subtitle = if (hasActiveFilters) {
                    if (fa) "جستجو یا فیلترها را پاک کن." else "Try clearing search or filters."
                } else {
                    if (fa) "برای ثبت اولین درآمد یا هزینه، + را بزن." else "Tap + to add your first income or expense."
                },
                modifier = Modifier.fillMaxWidth().weight(1f)
            )
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(items, key = { it.id }) { tx ->
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { target ->
                            if (target == SwipeToDismissBoxValue.EndToStart ||
                                target == SwipeToDismissBoxValue.StartToEnd
                            ) {
                                viewModel.delete(tx.id)
                                true
                            } else false
                        }
                    )
                    SwipeToDismissBox(
                        state = dismissState,
                        backgroundContent = {
                            Row(
                                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Filled.Delete,
                                    contentDescription = if (fa) "حذف" else "Delete",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        },
                        content = {
                            TransactionRow(
                                transaction = tx,
                                category = categoryById[tx.categoryId],
                                modifier = Modifier.clickable { onEditClick(tx.id) }
                            )
                        }
                    )
                }
            }
        }
    }
}
