package com.finflow.app.data.repository

import com.finflow.app.data.local.dao.TransactionDao
import com.finflow.app.data.mapper.toDomain
import com.finflow.app.data.mapper.toEntity
import com.finflow.app.domain.model.Transaction
import com.finflow.app.domain.model.TransactionSort
import com.finflow.app.domain.model.TransactionSortField
import com.finflow.app.domain.model.TransactionType
import com.finflow.app.domain.repository.TransactionRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

/**
 * Room-backed [TransactionRepository].
 *
 * Sorting is pushed down to SQL where possible; combined text/type/category
 * filtering is applied in-memory so a single reactive stream can serve the
 * Phase 2 list screen.
 */
class TransactionRepositoryImpl @Inject constructor(
    private val dao: TransactionDao
) : TransactionRepository {

    override fun observeTransactions(
        sort: TransactionSort,
        query: String,
        type: TransactionType?,
        categoryId: Long?
    ): Flow<List<Transaction>> {
        val base = when (sort.field) {
            TransactionSortField.DATE ->
                if (sort.ascending) dao.observeByDateAsc() else dao.observeByDateDesc()
            TransactionSortField.AMOUNT ->
                if (sort.ascending) dao.observeByAmountAsc() else dao.observeByAmountDesc()
            TransactionSortField.CATEGORY -> dao.observeByCategory()
        }
        return base.map { list ->
            list.asSequence()
                .map { it.toDomain() }
                .filter { type == null || it.type == type }
                .filter { categoryId == null || it.categoryId == categoryId }
                .filter {
                    query.isBlank() ||
                        it.note.contains(query, ignoreCase = true) ||
                        (it.paymentMethod?.contains(query, ignoreCase = true) == true)
                }
                .toList()
                .let { result ->
                    if (sort.field == TransactionSortField.CATEGORY && !sort.ascending) {
                        result.sortedByDescending { it.categoryId }
                    } else result
                }
        }
    }

    override fun observeBalance(): Flow<Double> = combine(
        dao.observeIncomeTotal(),
        dao.observeExpenseTotal()
    ) { income, expense -> income - expense }

    override fun observeIncomeTotal(): Flow<Double> = dao.observeIncomeTotal()
    override fun observeExpenseTotal(): Flow<Double> = dao.observeExpenseTotal()

    override suspend fun getById(id: Long): Transaction? = dao.getById(id)?.toDomain()
    override suspend fun upsert(transaction: Transaction): Long = dao.upsert(transaction.toEntity())
    override suspend fun deleteById(id: Long) = dao.deleteById(id)
}
