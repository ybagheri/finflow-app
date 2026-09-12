package com.finflow.app

import com.finflow.app.core.util.CurrencyUtils
import org.junit.Assert.assertEquals
import org.junit.Test

/** Phase 1 smoke tests for currency math (no Android framework needed). */
class CurrencyUtilsTest {
    @Test
    fun `irr to usd uses static rate`() {
        assertEquals(1.0, CurrencyUtils.convert(42_000.0, "IRR", "USD"), 0.001)
    }

    @Test
    fun `same currency is identity`() {
        assertEquals(123.0, CurrencyUtils.convert(123.0, "IRR", "IRR"), 0.0)
    }

    @Test
    fun `custom rate drives conversion`() {
        assertEquals(2.0, CurrencyUtils.convertWithRate(100_000.0, "IRR", "USD", 50_000.0), 0.001)
        assertEquals(100_000.0, CurrencyUtils.convertWithRate(2.0, "USD", "IRR", 50_000.0), 0.001)
    }

    @Test
    fun `non-positive rate is identity`() {
        assertEquals(10.0, CurrencyUtils.convertWithRate(10.0, "IRR", "USD", 0.0), 0.0)
    }
}
