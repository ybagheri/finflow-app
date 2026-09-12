package com.finflow.app.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.finflow.app.data.local.db.FinFlowDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.userPrefs by preferencesDataStore(name = "finflow_prefs")

/**
 * App-level providers: Room database, DAOs and DataStore.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): FinFlowDatabase =
        Room.databaseBuilder(context, FinFlowDatabase::class.java, FinFlowDatabase.NAME)
            .fallbackToDestructiveMigrationOnDowngrade()
            .build()

    @Provides
    fun provideTransactionDao(db: FinFlowDatabase) = db.transactionDao()

    @Provides
    fun provideCategoryDao(db: FinFlowDatabase) = db.categoryDao()

    @Provides
    fun provideBudgetDao(db: FinFlowDatabase) = db.budgetDao()

    @Provides
    fun provideGoalDao(db: FinFlowDatabase) = db.goalDao()

    @Provides
    fun provideRecurringDao(db: FinFlowDatabase) = db.recurringRuleDao()

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        context.userPrefs
}
