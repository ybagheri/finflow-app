package com.finflow.app.presentation.screens.reports

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import com.finflow.app.core.util.DateUtils
import com.finflow.app.core.util.PeriodSummary
import com.finflow.app.domain.model.Category
import com.finflow.app.domain.model.Transaction
import java.time.LocalDate

/**
 * Phase 3 export: CSV (spreadsheet-friendly) + single-document PDF summary,
 * both delivered through the Storage Access Framework (no storage permission).
 */
object ReportExport {

    private fun escapeCsv(value: String): String {
        val needsQuotes = value.any { it == ',' || it == '"' || it == '\n' }
        return if (needsQuotes) "\"${value.replace("\"", "\"\"")}\"" else value
    }

    /** Pure CSV builder — unit-tested on the JVM. */
    fun buildCsv(
        transactions: List<Transaction>,
        categories: Map<Long, Category>
    ): String = buildString {
        appendLine("id,date,type,category,amount,currency,payment_method,note")
        transactions.sortedBy { it.dateEpochDay }.forEach { tx ->
            val date = LocalDate.ofEpochDay(tx.dateEpochDay).toString()
            val category = categories[tx.categoryId]?.name ?: tx.type.name
            appendLine(
                listOf(
                    tx.id.toString(),
                    date,
                    tx.type.name,
                    escapeCsv(category),
                    tx.amount.toString(),
                    tx.currencyCode,
                    escapeCsv(tx.paymentMethod.orEmpty()),
                    escapeCsv(tx.note)
                ).joinToString(",")
            )
        }
    }

    /** Writes a one-to-N-page PDF summary to [uri] (SAF-provided). */
    fun writePdf(
        context: Context,
        uri: Uri,
        title: String,
        summary: PeriodSummary,
        topCategories: List<Pair<String, Double>>,
        transactions: List<Transaction>
    ) {
        val document = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842
        val margin = 48f
        val titlePaint = Paint().apply { textSize = 22f; isFakeBoldText = true }
        val headerPaint = Paint().apply { textSize = 15f; isFakeBoldText = true }
        val bodyPaint = Paint().apply { textSize = 12f }
        val lineHeight = 20f

        var page = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create())
        var canvas = page.canvas
        var pageNumber = 1
        var y = margin + 10f

        fun newPage() {
            document.finishPage(page)
            pageNumber += 1
            page = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
            canvas = page.canvas
            y = margin + 10f
        }
        fun drawLine(text: String, paint: Paint = bodyPaint) {
            if (y > pageHeight - margin) newPage()
            canvas.drawText(text, margin, y, paint)
            y += lineHeight
        }

        drawLine(title, titlePaint)
        y += 6f
        drawLine("Generated ${DateUtils.formatEpochDay(DateUtils.todayEpochDay())}", bodyPaint)
        y += 10f
        drawLine("Summary", headerPaint)
        drawLine("Income: ${summary.income}")
        drawLine("Expense: ${summary.expense}")
        drawLine("Net balance: ${summary.net}")
        drawLine("Transactions: ${summary.count}")
        y += 10f
        drawLine("Top categories", headerPaint)
        if (topCategories.isEmpty()) {
            drawLine("No activity in this period.")
        } else {
            topCategories.take(10).forEach { (name, total) -> drawLine("$name — $total") }
        }
        y += 10f
        drawLine("Transactions", headerPaint)
        if (transactions.isEmpty()) {
            drawLine("No transactions in this period.")
        } else {
            transactions.sortedBy { it.dateEpochDay }.forEach { tx ->
                val date = LocalDate.ofEpochDay(tx.dateEpochDay).toString()
                val note = tx.note.ifBlank { tx.type.name }.take(32)
                drawLine("$date  ${tx.type.name}  ${tx.amount} ${tx.currencyCode}  $note")
            }
        }
        document.finishPage(page)
        context.contentResolver.openOutputStream(uri)?.use { out ->
            document.writeTo(out)
        }
        document.close()
    }
}
