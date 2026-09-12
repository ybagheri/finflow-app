package com.finflow.app

import android.app.Application
import com.finflow.app.core.util.Notifications
import com.finflow.app.data.work.RecurringScheduler
import dagger.hilt.android.HiltAndroidApp

/**
 * Application entry point for FinFlow.
 *
 * Annotated with [HiltAndroidApp] to trigger Hilt code generation
 * and provide the app-level dependency container. Phase 4 wires
 * notification channels + the recurring-transaction worker here so
 * both survive process restarts without any Activity being open.
 */
@HiltAndroidApp
class FinFlowApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Notifications.createChannels(this)
        runCatching { RecurringScheduler.schedule(this) }
    }
}
