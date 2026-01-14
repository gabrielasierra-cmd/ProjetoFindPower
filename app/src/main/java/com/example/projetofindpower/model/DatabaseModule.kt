package com.example.projetofindpower.di

import android.content.Context
import androidx.room.Room
import com.example.projetofindpower.model.AppDatabase
import com.example.projetofindpower.model.MovimentacaoDao
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
        return AppDatabase.getInstance(context) // Usando o método getInstance que já criamos
    }

    @Provides
    fun provideMovimentacaoDao(database: AppDatabase): MovimentacaoDao {
        return database.movimentacaoDao()
    }
}
