package com.finflow.app.core.util

import com.finflow.app.domain.model.Category

/**
 * Default categories are seeded once (English names, stored in Room) and
 * can be renamed by the user, so the DB can't just hold a translated
 * string — the UI translates the *unmodified* seed names for display when
 * the app language is Persian, keyed by [Category.iconKey] since that is
 * stable even if the user later renames a default category (in which case
 * this intentionally stops translating: a user-chosen name wins).
 */
object CategoryLocalization {
    private val FA_BY_ICON_KEY = mapOf(
        "work" to "حقوق",
        "business" to "کسب‌وکار",
        "add" to "سایر درآمدها",
        "food" to "غذا",
        "transport" to "حمل‌ونقل",
        "home" to "مسکن",
        "shopping" to "خرید",
        "health" to "سلامت",
        "entertainment" to "سرگرمی",
        "remove" to "سایر هزینه‌ها"
    )

    /** Seed-default English names, keyed by iconKey — used to detect a user rename. */
    private val EN_BY_ICON_KEY = mapOf(
        "work" to "Salary",
        "business" to "Business",
        "add" to "Other Income",
        "food" to "Food",
        "transport" to "Transport",
        "home" to "Housing",
        "shopping" to "Shopping",
        "health" to "Health",
        "entertainment" to "Entertainment",
        "remove" to "Other Expense"
    )

    fun displayName(category: Category, languageCode: String): String {
        if (languageCode != LANGUAGE_PERSIAN || !category.isDefault) return category.name
        // Only translate if the name still matches the original seed value —
        // if the user renamed it, show what they typed instead.
        if (category.name != EN_BY_ICON_KEY[category.iconKey]) return category.name
        return FA_BY_ICON_KEY[category.iconKey] ?: category.name
    }
}
