package com.finflow.app.presentation.screens.reports

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.finflow.app.core.util.CategoryTotal
import com.finflow.app.core.util.LANGUAGE_PERSIAN
import com.finflow.app.core.util.LocalAppLanguage
import com.finflow.app.core.util.MonthPoint
import com.finflow.app.core.util.PersianCalendar
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.max

/**
 * Phase 3 charts, hand-rolled on Compose Canvas (a conscious alternative to
 * Vico — see ADR-002 addendum — keeping zero native chart-API risk on all
 * API levels while matching the Material 3 palette).
 */

/** Donut chart of per-category totals with a legend. */
@Composable
fun CategoryDonutChart(
    totals: List<CategoryTotal>,
    colorOf: (Long) -> Color,
    nameOf: (Long) -> String,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val fa = LocalAppLanguage.current == LANGUAGE_PERSIAN
    if (totals.isEmpty()) {
        Text(
            if (fa) "هنوز داده‌ای برای این انتخاب نیست." else "No data for this selection yet.",
            style = MaterialTheme.typography.bodyMedium,
            color = scheme.onSurfaceVariant,
            modifier = modifier
        )
        return
    }
    val grand = totals.sumOf { it.total }.takeIf { it > 0 } ?: 1.0
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Canvas(
            modifier = Modifier.size(180.dp).align(Alignment.CenterHorizontally)
        ) {
            var startAngle = -90f
            totals.forEach { slice ->
                val sweep = (slice.total / grand * 360.0).toFloat()
                drawArc(
                    color = colorOf(slice.categoryId),
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = false,
                    size = Size(size.width, size.height),
                    style = Stroke(width = 56f)
                )
                startAngle += sweep
            }
        }
        totals.take(6).forEach { slice ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Canvas(modifier = Modifier.size(12.dp)) {
                        drawCircle(colorOf(slice.categoryId))
                    }
                    Text(
                        nameOf(slice.categoryId),
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    "${(slice.total / grand * 100).toInt()}%",
                    style = MaterialTheme.typography.labelLarge,
                    color = scheme.onSurfaceVariant
                )
            }
        }
    }
}

/** Grouped income/expense bars for trailing months; labels are Compose rows. */
@Composable
fun MonthlyTrendBars(
    points: List<MonthPoint>,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val fa = LocalAppLanguage.current == LANGUAGE_PERSIAN
    val incomeColor = Color(0xFF2E7D32)
    val expenseColor = scheme.error
    val trackColor = scheme.surfaceVariant
    val peak = points.maxOfOrNull { max(it.income, it.expense) }?.takeIf { it > 0 } ?: 1.0

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Canvas(modifier = Modifier.fillMaxWidth().height(160.dp)) {
            if (points.isEmpty()) return@Canvas
            val groupWidth = size.width / points.size
            val barWidth = (groupWidth * 0.28f).coerceAtLeast(6f)
            points.forEachIndexed { index, point ->
                val cx = groupWidth * index + groupWidth / 2f
                val incomeH = (point.income / peak * size.height).toFloat()
                val expenseH = (point.expense / peak * size.height).toFloat()
                // tracks
                drawRoundRect(trackColor, topLeft = androidx.compose.ui.geometry.Offset(cx - barWidth - 2f, 0f), size = Size(barWidth, size.height))
                drawRoundRect(trackColor, topLeft = androidx.compose.ui.geometry.Offset(cx + 2f, 0f), size = Size(barWidth, size.height))
                // bars (bottom-anchored)
                drawRoundRect(incomeColor, topLeft = androidx.compose.ui.geometry.Offset(cx - barWidth - 2f, size.height - incomeH), size = Size(barWidth, incomeH))
                drawRoundRect(expenseColor, topLeft = androidx.compose.ui.geometry.Offset(cx + 2f, size.height - expenseH), size = Size(barWidth, expenseH))
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            points.forEach { point ->
                val label = if (fa) {
                    val (_, jm, _) = PersianCalendar.toJalali(
                        point.month.year, point.month.monthValue, 1
                    )
                    PersianCalendar.monthNamesFa[jm - 1].take(3)
                } else {
                    point.month.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                }
                Text(
                    label,
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                    maxLines = 1
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            LegendDot(incomeColor, if (fa) "درآمد" else "Income")
            LegendDot(expenseColor, if (fa) "هزینه" else "Expense")
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Canvas(modifier = Modifier.size(10.dp)) { drawCircle(color) }
        Text(label, style = MaterialTheme.typography.labelMedium)
    }
}
