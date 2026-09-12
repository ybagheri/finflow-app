package com.finflow.app.domain.repository

import com.finflow.app.domain.model.Category
import com.finflow.app.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow

/** Contract for category CRUD + seed defaults. */
interface CategoryRepository {
    fun observeAll(): Flow<List<Category>>
    fun observeByType(type: TransactionType): Flow<List<Category>>
    suspend fun getById(id: Long): Category?
    suspend fun upsert(category: Category): Long
    suspend fun deleteById(id: Long)
    /** Inserts built-in categories on first launch; no-op afterwards. */
    suspend fun seedDefaultsIfEmpty()
}
