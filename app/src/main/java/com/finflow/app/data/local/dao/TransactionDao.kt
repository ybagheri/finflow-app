package com.finflow.app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.finflow.app.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

/**
 * Queries for [TransactionEntity]. Search/filter/sort variants back the
 * reactive streams consumed by the transaction list (Phase 2).
 */
@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY dateEpochDay DESC, createdAtMillis DESC")
    fun observeByDateDesc(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY dateEpochDay ASC, createdAtMillis ASC")
    fun observeByDateAsc(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY amount DESC")
    fun observeByAmountDesc(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY amount ASC")
    fun observeByAmountAsc(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY categoryId ASC, dateEpochDay DESC")
    fun observeByCategory(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): TransactionEntity?

    @Query(
        """SELECT * FROM transactions
           WHERE (:type IS NULL OR type = :type)
           AND (:categoryId IS NULL OR categoryId = :categoryId)
           AND (note LIKE '%' || :query || '%' OR paymentMethod LIKE '%' || :query || '%')
           ORDER BY dateEpochDay DESC, createdAtMillis DESC"""
    )
    fun search(type: String?, categoryId: Long?, query: String): Flow<List<TransactionEntity>>

    @Query("SELECT COALESCE(SUM(CASE WHEN type = 'INCOME' THEN amount ELSE 0 END), 0) FROM transactions")
    fun observeIncomeTotal(): Flow<Double>

    @Query("SELECT COALESCE(SUM(CASE WHEN type = 'EXPENSE' THEN amount ELSE 0 END), 0) FROM transactions")
    fun observeExpenseTotal(): Flow<Double>

    @Upsert
    suspend fun upsert(entity: TransactionEntity): Long

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Long)
}
