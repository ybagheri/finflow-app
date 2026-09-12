package com.finflow.app.presentation.screens.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finflow.app.data.prefs.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

/** Small view-model that only marks onboarding as complete. */
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val prefs: UserPreferences
) : ViewModel() {
    fun complete(onDone: () -> Unit) {
        viewModelScope.launch {
            prefs.setOnboardingDone(true)
            onDone()
        }
    }
}

private data class OnboardingPoint(
    val title: String,
    val subtitle: String,
    val icon: ImageVector
)

/**
 * Phase 5 first-launch onboarding: three value props + a get-started
 * button. Shown once (flag in DataStore), skippable via back.
 */
@Composable
fun OnboardingScreen(
    onDone: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val points = listOf(
        OnboardingPoint(
            "Track every rial",
            "Log income and expenses in seconds, offline-first.",
            Icons.Filled.AccountBalanceWallet
        ),
        OnboardingPoint(
            "Budgets that warn you",
            "Monthly caps per category with overspend alerts.",
            Icons.Filled.Savings
        ),
        OnboardingPoint(
            "Insights that motivate",
            "Streaks, daily averages and month-over-month movers.",
            Icons.Filled.Insights
        )
    )
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Welcome to FinFlow", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        points.forEach { point ->
            ListItem(
                headlineContent = { Text(point.title) },
                supportingContent = { Text(point.subtitle) },
                leadingContent = { Icon(point.icon, contentDescription = null) }
            )
        }
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { viewModel.complete(onDone) },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Get started") }
    }
}
