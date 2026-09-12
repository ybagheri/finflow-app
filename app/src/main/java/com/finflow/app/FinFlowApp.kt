package com.finflow.app

import android.app.Application
import androidx.glance.appwidget.GlanceAppWidgetManager
import com.finflow.app.core.util.Notifications
import com.finflow.app.data.work.RecurringScheduler
import com.finflow.app.presentation.widget.BalanceWidget
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Application entry point for FinFlow.
 *
 * Annotated with [HiltAndroidApp] to trigger Hilt code generation
 * and provide the app-level dependency container. Phase 4 wires
 * notification channels + the recurring-transaction worker here so
 * both survive process restarts without any Activity being open.
 * Phase 5 refreshes the Glance widget on every launch so the
 * balance snapshot never goes stale.
 */
@HiltAndroidApp
class FinFlowApp : Application() {
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        Notifications.createChannels(this)
        runCatching { RecurringScheduler.schedule(this) }
        appScope.launch {
            runCatching {
                val manager = GlanceAppWidgetManager(this@FinFlowApp)
                val widget = BalanceWidget()
                manager.getGlanceIds(BalanceWidget::class.java).forEach { id ->
                    widget.update(this@FinFlowApp, id)
                }
            }
        }
    }
}
