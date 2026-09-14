package com.finflow.app.presentation.screens.recurring

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
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.finflow.app.core.util.CurrencyUtils
import com.finflow.app.core.util.DateUtils
import com.finflow.app.core.util.LANGUAGE_PERSIAN
import com.finflow.app.core.util.LocalAppLanguage
import com.finflow.app.data.work.RecurringScheduler
import com.finflow.app.domain.model.RecurrenceInterval
import com.finflow.app.domain.model.RecurringRule
import com.finflow.app.domain.model.TransactionType
import com.finflow.app.presentation.components.EmptyState
import com.finflow.app.presentation.components.JalaliDatePickerDialog
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

private fun intervalLabel(interval: RecurrenceInterval, fa: Boolean): String =
    if (fa) when (interval) {
        RecurrenceInterval.DAILY -> "روزانه"
        RecurrenceInterval.WEEKLY -> "هفتگی"
        RecurrenceInterval.MONTHLY -> "ماهانه"
    } else interval.name.lowercase().replaceFirstChar { it.uppercase() }

private fun typeLabel(type: TransactionType, fa: Boolean): String =
    if (fa) {
        if (type == TransactionType.INCOME) "درآمد" else "هزینه"
    } else {
        type.name.lowercase().replaceFirstChar { it.uppercase() }
    }

/**
 * Phase 4 recurring rules: list with next-due dates, active toggles,
 * create/edit dialog and a "Run now" manual materialization button
 * (the daily worker otherwise handles this in the background).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringScreen(viewModel: RecurringViewModel = hiltViewModel()) {
    val rules by viewModel.rules.collectAsState()
    val language = LocalAppLanguage.current
    val categories by viewModel.categories.collectAsState()
    val categoryById = categories.associateBy { it.id }
    var editing by remember { mutableStateOf<RecurringRule?>(null) }
    var creating by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf<RecurringRule?>(null) }
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    val fa = language == LANGUAGE_PERSIAN

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        floatingActionButton = {
            FloatingActionButton(onClick = { creating = true }) {
                Icon(Icons.Filled.Add, contentDescription = if (fa) "قانون جدید" else "New rule")
            }
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(if (fa) "تکرارشونده" else "Recurring", style = MaterialTheme.typography.headlineSmall)
                OutlinedButton(onClick = { RecurringScheduler.runOnce(context) }) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null)
                    Text(if (fa) "اجرا کن" else "Run now")
                }
            }
            Text(
                if (fa) "قوانین به‌صورت خودکار و روزانه تراکنش می‌سازند." else "Rules spawn transactions daily in the background.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (rules.isEmpty()) {
                EmptyState(
                    title = if (fa) "قانون تکرارشونده‌ای نیست" else "No recurring rules",
                    subtitle = if (fa) "اجاره، حقوق یا اشتراک‌ها را خودکار کن." else "Automate rent, salary or subscriptions.",
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(rules, key = { it.id }) { rule ->
                        val catName = categoryById[rule.categoryId]?.name ?: rule.type.name
                        val nextDue = remember(rule, language) {
                            RecurringScheduler.nextDueAfter(rule)?.let {
                                DateUtils.formatForDisplay(it.toEpochDay(), language)
                            } ?: (if (fa) "پایان یافته" else "ended")
                        }
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        rule.note.ifBlank { catName },
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    Switch(
                                        checked = rule.isActive,
                                        onCheckedChange = { viewModel.setActive(rule, it) }
                                    )
                                }
                                Text(
                                    "${CurrencyUtils.format(rule.amount, rule.currencyCode)} • " +
                                        intervalLabel(rule.interval, fa) +
                                        " • $catName",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "${if (fa) "بعدی" else "Next"}: $nextDue",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Row {
                                        IconButton(onClick = { editing = rule }) {
                                            Icon(Icons.Filled.Edit, contentDescription = if (fa) "ویرایش" else "Edit")
                                        }
                                        IconButton(onClick = { deleting = rule }) {
                                            Icon(
                                                Icons.Filled.Delete,
                                                contentDescription = if (fa) "حذف" else "Delete",
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (creating || editing != null) {
        RuleDialog(
            existing = editing,
            categories = categories,
            onDismiss = { creating = false; editing = null },
            onSave = { amount, type, categoryId, interval, start, end, note, method, onError ->
                viewModel.saveRule(editing, amount, type, categoryId, interval, start, end, note, method,
                    onDone = { creating = false; editing = null }, onError = onError)
            }
        )
    }
    deleting?.let { rule ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text(if (fa) "این قانون حذف شود؟" else "Delete this rule?") },
            text = {
                Text(
                    if (fa) "تراکنش‌های قبلاً ساخته‌شده باقی می‌مانند؛ تراکنش جدیدی ساخته نمی‌شود." else "Already-created transactions stay; no new ones will spawn."
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.delete(rule); deleting = null }) {
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
private fun RuleDialog(
    existing: RecurringRule?,
    categories: List<com.finflow.app.domain.model.Category>,
    onDismiss: () -> Unit,
    onSave: (
        amount: Double, type: TransactionType, categoryId: Long,
        interval: RecurrenceInterval, start: Long, end: Long?,
        note: String, method: String?, onError: (String) -> Unit
    ) -> Unit
) {
    var amount by remember { mutableStateOf(existing?.amount?.toString().orEmpty()) }
    val language = LocalAppLanguage.current
    var type by remember { mutableStateOf(existing?.type ?: TransactionType.EXPENSE) }
    var categoryId by remember { mutableStateOf(existing?.categoryId ?: 0L) }
    var interval by remember { mutableStateOf(existing?.interval ?: RecurrenceInterval.MONTHLY) }
    var start by remember { mutableStateOf(existing?.startEpochDay ?: DateUtils.todayEpochDay()) }
    var end by remember { mutableStateOf(existing?.endEpochDay) }
    var note by remember { mutableStateOf(existing?.note.orEmpty()) }
    var error by remember { mutableStateOf<String?>(null) }
    var categoryMenu by remember { mutableStateOf(false) }
    var picking by remember { mutableStateOf<String?>(null) } // "start" | "end" | null
    val fa = language == LANGUAGE_PERSIAN

    val typeCategories = remember(categories, type) { categories.filter { it.type == type } }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) (if (fa) "قانون جدید" else "New rule") else (if (fa) "ویرایش قانون" else "Edit rule")) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it; error = null },
                    label = { Text(if (fa) "مبلغ" else "Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TransactionType.entries.forEach { t ->
                        FilterChip(
                            selected = type == t,
                            onClick = { type = t; categoryId = 0L },
                            label = { Text(typeLabel(t, fa)) }
                        )
                    }
                }
                OutlinedButton(
                    onClick = { categoryMenu = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(typeCategories.firstOrNull { it.id == categoryId }?.name ?: (if (fa) "انتخاب دسته" else "Select category"))
                }
                DropdownMenu(expanded = categoryMenu, onDismissRequest = { categoryMenu = false }) {
                    typeCategories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat.name) },
                            onClick = { categoryId = cat.id; categoryMenu = false }
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RecurrenceInterval.entries.forEach { iv ->
                        FilterChip(
                            selected = interval == iv,
                            onClick = { interval = iv },
                            label = { Text(intervalLabel(iv, fa)) }
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { picking = "start" },
                        modifier = Modifier.weight(1f)
                    ) { Text("${if (fa) "از" else "From"} ${DateUtils.formatForDisplay(start, language)}") }
                    OutlinedButton(
                        onClick = { picking = "end" },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(end?.let { DateUtils.formatForDisplay(it, language) } ?: (if (fa) "بدون پایان" else "No end"))
                    }
                }
                if (end != null) {
                    TextButton(onClick = { end = null }) { Text(if (fa) "پاک کردن تاریخ پایان" else "Clear end date") }
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
                val parsed = amount.trim().toDoubleOrNull()
                if (parsed == null || parsed <= 0) {
                    error = if (fa) "مبلغی بزرگ‌تر از صفر وارد کن" else "Enter an amount greater than 0"
                } else {
                    onSave(parsed, type, categoryId, interval, start, end, note, null) {
                        error = it
                    }
                }
            }) { Text(if (fa) "ذخیره" else "Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(if (fa) "لغو" else "Cancel") }
        }
    )
    if (picking != null) {
        val pickingKey = picking
        if (fa) {
            JalaliDatePickerDialog(
                initialEpochDay = if (pickingKey == "start") start else (end ?: DateUtils.todayEpochDay()),
                onConfirm = { day ->
                    if (pickingKey == "start") start = day else end = day
                    picking = null
                },
                onDismiss = { picking = null }
            )
        } else {
            val initial = (if (pickingKey == "start") start else (end ?: DateUtils.todayEpochDay()))
                .let { LocalDate.ofEpochDay(it) }
            val pickerState = rememberDatePickerState(
                initialSelectedDateMillis = initial.atStartOfDay(ZoneId.systemDefault())
                    .toInstant().toEpochMilli()
            )
            DatePickerDialog(
                onDismissRequest = { picking = null },
                confirmButton = {
                    TextButton(onClick = {
                        pickerState.selectedDateMillis?.let { millis ->
                            val day = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault()).toLocalDate().toEpochDay()
                            if (pickingKey == "start") start = day else end = day
                        }
                        picking = null
                    }) { Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = { picking = null }) { Text("Cancel") }
                }
            ) {
                DatePicker(state = pickerState)
            }
        }
    }
}
