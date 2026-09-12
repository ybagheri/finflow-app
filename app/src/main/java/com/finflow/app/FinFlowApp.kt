package com.finflow.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application entry point for FinFlow.
 *
 * Annotated with [HiltAndroidApp] to trigger Hilt code generation
 * and provide the app-level dependency container.
 */
@HiltAndroidApp
class FinFlowApp : Application()
