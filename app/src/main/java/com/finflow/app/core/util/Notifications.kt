package com.finflow.app.core.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

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
        limit: Double
    ) {
        runCatching {
            val notification = NotificationCompat.Builder(context, CHANNEL_BUDGETS)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle("Over budget: $categoryName")
                .setContentText(
                    "Spent ${CurrencyUtils.format(spent, "IRR")} " +
                        "of ${CurrencyUtils.format(limit, "IRR")} cap"
                )
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build()
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        }
    }
}
