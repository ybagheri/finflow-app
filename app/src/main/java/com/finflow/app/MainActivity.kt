package com.finflow.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.finflow.app.presentation.navigation.FinFlowNavGraph
import com.finflow.app.presentation.theme.FinFlowTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Single-activity host for the whole app (Jetpack Compose + Navigation Compose).
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FinFlowTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    FinFlowNavGraph()
                }
            }
        }
    }
}
