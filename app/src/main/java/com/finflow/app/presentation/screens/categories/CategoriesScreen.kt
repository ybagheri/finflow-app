package com.finflow.app.presentation.screens.categories

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.finflow.app.presentation.components.EmptyState

/** Phase 1 category list (seeded defaults); CRUD dialog lands in Phase 2. */
@Composable
fun CategoriesScreen(viewModel: CategoriesViewModel = hiltViewModel()) {
    val categories by viewModel.categories.collectAsState()
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Categories", style = MaterialTheme.typography.headlineSmall)
        if (categories.isEmpty()) {
            EmptyState(
                title = "No categories",
                subtitle = "Defaults are seeded on first launch.",
                modifier = Modifier.fillMaxSize()
            )
        } else {
            LazyColumn {
                items(categories, key = { it.id }) { cat ->
                    ListItem(
                        headlineContent = { Text(cat.name) },
                        supportingContent = { Text(cat.type.name) }
                    )
                }
            }
        }
    }
}
