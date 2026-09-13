package com.finflow.app.core.util

/** App-wide UI language choice. */
object LanguageCatalog {
    data class Option(val code: String, val label: String)

    /** Label is shown in the language itself, so the user can always read it. */
    val options = listOf(
        Option(LANGUAGE_ENGLISH, "English"),
        Option(LANGUAGE_PERSIAN, "فارسی")
    )
}

/** Display-currency choice, offered at onboarding and editable later in Settings. */
object CurrencyCatalog {
    data class Option(val code: String, val labelEn: String, val labelFa: String)

    val options = listOf(
        Option("IRR", "Iranian Rial (IRR)", "ریال ایران (IRR)"),
        Option("USD", "US Dollar (USD)", "دلار آمریکا (USD)"),
        Option("EUR", "Euro (EUR)", "یورو (EUR)"),
        Option("GBP", "British Pound (GBP)", "پوند بریتانیا (GBP)")
    )

    fun label(code: String, languageCode: String): String {
        val option = options.find { it.code == code } ?: return code
        return if (languageCode == LANGUAGE_PERSIAN) option.labelFa else option.labelEn
    }
}
