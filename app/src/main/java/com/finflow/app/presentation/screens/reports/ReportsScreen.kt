package com.finflow.app.presentation.screens.reports

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.finflow.app.core.util.CategoryLocalization
import com.finflow.app.core.util.CurrencyUtils
import com.finflow.app.core.util.DateUtils
import com.finflow.app.core.util.LANGUAGE_PERSIAN
import com.finflow.app.core.util.LocalAppLanguage
import com.finflow.app.core.util.ReportUtils
import com.finflow.app.domain.model.TransactionType
import java.time.format.TextStyle
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Phase 3 reports dashboard: monthly/yearly summary, category totals,
 * net balance, top categories, donut + trend charts, CSV/PDF export.
 */
@Composable
fun ReportsScreen(viewModel: ReportsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    val period by viewModel.period.collectAsState()
    val month by viewModel.month.collectAsState()
    val year by viewModel.year.collectAsState()
    val pieType by viewModel.pieType.collectAsState()
    val displayCurrency by viewModel.displayCurrency.collectAsState()
    val ratesToIrr by viewModel.ratesToIrr.collectAsState()
    val context = LocalContext.current
    val language = LocalAppLanguage.current
    val fa = language == LANGUAGE_PERSIAN
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    val categoryById = state.categories.associateBy { it.id }
    val fallbackCategoryColor = MaterialTheme.colorScheme.primary
    val colorOf: (Long) -> Color = { id ->
        categoryById[id]?.let { Color(it.colorArgb) } ?: fallbackCategoryColor
    }
    val nameOf: (Long) -> String = { id ->
        categoryById[id]?.let { CategoryLocalization.displayName(it, language) } ?: (if (fa) "ناشناخته" else "Unknown")
    }
    // Totals are stored in IRR; convert once for display (Phase 5/6 currency).
    fun shown(amount: Double): String = CurrencyUtils.format(
        CurrencyUtils.convertWithRates(amount, "IRR", displayCurrency, ratesToIrr),
        displayCurrency
    )

    val csvLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            scope.launch(Dispatchers.IO) {
                runCatching {
                    context.contentResolver.openOutputStream(uri)?.use { out ->
                        out.write(
                            ReportExport.buildCsv(
                                state.periodTransactions,
                                categoryById
                            ).toByteArray()
                        )
                    } ?: error("Could not open file")
                }.onFailure {
                    snackbar.showSnackbar((if (fa) "خروجی CSV ناموفق بود: " else "CSV export failed: ") + it.message)
                }.onSuccess {
                    snackbar.showSnackbar(
                        if (fa) "CSV خروجی گرفته شد (${state.periodTransactions.size} ردیف)"
                        else "CSV exported (${state.periodTransactions.size} rows)"
                    )
                }
            }
        }
    }
    val pdfLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        if (uri != null) {
            scope.launch(Dispatchers.IO) {
                runCatching {
                    ReportExport.writePdf(
                        context = context,
                        uri = uri,
                        title = viewModel.exportTitle(state, language),
                        summary = state.summary,
                        topCategories = state.categoryTotals.take(10).map { nameOf(it.categoryId) to it.total },
                        transactions = state.periodTransactions,
                        languageCode = language
                    )
                }.onFailure {
                    snackbar.showSnackbar((if (fa) "خروجی PDF ناموفق بود: " else "PDF export failed: ") + it.message)
                }.onSuccess {
                    snackbar.showSnackbar(if (fa) "PDF خروجی گرفته شد" else "PDF exported")
                }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(if (fa) "گزارش‌ها" else "Reports", style = MaterialTheme.typography.headlineSmall)

        // Period scope toggle
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = period == ReportPeriod.MONTH,
                onClick = { viewModel.setPeriod(ReportPeriod.MONTH) },
                label = { Text(if (fa) "ماهانه" else "Monthly") }
            )
            FilterChip(
                selected = period == ReportPeriod.YEAR,
                onClick = { viewModel.setPeriod(ReportPeriod.YEAR) },
                label = { Text(if (fa) "سالانه" else "Yearly") }
            )
        }

        // Period navigator
        Card(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (period == ReportPeriod.MONTH) viewModel.shiftMonth(-1)
                        else viewModel.shiftYear(-1)
                    }
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = if (fa) "قبلی" else "Previous")
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        when (period) {
                            ReportPeriod.MONTH -> if (fa) {
                                DateUtils.formatMonthForDisplay(month.atDay(1).toEpochDay(), language)
                            } else {
                                "${month.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${month.year}"
                            }
                            ReportPeriod.YEAR -> year.toString()
                        },
                        style = MaterialTheme.typography.titleMedium
                    )
                    TextButton(onClick = { viewModel.goToToday() }) {
                        Text(if (fa) "امروز" else "Today")
                    }
                }
                IconButton(
                    onClick = {
                        if (period == ReportPeriod.MONTH) viewModel.shiftMonth(1)
                        else viewModel.shiftYear(1)
                    }
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = if (fa) "بعدی" else "Next")
                }
            }
        }

        // Summary card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(
                Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(if (fa) "موجودی خالص" else "Net balance", style = MaterialTheme.typography.labelLarge)
                Text(
                    shown(state.summary.net),
                    style = MaterialTheme.typography.headlineMedium
                )
                Text("${if (fa) "درآمد" else "Income"}: ${shown(state.summary.income)}")
                Text("${if (fa) "هزینه" else "Expense"}: ${shown(state.summary.expense)}")
                Text(
                    (if (fa) {
                        "${state.summary.count} تراکنش"
                    } else {
                        "${state.summary.count} transaction${if (state.summary.count == 1) "" else "s"}"
                    }) + momSuffix(state.summary.net, state.previousSummary.net, fa),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        // Donut breakdown
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(if (fa) "تفکیک هزینه‌ها" else "Spending breakdown", style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = pieType == TransactionType.EXPENSE,
                        onClick = { viewModel.setPieType(TransactionType.EXPENSE) },
                        label = { Text(if (fa) "هزینه‌ها" else "Expenses") }
                    )
                    FilterChip(
                        selected = pieType == TransactionType.INCOME,
                        onClick = { viewModel.setPieType(TransactionType.INCOME) },
                        label = { Text(if (fa) "درآمدها" else "Income") }
                    )
                }
                CategoryDonutChart(
                    totals = state.categoryTotals,
                    colorOf = colorOf,
                    nameOf = nameOf,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Trend chart
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(if (fa) "روند ۶ ماه اخیر" else "6-month trend", style = MaterialTheme.typography.titleMedium)
                MonthlyTrendBars(points = state.trend, modifier = Modifier.fillMaxWidth())
            }
        }

        // Top categories
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(if (fa) "پردسته‌ترین‌ها" else "Top categories", style = MaterialTheme.typography.titleMedium)
                if (state.categoryTotals.isEmpty()) {
                    Text(
                        if (fa) "هنوز چیزی اینجا نیست." else "Nothing here yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                state.categoryTotals.take(5).forEachIndexed { index, total ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("${index + 1}. ${nameOf(total.categoryId)} (${total.count})")
                        Text(shown(total.total))
                    }
                    if (index < minOf(4, state.categoryTotals.size - 1)) {
                        HorizontalDivider()
                    }
                }
            }
        }

        // Export
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { csvLauncher.launch("finflow-report.csv") },
                modifier = Modifier.weight(1f)
            ) { Text(if (fa) "خروجی CSV" else "Export CSV") }
            OutlinedButton(
                onClick = { pdfLauncher.launch("finflow-report.pdf") },
                modifier = Modifier.weight(1f)
            ) { Text(if (fa) "خروجی PDF" else "Export PDF") }
        }
    }
}

private fun momSuffix(current: Double, previous: Double, fa: Boolean): String {
    val change = ReportUtils.percentChange(current, previous)
        ?: return if (fa) " • جدید در این دوره" else " • new this period"
    val arrow = if (change >= 0) "▲" else "▼"
    return if (fa) {
        " • $arrow ${kotlin.math.abs(change).toInt()}٪ نسبت به دوره قبل"
    } else {
        " • $arrow ${kotlin.math.abs(change).toInt()}% vs previous"
    }
}
