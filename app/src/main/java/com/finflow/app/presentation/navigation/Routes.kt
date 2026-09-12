package com.finflow.app.presentation.navigation

/**
 * Type-safe destination routes for Navigation Compose.
 * Phase 1 wires the skeleton; each phase fills in real screens.
 */
object Routes {
    const val HOME = "home"
    const val TRANSACTIONS = "transactions"
    const val ADD_EDIT = "add_edit?id={id}&type={type}"
    const val CATEGORIES = "categories"
    const val REPORTS = "reports"
    const val SETTINGS = "settings"
    const val MORE = "more"
    const val BUDGETS = "budgets"
    const val GOALS = "goals"
    const val RECURRING = "recurring"

    fun addEdit(id: Long = 0, type: String = "EXPENSE") = "add_edit?id=$id&type=$type"
}
