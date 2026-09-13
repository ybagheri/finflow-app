package com.finflow.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.finflow.app.core.util.CurrencyUtils
import com.finflow.app.core.util.DateUtils
import com.finflow.app.core.util.LocalAppLanguage
import com.finflow.app.domain.model.Category
import com.finflow.app.domain.model.Transaction
import com.finflow.app.domain.model.TransactionType

/**
 * Shared Phase 2 transaction row used by Home preview and Transactions list.
 */
@Composable
fun TransactionRow(
    transaction: Transaction,
    category: Category?,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isIncome = transaction.type == TransactionType.INCOME
    val amountColor = if (isIncome) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
    val sign = if (isIncome) "+" else "-"
    val language = LocalAppLanguage.current
    ListItem(
        modifier = modifier,
        headlineContent = {
            Text(transaction.note.ifBlank { category?.name ?: transaction.type.name })
        },
        supportingContent = {
            Text(
                buildString {
                    append(category?.name ?: transaction.type.name)
                    append(" • ")
                    append(DateUtils.formatForDisplay(transaction.dateEpochDay, language))
                    transaction.paymentMethod?.takeIf { it.isNotBlank() }?.let {
                        append(" • ")
                        append(it)
                    }
                }
            )
        },
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        try {
                            Color(category?.colorArgb ?: 0xFF6750A4.toInt())
                        } catch (_: Exception) {
                            MaterialTheme.colorScheme.primaryContainer
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = (category?.name?.firstOrNull() ?: transaction.type.name.first())
                        .toString().uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
            }
        },
        trailingContent = {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "$sign${CurrencyUtils.format(transaction.amount, transaction.currencyCode)}",
                    style = MaterialTheme.typography.titleSmall,
                    color = amountColor
                )
            }
        }
    )
}
