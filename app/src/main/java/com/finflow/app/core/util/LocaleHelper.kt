package com.finflow.app.core.util

import android.content.Context
import android.content.res.Configuration
import androidx.compose.runtime.compositionLocalOf
import java.util.Locale

/** Language codes FinFlow supports as a full app language, not just a device locale. */
const val LANGUAGE_ENGLISH = "en"
const val LANGUAGE_PERSIAN = "fa"

/**
 * Reads/writes the chosen app language *synchronously* (plain SharedPreferences)
 * so [android.app.Activity.attachBaseContext] — which runs before Hilt/DataStore
 * are usable — can apply it on every cold start, including on devices below the
 * Android 13 per-app-language API (down to minSdk 26).
 *
 * [com.finflow.app.data.prefs.UserPreferences] remains the source of truth for
 * the rest of the app (reactive Flow, used by Settings/Onboarding); this object
 * just mirrors the same value for the one place that needs it before DI is up.
 */
object LocaleHelper {
    private const val PREFS_NAME = "locale_prefs"
    private const val KEY_LANGUAGE = "app_language"

    fun persist(context: Context, languageCode: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LANGUAGE, languageCode)
            .apply()
    }

    fun getPersisted(context: Context): String? =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_LANGUAGE, null)

    /** Wraps [context] with the persisted language applied, if one was chosen. */
    fun wrap(context: Context): Context {
        val languageCode = getPersisted(context) ?: return context
        return applyLocale(context, languageCode)
    }

    fun applyLocale(context: Context, languageCode: String): Context {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        return context.createConfigurationContext(config)
    }
}

/** Current app language ("en"/"fa"), provided at the composition root in [com.finflow.app.MainActivity]. */
val LocalAppLanguage = compositionLocalOf { LANGUAGE_ENGLISH }
