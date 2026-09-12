package com.finflow.app.di

import com.finflow.app.data.repository.BudgetRepositoryImpl
import com.finflow.app.data.repository.CategoryRepositoryImpl
import com.finflow.app.data.repository.GoalRepositoryImpl
import com.finflow.app.data.repository.RecurringRepositoryImpl
import com.finflow.app.data.repository.TransactionRepositoryImpl
import com.finflow.app.domain.repository.BudgetRepository
import com.finflow.app.domain.repository.CategoryRepository
import com.finflow.app.domain.repository.GoalRepository
import com.finflow.app.domain.repository.RecurringRepository
import com.finflow.app.domain.repository.TransactionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Binds repository interfaces to their Room implementations.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindTransactions(impl: TransactionRepositoryImpl): TransactionRepository

    @Binds
    @Singleton
    abstract fun bindCategories(impl: CategoryRepositoryImpl): CategoryRepository

    @Binds
    @Singleton
    abstract fun bindBudgets(impl: BudgetRepositoryImpl): BudgetRepository

    @Binds
    @Singleton
    abstract fun bindGoals(impl: GoalRepositoryImpl): GoalRepository

    @Binds
    @Singleton
    abstract fun bindRecurring(impl: RecurringRepositoryImpl): RecurringRepository
}
