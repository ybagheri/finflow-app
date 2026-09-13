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

    /**
     * Static demo IRR-per-unit rates for every non-IRR currency the app offers
     * at onboarding/Settings. All user-editable (see [UserPreferences][com.finflow.app.data.prefs.UserPreferences]);
     * these are just sensible starting points, same spirit as [IRR_PER_USD].
     */
    val DEFAULT_RATES_TO_IRR: Map<String, Double> = mapOf(
        "USD" to 42_000.0,
        "EUR" to 45_500.0,
        "GBP" to 53_000.0
    )

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

    /**
     * General multi-currency conversion via a code -> IRR-per-unit rate map
     * (IRR itself is the implicit 1.0 base and never needs an entry). Falls
     * back to [amount] unchanged if either side is an unrecognized/invalid rate.
     */
    fun convertWithRates(amount: Double, from: String, to: String, ratesToIrr: Map<String, Double>): Double {
        if (from == to) return amount
        val fromRate = if (from == "IRR") 1.0 else ratesToIrr[from]
        val toRate = if (to == "IRR") 1.0 else ratesToIrr[to]
        if (fromRate == null || toRate == null || fromRate <= 0 || toRate <= 0) return amount
        return (amount * fromRate) / toRate
    }

    fun format(amount: Double, currencyCode: String): String = try {
        // Locale.US pins the numeral system to Latin digits/Western grouping
        // regardless of the app's UI language (Locale.getDefault() changes to
        // "fa" when Persian is selected, which would otherwise render Persian
        // digits here) — the currency symbol still varies by currencyCode.
        val nf = NumberFormat.getCurrencyInstance(Locale.US)
        nf.currency = Currency.getInstance(currencyCode)
        nf.maximumFractionDigits = if (currencyCode == "IRR") 0 else 2
        nf.format(amount)
    } catch (_: Exception) {
        "$amount $currencyCode"
    }
}
