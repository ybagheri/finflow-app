package com.finflow.app.data.mapper

import com.finflow.app.data.local.entity.BudgetEntity
import com.finflow.app.data.local.entity.CategoryEntity
import com.finflow.app.data.local.entity.GoalEntity
import com.finflow.app.data.local.entity.RecurringRuleEntity
import com.finflow.app.data.local.entity.TransactionEntity
import com.finflow.app.domain.model.Budget
import com.finflow.app.domain.model.Category
import com.finflow.app.domain.model.Goal
import com.finflow.app.domain.model.RecurrenceInterval
import com.finflow.app.domain.model.RecurringRule
import com.finflow.app.domain.model.Transaction
import com.finflow.app.domain.model.TransactionType

/**
 * Bidirectional entity <-> domain mappings. Kept as top-level pure functions
 * so they are trivially unit-testable without Room or Hilt.
 */

fun TransactionEntity.toDomain() = Transaction(
    id = id,
    amount = amount,
    type = TransactionType.fromName(type),
    categoryId = categoryId,
    dateEpochDay = dateEpochDay,
    note = note,
    paymentMethod = paymentMethod,
    currencyCode = currencyCode,
    createdAtMillis = createdAtMillis
)

fun Transaction.toEntity() = TransactionEntity(
    id = id,
    amount = amount,
    type = type.name,
    categoryId = categoryId,
    dateEpochDay = dateEpochDay,
    note = note,
    paymentMethod = paymentMethod,
    currencyCode = currencyCode,
    createdAtMillis = createdAtMillis
)

fun CategoryEntity.toDomain() = Category(
    id = id,
    name = name,
    type = TransactionType.fromName(type),
    iconKey = iconKey,
    colorArgb = colorArgb,
    isDefault = isDefault
)

fun Category.toEntity() = CategoryEntity(
    id = id,
    name = name,
    type = type.name,
    iconKey = iconKey,
    colorArgb = colorArgb,
    isDefault = isDefault
)

fun BudgetEntity.toDomain() = Budget(
    id = id,
    categoryId = categoryId,
    limitAmount = limitAmount,
    monthKey = monthKey,
    currencyCode = currencyCode
)

fun Budget.toEntity() = BudgetEntity(
    id = id,
    categoryId = categoryId,
    limitAmount = limitAmount,
    monthKey = monthKey,
    currencyCode = currencyCode
)

fun GoalEntity.toDomain() = Goal(
    id = id,
    title = title,
    targetAmount = targetAmount,
    savedAmount = savedAmount,
    currencyCode = currencyCode,
    deadlineEpochDay = deadlineEpochDay,
    note = note
)

fun Goal.toEntity() = GoalEntity(
    id = id,
    title = title,
    targetAmount = targetAmount,
    savedAmount = savedAmount,
    currencyCode = currencyCode,
    deadlineEpochDay = deadlineEpochDay,
    note = note
)

fun RecurringRuleEntity.toDomain() = RecurringRule(
    id = id,
    amount = amount,
    type = TransactionType.fromName(type),
    categoryId = categoryId,
    interval = RecurrenceInterval.valueOf(interval),
    startEpochDay = startEpochDay,
    endEpochDay = endEpochDay,
    note = note,
    paymentMethod = paymentMethod,
    currencyCode = currencyCode,
    isActive = isActive
)

fun RecurringRule.toEntity() = RecurringRuleEntity(
    id = id,
    amount = amount,
    type = type.name,
    categoryId = categoryId,
    interval = interval.name,
    startEpochDay = startEpochDay,
    endEpochDay = endEpochDay,
    note = note,
    paymentMethod = paymentMethod,
    currencyCode = currencyCode,
    isActive = isActive
)
