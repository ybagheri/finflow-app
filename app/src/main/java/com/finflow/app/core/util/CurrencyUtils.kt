package com.finflow.app.core.util

import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

/**
 * Currency formatting + a minimal hard-coded IRR<->USD conversion.
 * A proper rates API lands in Phase 5; Phase 1 only needs deterministic local math.
 */
object CurrencyUtils {
    /** Static demo rate: 1 USD = 42,000 IRR. User-editable in Phase 5 settings. */
    const val IRR_PER_USD = 42_000.0

    fun convert(amount: Double, from: String, to: String): Double =
        convertWithRate(amount, from, to, IRR_PER_USD)

    /** Conversion with an explicit rate (Phase 5: the rate is user-editable). */
    fun convertWithRate(amount: Double, from: String, to: String, irrPerUsd: Double): Double {
        if (from == to || irrPerUsd <= 0) return amount
        val inIrr = when (from) {
            "USD" -> amount * irrPerUsd
            else -> amount // IRR and unknown codes treated as IRR
        }
        return when (to) {
            "USD" -> inIrr / irrPerUsd
            else -> inIrr
        }
    }

    fun format(amount: Double, currencyCode: String): String = try {
        val nf = NumberFormat.getCurrencyInstance(Locale.getDefault())
        nf.currency = Currency.getInstance(currencyCode)
        nf.maximumFractionDigits = if (currencyCode == "IRR") 0 else 2
        nf.format(amount)
    } catch (_: Exception) {
        "$amount $currencyCode"
    }
}
