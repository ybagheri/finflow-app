package com.finflow.app.presentation.screens.budgets

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.finflow.app.core.util.CategoryLocalization
import com.finflow.app.core.util.CurrencyUtils
import com.finflow.app.core.util.DateUtils
import com.finflow.app.core.util.LANGUAGE_PERSIAN
import com.finflow.app.core.util.LocalAppLanguage
import java.time.format.TextStyle
import java.util.Locale

/**
 * Phase 4 budgets: monthly caps with progress bars; overspend fires a
 * system notification (one-shot per budget+month) when permitted.
 */
@Composable
fun BudgetsScreen(viewModel: BudgetsViewModel = hiltViewModel()) {
    val rows by viewModel.rows.collectAsState()
    val month by viewModel.month.collectAsState()
    val displayCurrency by viewModel.displayCurrency.collectAsState()
    val ratesToIrr by viewModel.ratesToIrr.collectAsState()
    var editing by remember { mutableStateOf<BudgetRow?>(null) }
    val context = LocalContext.current
    val language = LocalAppLanguage.current
    val fa = language == LANGUAGE_PERSIAN

    // Totals are stored in IRR; convert once for display (Phase 5/6 currency).
    fun shown(amount: Double): String = CurrencyUtils.format(
        CurrencyUtils.convertWithRates(amount, "IRR", displayCurrency, ratesToIrr),
        displayCurrency
    )

    // Ask for the notification runtime permission once (Android 13+).
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val capped = rows.count { it.budget != null }
    val overspent = rows.count { it.overspent }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(if (fa) "بودجه‌ها" else "Budgets", style = MaterialTheme.typography.headlineSmall)
        Card(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.shiftMonth(-1) }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = if (fa) "ماه قبل" else "Previous month")
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        if (fa) {
                            DateUtils.formatMonthForDisplay(month.atDay(1).toEpochDay(), language)
                        } else {
                            "${month.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${month.year}"
                        },
                        style = MaterialTheme.typography.titleMedium
                    )
                    TextButton(onClick = { viewModel.goToCurrentMonth() }) { Text(if (fa) "این ماه" else "This month") }
                }
                IconButton(onClick = { viewModel.shiftMonth(1) }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = if (fa) "ماه بعد" else "Next month")
                }
            }
        }
        Text(
            if (fa) {
                "$capped سقف تعیین‌شده" + if (overspent > 0) " • $overspent بیش از بودجه" else ""
            } else {
                "$capped cap${if (capped == 1) "" else "s"} set" +
                    if (overspent > 0) " • $overspent over budget" else ""
            },
            style = MaterialTheme.typography.labelMedium,
            color = if (overspent > 0) MaterialTheme.colorScheme.error
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(rows, key = { it.category.id }) { row ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = if (row.overspent) {
                        CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    } else CardDefaults.cardColors()
                ) {
                    Column(
                        Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                CategoryLocalization.displayName(row.category, language),
                                style = MaterialTheme.typography.titleSmall
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (row.budget == null) {
                                    IconButton(onClick = { editing = row }) {
                                        Icon(Icons.Filled.Add, contentDescription = if (fa) "تعیین سقف" else "Set cap")
                                    }
                                } else {
                                    IconButton(onClick = { viewModel.removeCap(row) }) {
                                        Icon(
                                            Icons.Filled.Delete,
                                            contentDescription = if (fa) "حذف سقف" else "Remove cap",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                        if (row.budget != null) {
                            LinearProgressIndicator(
                                progress = { (row.progress ?: 0f).coerceIn(0f, 1f) },
                                modifier = Modifier.fillMaxWidth(),
                                color = if (row.overspent) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.primary,
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "${shown(row.spent)} / " + shown(row.limit!!),
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    if (row.overspent) {
                                        if (fa) "بیش از بودجه" else "Over budget"
                                    } else {
                                        "${((row.progress ?: 0f) * 100).toInt()}%"
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (row.overspent) MaterialTheme.colorScheme.error
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            TextButton(onClick = { editing = row }) { Text(if (fa) "ویرایش سقف" else "Edit cap") }
                        } else {
                            Text(
                                "${if (fa) "خرج‌شده" else "Spent"} ${shown(row.spent)} • ${if (fa) "بدون سقف" else "no cap set"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    editing?.let { row ->
        var amount by remember(row) {
            mutableStateOf(row.budget?.limitAmount?.toString().orEmpty())
        }
        var error by remember { mutableStateOf<String?>(null) }
        AlertDialog(
            onDismissRequest = { editing = null },
            title = {
                val catName = CategoryLocalization.displayName(row.category, language)
                Text(if (fa) "سقف برای $catName" else "Cap for $catName")
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it; error = null },
                        label = { Text(if (fa) "سقف ماهانه" else "Monthly cap") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        isError = error != null,
                        supportingText = error?.let { { Text(it) } },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        "${if (fa) "تاکنون خرج شده" else "Spent so far"}: ${shown(row.spent)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val parsed = amount.trim().toDoubleOrNull()
                    if (parsed == null || parsed <= 0) {
                        error = if (fa) "مبلغی بزرگ‌تر از صفر وارد کن" else "Enter an amount greater than 0"
                    } else {
                        viewModel.setCap(row.category, parsed) { editing = null }
                    }
                }) { Text(if (fa) "ذخیره" else "Save") }
            },
            dismissButton = {
                TextButton(onClick = { editing = null }) { Text(if (fa) "لغو" else "Cancel") }
            }
        )
    }
}
