package com.finflow.app

import android.content.Context
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.finflow.app.core.util.LocalAppLanguage
import com.finflow.app.core.util.LocaleHelper
import com.finflow.app.data.prefs.UserPreferences
import com.finflow.app.presentation.navigation.FinFlowNavGraph
import com.finflow.app.presentation.navigation.Routes
import com.finflow.app.presentation.theme.FinFlowTheme
import com.finflow.app.presentation.theme.ThemeMode
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Single-activity host for the whole app (Jetpack Compose + Navigation Compose).
 *
 * Phase 5: theme + onboarding flag come from DataStore (with a blank splash
 * while loading), and an optional biometric gate guards the content when
 * the user enables the app lock in settings.
 */
@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject
    lateinit var prefs: UserPreferences

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeName by produceState(initialValue = "SYSTEM") {
                prefs.themeMode.collect { value = it }
            }
            val dynamicColor by produceState(initialValue = true) {
                prefs.dynamicColor.collect { value = it }
            }
            val onboarded by produceState<Boolean?>(initialValue = null) {
                prefs.onboardingDone.collect { value = it }
            }
            val lockEnabled by produceState(initialValue = false) {
                prefs.biometricLock.collect { value = it }
            }
            val appLanguage by produceState(initialValue = LocaleHelper.getPersisted(this@MainActivity) ?: "en") {
                prefs.appLanguage.collect { value = it ?: "en" }
            }
            val mode = runCatching { ThemeMode.valueOf(themeName) }
                .getOrDefault(ThemeMode.SYSTEM)

            CompositionLocalProvider(LocalAppLanguage provides appLanguage) {
                FinFlowTheme(mode = mode, dynamicColor = dynamicColor) {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        if (onboarded == null) {
                            // Splash while DataStore loads; avoids a wrong start destination.
                        } else if (lockEnabled) {
                            var unlocked by remember { mutableStateOf(false) }
                            var failed by remember { mutableStateOf(false) }
                            LaunchedEffect(Unit) {
                                authenticate(
                                    onSuccess = { unlocked = true },
                                    onError = { failed = true }
                                )
                            }
                            if (unlocked) {
                                FinFlowNavGraph(
                                    startDestination = if (onboarded == true) Routes.HOME else Routes.ONBOARDING
                                )
                            } else {
                                Column(
                                    Modifier.fillMaxSize().padding(24.dp),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        if (failed) "Authentication needed" else "FinFlow is locked",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Spacer(Modifier.height(12.dp))
                                    Button(onClick = {
                                        failed = false
                                        authenticate(
                                            onSuccess = { unlocked = true },
                                            onError = { failed = true }
                                        )
                                    }) { Text("Unlock") }
                                }
                            }
                        } else {
                            FinFlowNavGraph(
                                startDestination = if (onboarded == true) Routes.HOME else Routes.ONBOARDING
                            )
                        }
                    }
                }
            }
        }
    }

    /**
     * Prompts for biometrics; devices without enrolled biometrics (or with
     * the lock disabled) pass through so the app can never brick itself.
     */
    private fun authenticate(onSuccess: () -> Unit, onError: () -> Unit) {
        if (BiometricManager.from(this).canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG
            ) != BiometricManager.BIOMETRIC_SUCCESS
        ) {
            onSuccess()
            return
        }
        val prompt = BiometricPrompt(
            this,
            ContextCompat.getMainExecutor(this),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    onError()
                }
            }
        )
        prompt.authenticate(
            BiometricPrompt.PromptInfo.Builder()
                .setTitle("Unlock FinFlow")
                .setSubtitle("Confirm it's you to open your finances")
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                .build()
        )
    }
}
