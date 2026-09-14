package com.finflow.app.presentation.screens.goals

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.finflow.app.core.util.CurrencyUtils
import com.finflow.app.core.util.DateUtils
import com.finflow.app.core.util.LANGUAGE_PERSIAN
import com.finflow.app.core.util.LocalAppLanguage
import com.finflow.app.domain.model.Goal
import com.finflow.app.presentation.components.EmptyState
import com.finflow.app.presentation.components.JalaliDatePickerDialog
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Phase 4 goals: list with progress bars, create/edit dialog,
 * deposit dialog and delete confirmation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(viewModel: GoalsViewModel = hiltViewModel()) {
    val goals by viewModel.goals.collectAsState()
    val language = LocalAppLanguage.current
    var editing by remember { mutableStateOf<Goal?>(null) }
    var creating by remember { mutableStateOf(false) }
    var depositing by remember { mutableStateOf<Goal?>(null) }
    var deleting by remember { mutableStateOf<Goal?>(null) }
    val fa = language == LANGUAGE_PERSIAN

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { creating = true }) {
                Icon(Icons.Filled.Add, contentDescription = if (fa) "هدف جدید" else "New goal")
            }
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(if (fa) "اهداف" else "Goals", style = MaterialTheme.typography.headlineSmall)
            if (goals.isEmpty()) {
                EmptyState(
                    title = if (fa) "هنوز هدفی نیست" else "No goals yet",
                    subtitle = if (fa) "یک هدف پس‌انداز بساز و واریزی‌هایت را پیگیری کن." else "Create a savings goal and track deposits towards it.",
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(goals, key = { it.id }) { goal ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(goal.title, style = MaterialTheme.typography.titleSmall)
                                    Row {
                                        IconButton(onClick = { depositing = goal }) {
                                            Icon(Icons.Filled.Savings, contentDescription = if (fa) "واریز" else "Deposit")
                                        }
                                        IconButton(onClick = { editing = goal }) {
                                            Icon(Icons.Filled.Edit, contentDescription = if (fa) "ویرایش" else "Edit")
                                        }
                                        IconButton(onClick = { deleting = goal }) {
                                            Icon(
                                                Icons.Filled.Delete,
                                                contentDescription = if (fa) "حذف" else "Delete",
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }
                                LinearProgressIndicator(
                                    progress = { goal.progress },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        "${CurrencyUtils.format(goal.savedAmount, goal.currencyCode)} / " +
                                            CurrencyUtils.format(goal.targetAmount, goal.currencyCode),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        "${(goal.progress * 100).toInt()}%",
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                                goal.deadlineEpochDay?.let { deadline ->
                                    Text(
                                        "${if (fa) "سررسید" else "Due"} ${DateUtils.formatForDisplay(deadline, language)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (creating || editing != null) {
        GoalDialog(
            existing = editing,
            onDismiss = { creating = false; editing = null },
            onSave = { title, target, deadline, note, onError ->
                viewModel.saveGoal(editing, title, target, deadline, note,
                    onDone = { creating = false; editing = null }, onError = onError)
            }
        )
    }
    depositing?.let { goal ->
        var amount by remember { mutableStateOf("") }
        var error by remember { mutableStateOf<String?>(null) }
        AlertDialog(
            onDismissRequest = { depositing = null },
            title = { Text(if (fa) "واریز به ${goal.title}" else "Deposit to ${goal.title}") },
            text = {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it; error = null },
                    label = { Text(if (fa) "مبلغ" else "Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = error != null,
                    supportingText = error?.let { { Text(it) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val parsed = amount.trim().toDoubleOrNull()
                    if (parsed == null || parsed <= 0) {
                        error = if (fa) "مبلغی بزرگ‌تر از صفر وارد کن" else "Enter an amount greater than 0"
                    } else {
                        viewModel.deposit(goal, parsed,
                            onDone = { depositing = null }, onError = { error = it })
                    }
                }) { Text(if (fa) "واریز" else "Deposit") }
            },
            dismissButton = {
                TextButton(onClick = { depositing = null }) { Text(if (fa) "لغو" else "Cancel") }
            }
        )
    }
    deleting?.let { goal ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text(if (fa) "حذف «${goal.title}»؟" else "Delete \"${goal.title}\"?") },
            text = {
                Text(
                    if (fa) "پیشرفت واریزی ${CurrencyUtils.format(goal.savedAmount, goal.currencyCode)} از بین می‌رود."
                    else "Saved progress of ${CurrencyUtils.format(goal.savedAmount, goal.currencyCode)} will be lost."
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.delete(goal); deleting = null }) {
                    Text(if (fa) "حذف" else "Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleting = null }) { Text(if (fa) "لغو" else "Cancel") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GoalDialog(
    existing: Goal?,
    onDismiss: () -> Unit,
    onSave: (title: String, target: Double, deadline: Long?, note: String, onError: (String) -> Unit) -> Unit
) {
    var title by remember { mutableStateOf(existing?.title.orEmpty()) }
    val language = LocalAppLanguage.current
    var target by remember { mutableStateOf(existing?.targetAmount?.toString().orEmpty()) }
    var note by remember { mutableStateOf(existing?.note.orEmpty()) }
    var deadline by remember { mutableStateOf(existing?.deadlineEpochDay) }
    var showPicker by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val fa = language == LANGUAGE_PERSIAN

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) (if (fa) "هدف جدید" else "New goal") else (if (fa) "ویرایش هدف" else "Edit goal")) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it; error = null },
                    label = { Text(if (fa) "عنوان" else "Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = target,
                    onValueChange = { target = it; error = null },
                    label = { Text(if (fa) "مبلغ هدف" else "Target amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedButton(
                    onClick = { showPicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(deadline?.let { DateUtils.formatForDisplay(it, language) } ?: (if (fa) "سررسید (اختیاری)" else "Deadline (optional)"))
                }
                if (deadline != null) {
                    TextButton(onClick = { deadline = null }) { Text(if (fa) "پاک کردن سررسید" else "Clear deadline") }
                }
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(if (fa) "یادداشت (اختیاری)" else "Note (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val parsed = target.trim().toDoubleOrNull()
                onSave(title, parsed ?: -1.0, deadline, note) { error = it }
            }) { Text(if (fa) "ذخیره" else "Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(if (fa) "لغو" else "Cancel") }
        }
    )
    if (showPicker) {
        if (language == LANGUAGE_PERSIAN) {
            JalaliDatePickerDialog(
                initialEpochDay = deadline ?: DateUtils.todayEpochDay(),
                onConfirm = { deadline = it; showPicker = false },
                onDismiss = { showPicker = false }
            )
        } else {
            val pickerState = rememberDatePickerState(
                initialSelectedDateMillis = (deadline?.let { LocalDate.ofEpochDay(it) } ?: LocalDate.now())
                    .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            )
            DatePickerDialog(
                onDismissRequest = { showPicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        pickerState.selectedDateMillis?.let { millis ->
                            deadline = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault()).toLocalDate().toEpochDay()
                        }
                        showPicker = false
                    }) { Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = { showPicker = false }) { Text("Cancel") }
                }
            ) {
                DatePicker(state = pickerState)
            }
        }
    }
}
