package com.example.projetofindpower.di

import android.content.Context
import androidx.room.Room
import com.example.projetofindpower.model.AppDatabase
import com.example.projetofindpower.model.DespesaDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "findpower_db"
        ).build()
    }

    @Provides
    fun provideDespesaDao(database: AppDatabase): DespesaDao {
        return database.despesaDao() // Esta função deve existir no seu AppDatabase
    }
}