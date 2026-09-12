package com.finflow.app.presentation.screens.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.finflow.app.domain.model.Category
import com.finflow.app.domain.model.TransactionType
import com.finflow.app.presentation.components.EmptyState

/** Phase 2 category management: create/rename/recolor/delete custom entries. */
@Composable
fun CategoriesScreen(viewModel: CategoriesViewModel = hiltViewModel()) {
    val categories by viewModel.categories.collectAsState()
    val error by viewModel.error.collectAsState()
    var editing by remember { mutableStateOf<Category?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf<Category?>(null) }
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(error) {
        if (error != null) {
            snackbar.showSnackbar(error!!)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                editing = null
                showDialog = true
            }) {
                Icon(Icons.Filled.Add, contentDescription = "Add category")
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text("Categories", style = MaterialTheme.typography.headlineSmall)
            Text(
                "Defaults cannot be deleted. Custom categories support full CRUD.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
                            supportingContent = {
                                Text(
                                    (if (cat.isDefault) "Default • " else "Custom • ") +
                                        cat.type.name.lowercase()
                                            .replaceFirstChar { it.uppercase() }
                                )
                            },
                            leadingContent = {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(cat.colorArgb)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        cat.name.firstOrNull()?.uppercase() ?: "?",
                                        color = Color.White,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                }
                            },
                            trailingContent = {
                                Row {
                                    IconButton(onClick = {
                                        editing = cat
                                        showDialog = true
                                    }) {
                                        Icon(Icons.Filled.Edit, contentDescription = "Rename")
                                    }
                                    if (!cat.isDefault) {
                                        IconButton(onClick = { confirmDelete = cat }) {
                                            Icon(
                                                Icons.Filled.Delete,
                                                contentDescription = "Delete",
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showDialog) {
        CategoryDialog(
            existing = editing,
            onDismiss = { showDialog = false },
            onSave = { name, type, color ->
                viewModel.saveCategory(editing, name, type, color) { showDialog = false }
            }
        )
    }
    confirmDelete?.let { cat ->
        AlertDialog(
            onDismissRequest = { confirmDelete = null },
            title = { Text("Delete \"${cat.name}\"?") },
            text = { Text("Transactions in this category will keep their history but lose the link. This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteCategory(cat)
                    confirmDelete = null
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun CategoryDialog(
    existing: Category?,
    onDismiss: () -> Unit,
    onSave: (name: String, type: TransactionType, color: Int) -> Unit
) {
    var name by remember { mutableStateOf(existing?.name.orEmpty()) }
    var type by remember { mutableStateOf(existing?.type ?: TransactionType.EXPENSE) }
    var color by remember { mutableIntStateOf(existing?.colorArgb ?: CATEGORY_COLORS[0]) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "New category" else "Edit category") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TransactionType.entries.forEach { t ->
                        FilterChip(
                            selected = type == t,
                            onClick = { type = t },
                            label = {
                                Text(t.name.lowercase().replaceFirstChar { it.uppercase() })
                            }
                        )
                    }
                }
                Text("Color", style = MaterialTheme.typography.labelLarge)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CATEGORY_COLORS.forEach { c ->
                        val selected = c == color
                        Box(
                            modifier = Modifier
                                .size(if (selected) 40.dp else 32.dp)
                                .clip(CircleShape)
                                .background(Color(c))
                                .clickable { color = c }
                        )
                    }
                }
                if (existing?.isDefault == true) {
                    Text(
                        "Default category: rename and recolor are allowed; delete is disabled.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(name, type, color) }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
