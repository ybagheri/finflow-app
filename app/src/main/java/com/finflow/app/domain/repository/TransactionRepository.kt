package com.finflow.app.domain.repository

import com.finflow.app.domain.model.Category
import com.finflow.app.domain.model.Transaction
import com.finflow.app.domain.model.TransactionSort
import com.finflow.app.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow

/**
 * Single source of truth contract for transactions.
 * Implemented in `data/repository/TransactionRepositoryImpl` (Room-backed).
 */
interface TransactionRepository {
    /** Reactive stream of transactions honoring sort / filter params. */
    fun observeTransactions(
        sort: TransactionSort,
        query: String = "",
        type: TransactionType? = null,
        categoryId: Long? = null
    ): Flow<List<Transaction>>

    fun observeBalance(): Flow<Double>
    fun observeIncomeTotal(): Flow<Double>
    fun observeExpenseTotal(): Flow<Double>

    suspend fun getById(id: Long): Transaction?
    suspend fun upsert(transaction: Transaction): Long
    suspend fun deleteById(id: Long)
}
