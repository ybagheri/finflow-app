package com.finflow.app.presentation.screens.addedit

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * Phase 1 add/edit skeleton: amount + note with save wired to Room.
 * Category pickers, date pickers, type toggle and validation land in Phase 2.
 */
@Composable
fun AddEditScreen(
    onDone: () -> Unit,
    viewModel: AddEditViewModel = hiltViewModel()
) {
    val amount by viewModel.amount.collectAsState()
    val note by viewModel.note.collectAsState()

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Add transaction", style = MaterialTheme.typography.headlineSmall)
        OutlinedTextField(
            value = amount,
            onValueChange = viewModel::onAmountChange,
            label = { Text("Amount") }
        )
        OutlinedTextField(
            value = note,
            onValueChange = viewModel::onNoteChange,
            label = { Text("Note") }
        )
        Button(onClick = {
            viewModel.save()
            onDone()
        }) {
            Text("Save")
        }
    }
}
