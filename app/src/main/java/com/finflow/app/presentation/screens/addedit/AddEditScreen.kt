package com.finflow.app.presentation.screens.addedit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.finflow.app.core.util.DateUtils
import com.finflow.app.core.util.LocalAppLanguage
import com.finflow.app.domain.model.TransactionType
import java.time.LocalDate
import kotlinx.coroutines.launch

/**
 * Phase 2 add/edit: type toggle, category picker, date picker,
 * amount validation, payment method.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditScreen(
    onDone: () -> Unit,
    viewModel: AddEditViewModel = hiltViewModel()
) {
    val type by viewModel.type.collectAsState()
    val amount by viewModel.amount.collectAsState()
    val note by viewModel.note.collectAsState()
    val categoryId by viewModel.categoryId.collectAsState()
    val dateEpochDay by viewModel.dateEpochDay.collectAsState()
    val paymentMethod by viewModel.paymentMethod.collectAsState()
    val error by viewModel.error.collectAsState()
    val saving by viewModel.saving.collectAsState()
    val categories by viewModel.availableCategories.collectAsState()
    val scope = rememberCoroutineScope()
    val language = LocalAppLanguage.current
    var showDatePicker by remember { mutableStateOf(false) }
    var categoryMenu by remember { mutableStateOf(false) }
    var paymentMenu by remember { mutableStateOf(false) }

    val selectedCategory = categories.firstOrNull { it.id == categoryId } ?: categories.firstOrNull()

    Column(
        Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            if (viewModel.isEditing) "Edit transaction" else "Add transaction",
            style = MaterialTheme.typography.headlineSmall
        )
        // Type toggle
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TransactionType.entries.forEach { t ->
                FilterChip(
                    selected = type == t,
                    onClick = { viewModel.onTypeChange(t) },
                    label = { Text(t.name.lowercase().replaceFirstChar { it.uppercase() }) }
                )
            }
        }
        OutlinedTextField(
            value = amount,
            onValueChange = viewModel::onAmountChange,
            label = { Text("Amount") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            isError = error != null,
            supportingText = error?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        // Category picker (DropdownMenu for max API stability)
        OutlinedButton(
            onClick = { categoryMenu = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(selectedCategory?.name ?: "Select category")
        }
        DropdownMenu(expanded = categoryMenu, onDismissRequest = { categoryMenu = false }) {
            if (categories.isEmpty()) {
                DropdownMenuItem(text = { Text("No categories") }, onClick = { categoryMenu = false })
            }
            categories.forEach { cat ->
                DropdownMenuItem(
                    text = { Text(cat.name) },
                    onClick = {
                        viewModel.onCategoryChange(cat.id)
                        categoryMenu = false
                    }
                )
            }
        }
        // Date picker
        OutlinedButton(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth()) {
            Text(DateUtils.formatForDisplay(dateEpochDay, language))
        }
        if (showDatePicker) {
            val pickerState = rememberDatePickerState(
                initialSelectedDateMillis = LocalDate.ofEpochDay(dateEpochDay)
                    .atStartOfDay(java.time.ZoneId.systemDefault())
                    .toInstant().toEpochMilli()
            )
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        pickerState.selectedDateMillis?.let { millis ->
                            val epochDay = java.time.Instant.ofEpochMilli(millis)
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalDate().toEpochDay()
                            viewModel.onDateChange(epochDay)
                        }
                        showDatePicker = false
                    }) { Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
                }
            ) {
                DatePicker(state = pickerState)
            }
        }
        OutlinedTextField(
            value = note,
            onValueChange = viewModel::onNoteChange,
            label = { Text("Note (optional)") },
            modifier = Modifier.fillMaxWidth()
        )
        // Payment method picker
        OutlinedButton(
            onClick = { paymentMenu = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(paymentMethod.ifBlank { "Payment method (optional)" })
        }
        DropdownMenu(expanded = paymentMenu, onDismissRequest = { paymentMenu = false }) {
            DropdownMenuItem(
                text = { Text("None") },
                onClick = { viewModel.onPaymentMethodChange(""); paymentMenu = false }
            )
            PAYMENT_METHODS.forEach { method ->
                DropdownMenuItem(
                    text = { Text(method) },
                    onClick = { viewModel.onPaymentMethodChange(method); paymentMenu = false }
                )
            }
        }
        OutlinedTextField(
            value = paymentMethod,
            onValueChange = viewModel::onPaymentMethodChange,
            label = { Text("Or custom payment method") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(Modifier.height(4.dp))
        Button(
            onClick = {
                scope.launch {
                    if (viewModel.save()) onDone()
                }
            },
            enabled = !saving,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (saving) "Saving…" else "Save")
        }
    }
}
