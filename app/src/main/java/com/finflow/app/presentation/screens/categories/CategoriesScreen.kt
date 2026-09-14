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
import com.finflow.app.core.util.CategoryLocalization
import com.finflow.app.core.util.LANGUAGE_PERSIAN
import com.finflow.app.core.util.LocalAppLanguage
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
    val fa = LocalAppLanguage.current == LANGUAGE_PERSIAN

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
                Icon(Icons.Filled.Add, contentDescription = if (fa) "افزودن دسته" else "Add category")
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text(if (fa) "دسته‌ها" else "Categories", style = MaterialTheme.typography.headlineSmall)
            Text(
                if (fa) "دسته‌های پیش‌فرض قابل حذف نیستند. دسته‌های سفارشی کاملاً قابل ویرایش‌اند." else "Defaults cannot be deleted. Custom categories support full CRUD.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (categories.isEmpty()) {
                EmptyState(
                    title = if (fa) "دسته‌ای موجود نیست" else "No categories",
                    subtitle = if (fa) "دسته‌های پیش‌فرض در اولین اجرا ایجاد می‌شوند." else "Defaults are seeded on first launch.",
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LazyColumn {
                    items(categories, key = { it.id }) { cat ->
                        val displayName = CategoryLocalization.displayName(cat, if (fa) LANGUAGE_PERSIAN else "en")
                        ListItem(
                            headlineContent = { Text(displayName) },
                            supportingContent = {
                                val kind = if (fa) {
                                    if (cat.type == TransactionType.INCOME) "درآمد" else "هزینه"
                                } else {
                                    cat.type.name.lowercase().replaceFirstChar { it.uppercase() }
                                }
                                Text(
                                    (if (cat.isDefault) {
                                        if (fa) "پیش‌فرض • " else "Default • "
                                    } else {
                                        if (fa) "سفارشی • " else "Custom • "
                                    }) + kind
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
                                        displayName.firstOrNull()?.uppercase() ?: "?",
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
                                        Icon(Icons.Filled.Edit, contentDescription = if (fa) "ویرایش نام" else "Rename")
                                    }
                                    if (!cat.isDefault) {
                                        IconButton(onClick = { confirmDelete = cat }) {
                                            Icon(
                                                Icons.Filled.Delete,
                                                contentDescription = if (fa) "حذف" else "Delete",
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
        val displayName = CategoryLocalization.displayName(cat, if (fa) LANGUAGE_PERSIAN else "en")
        AlertDialog(
            onDismissRequest = { confirmDelete = null },
            title = { Text(if (fa) "حذف «$displayName»؟" else "Delete \"$displayName\"?") },
            text = {
                Text(
                    if (fa) "تراکنش‌های این دسته تاریخچه خود را حفظ می‌کنند اما ارتباطشان با دسته قطع می‌شود. این عمل قابل بازگشت نیست." else "Transactions in this category will keep their history but lose the link. This cannot be undone."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteCategory(cat)
                    confirmDelete = null
                }) { Text(if (fa) "حذف" else "Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = null }) { Text(if (fa) "لغو" else "Cancel") }
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
    val fa = LocalAppLanguage.current == LANGUAGE_PERSIAN
    var name by remember {
        mutableStateOf(existing?.let { CategoryLocalization.displayName(it, if (fa) LANGUAGE_PERSIAN else "en") }.orEmpty())
    }
    var type by remember { mutableStateOf(existing?.type ?: TransactionType.EXPENSE) }
    var color by remember { mutableIntStateOf(existing?.colorArgb ?: CATEGORY_COLORS[0]) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (existing == null) {
                    if (fa) "دسته جدید" else "New category"
                } else {
                    if (fa) "ویرایش دسته" else "Edit category"
                }
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(if (fa) "نام" else "Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TransactionType.entries.forEach { t ->
                        FilterChip(
                            selected = type == t,
                            onClick = { type = t },
                            label = {
                                Text(
                                    if (fa) {
                                        if (t == TransactionType.INCOME) "درآمد" else "هزینه"
                                    } else {
                                        t.name.lowercase().replaceFirstChar { it.uppercase() }
                                    }
                                )
                            }
                        )
                    }
                }
                Text(if (fa) "رنگ" else "Color", style = MaterialTheme.typography.labelLarge)
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
                        if (fa) "دسته پیش‌فرض: تغییر نام و رنگ مجاز است؛ حذف غیرفعال است." else "Default category: rename and recolor are allowed; delete is disabled.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(name, type, color) }) { Text(if (fa) "ذخیره" else "Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(if (fa) "لغو" else "Cancel") }
        }
    )
}
