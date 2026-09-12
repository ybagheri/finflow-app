package com.finflow.app.presentation.screens.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finflow.app.core.util.DateUtils
import com.finflow.app.core.util.MonthPoint
import com.finflow.app.core.util.PeriodSummary
import com.finflow.app.core.util.ReportUtils
import com.finflow.app.domain.model.Category
import com.finflow.app.domain.model.Transaction
import com.finflow.app.domain.model.TransactionSort
import com.finflow.app.domain.model.TransactionSortField
import com.finflow.app.domain.model.TransactionType
import com.finflow.app.domain.repository.CategoryRepository
import com.finflow.app.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/** Month vs year scope for the reports screen. */
enum class ReportPeriod { MONTH, YEAR }

/** Everything the reports screen needs, derived from two Room streams. */
data class ReportUiState(
    val period: ReportPeriod = ReportPeriod.MONTH,
    val month: YearMonth = YearMonth.now(),
    val year: Int = LocalDate.now().year,
    val pieType: TransactionType = TransactionType.EXPENSE,
    val summary: PeriodSummary = PeriodSummary(0.0, 0.0, 0),
    val previousSummary: PeriodSummary = PeriodSummary(0.0, 0.0, 0),
    val categoryTotals: List<com.finflow.app.core.util.CategoryTotal> = emptyList(),
    val trend: List<MonthPoint> = emptyList(),
    val periodTransactions: List<Transaction> = emptyList(),
    val categories: List<Category> = emptyList()
)

/**
 * Phase 3 reports view-model: monthly/yearly summary, category totals,
 * net balance, top categories and trailing-month trends.
 */
@HiltViewModel
class ReportsViewModel @Inject constructor(
    transactions: TransactionRepository,
    categories: CategoryRepository
) : ViewModel() {

    private val _period = MutableStateFlow(ReportPeriod.MONTH)
    val period: StateFlow<ReportPeriod> = _period.asStateFlow()

    private val _month = MutableStateFlow(YearMonth.now())
    val month: StateFlow<YearMonth> = _month.asStateFlow()

    private val _year = MutableStateFlow(LocalDate.now().year)
    val year: StateFlow<Int> = _year.asStateFlow()

    private val _pieType = MutableStateFlow(TransactionType.EXPENSE)
    val pieType: StateFlow<TransactionType> = _pieType.asStateFlow()

    private val allTransactions: StateFlow<List<Transaction>> =
        transactions.observeTransactions(
            TransactionSort(TransactionSortField.DATE, ascending = false)
        ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val allCategories: StateFlow<List<Category>> = categories.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private data class ReportFilters(
        val period: ReportPeriod,
        val month: YearMonth,
        val year: Int,
        val pieType: TransactionType
    )

    private val filters: StateFlow<ReportFilters> =
        combine(_period, _month, _year, _pieType) { period, month, year, pieType ->
            ReportFilters(period, month, year, pieType)
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            ReportFilters(ReportPeriod.MONTH, YearMonth.now(), LocalDate.now().year, TransactionType.EXPENSE)
        )

    val uiState: StateFlow<ReportUiState> =
        combine(allTransactions, allCategories, filters) {
            txs, cats, f ->
            val (period, month, year, pieType) = f
            val inScope = when (period) {
                ReportPeriod.MONTH -> ReportUtils.inMonth(txs, month)
                ReportPeriod.YEAR -> ReportUtils.inYear(txs, year)
            }
            val previous = when (period) {
                ReportPeriod.MONTH -> ReportUtils.inMonth(txs, month.minusMonths(1))
                ReportPeriod.YEAR -> ReportUtils.inYear(txs, year - 1)
            }
            val anchorMonth = when (period) {
                ReportPeriod.MONTH -> month
                ReportPeriod.YEAR -> YearMonth.of(year, 12)
            }
            ReportUiState(
                period = period,
                month = month,
                year = year,
                pieType = pieType,
                summary = ReportUtils.summarize(inScope),
                previousSummary = ReportUtils.summarize(previous),
                categoryTotals = ReportUtils.categoryTotals(inScope, pieType),
                trend = ReportUtils.monthlySeries(txs, anchorMonth, months = 6),
                periodTransactions = inScope,
                categories = cats
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ReportUiState())

    fun setPeriod(value: ReportPeriod) { _period.value = value }
    fun setPieType(value: TransactionType) { _pieType.value = value }
    fun shiftMonth(delta: Long) { _month.value = _month.value.plusMonths(delta) }
    fun shiftYear(delta: Long) { _year.value = _year.value + delta.toInt() }
    fun goToToday() {
        _month.value = YearMonth.now()
        _year.value = LocalDate.now().year
    }

    /** Title used for exports, e.g. "FinFlow report — September 2026". */
    fun exportTitle(state: ReportUiState): String = when (state.period) {
        ReportPeriod.MONTH -> "FinFlow report — ${DateUtils.formatEpochDay(state.month.atDay(1).toEpochDay(), "MMMM yyyy")}"
        ReportPeriod.YEAR -> "FinFlow report — ${state.year}"
    }
}
