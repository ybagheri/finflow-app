package com.finflow.app.presentation.widget

import android.content.Context
import androidx.compose.ui.unit.dp
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.text.Text
import androidx.glance.action.actionStartActivity
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.room.Room
import com.finflow.app.MainActivity
import com.finflow.app.core.util.CurrencyUtils
import com.finflow.app.core.util.DateUtils
import com.finflow.app.data.local.db.FinFlowDatabase
import com.finflow.app.data.prefs.UserPreferences
import com.finflow.app.domain.model.TransactionType
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first

/** Snapshot shown by the home-screen widget (amounts stored in IRR). */
private data class WidgetSnapshot(
    val balance: Double,
    val todaySpent: Double,
    val currency: String,
    val ratesToIrr: Map<String, Double>,
    val language: String
)

/**
 * Phase 5 Glance widget: balance + today's spending with an
 * "Open FinFlow" button. Reads are best-effort (runCatching) so a
 * broken DB never crashes the launcher; content refreshes on every
 * app launch (see FinFlowApp) plus the system update interval.
 */
class BalanceWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Single

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val snapshot = loadSnapshot(context)
        val balance = CurrencyUtils.format(
            CurrencyUtils.convertWithRates(
                snapshot.balance, "IRR", snapshot.currency, snapshot.ratesToIrr
            ),
            snapshot.currency
        )
        val today = CurrencyUtils.format(
            CurrencyUtils.convertWithRates(
                snapshot.todaySpent, "IRR", snapshot.currency, snapshot.ratesToIrr
            ),
            snapshot.currency
        )
        provideContent {
            val fa = snapshot.language == "fa"
            Column(
                modifier = GlanceModifier.fillMaxSize().padding(16.dp)
            ) {
                Text("FinFlow")
                Text("${if (fa) "موجودی" else "Balance"}: $balance")
                Text("${if (fa) "امروز" else "Today"}: $today")
                Button(
                    text = if (fa) "باز کردن FinFlow" else "Open FinFlow",
                    onClick = actionStartActivity<MainActivity>()
                )
            }
        }
    }

    private suspend fun loadSnapshot(context: Context): WidgetSnapshot = runCatching {
        val appContext = context.applicationContext
        // Reuse the app's Hilt singletons (same pattern as RecurringWorker).
        val entry = EntryPointAccessors.fromApplication(
            appContext,
            WidgetPrefsEntryPoint::class.java
        )
        val currency = entry.prefs().displayCurrency.first()
        val rates = entry.prefs().ratesToIrr.first()
        val language = entry.prefs().appLanguage.first() ?: "en"
        val db = Room.databaseBuilder(appContext, FinFlowDatabase::class.java, FinFlowDatabase.NAME)
            .build()
        try {
            val dao = db.transactionDao()
            val income = dao.observeIncomeTotal().first()
            val expense = dao.observeExpenseTotal().first()
            val today = DateUtils.todayEpochDay()
            val todaySpent = dao.observeByDateDesc().first()
                .filter { it.type == TransactionType.EXPENSE.name && it.dateEpochDay == today }
                .sumOf { it.amount }
            WidgetSnapshot(income - expense, todaySpent, currency, rates, language)
        } finally {
            runCatching { db.close() }
        }
    }.getOrElse {
        WidgetSnapshot(0.0, 0.0, "IRR", CurrencyUtils.DEFAULT_RATES_TO_IRR, "en")
    }
}

/** Hilt entry point so the widget can reuse the app's DataStore singleton. */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetPrefsEntryPoint {
    fun prefs(): UserPreferences
}

/** Receiver entry point declared in the manifest. */
class BalanceWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = BalanceWidget()
}
