package com.finflow.app.core.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

/**
 * Phase 4 budget-overspend alerts. Channels are created once from
 * [FinFlowApp.onCreate]; alerts fire at most once per budget+month
 * (dedupe lives in [com.finflow.app.data.prefs.UserPreferences]).
 */
object Notifications {
    const val CHANNEL_BUDGETS = "finflow_budgets"

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_BUDGETS,
            "Budget alerts",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply { description = "Fires when spending passes a monthly category cap" }
        context.getSystemService(NotificationManager::class.java)
            ?.createNotificationChannel(channel)
    }

    /** Posts an overspend alert; silently drops when notifications are disabled. */
    fun notifyOverspend(
        context: Context,
        notificationId: Int,
        categoryName: String,
        spent: Double,
        limit: Double,
        currencyCode: String = "IRR",
        ratesToIrr: Map<String, Double> = emptyMap()
    ) {
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return@runCatching
            }
            // Totals are stored in IRR; convert once for display (Phase 5/6 currency).
            val shownSpent = CurrencyUtils.convertWithRates(spent, "IRR", currencyCode, ratesToIrr)
            val shownLimit = CurrencyUtils.convertWithRates(limit, "IRR", currencyCode, ratesToIrr)
            val notification = NotificationCompat.Builder(context, CHANNEL_BUDGETS)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle("Over budget: $categoryName")
                .setContentText(
                    "Spent ${CurrencyUtils.format(shownSpent, currencyCode)} " +
                        "of ${CurrencyUtils.format(shownLimit, currencyCode)} cap"
                )
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build()
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        }
    }
}
