package com.example.projetofindpower.di

import android.content.Context
import androidx.room.Room
import com.example.projetofindpower.model.*
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
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    fun provideMovimentacaoDao(database: AppDatabase): MovimentacaoDao {
        return database.movimentacaoDao()
    }

    @Provides
    fun provideUserDao(database: AppDatabase): UserDao {
        return database.userDao()
    }

    @Provides
    fun provideRecomendacaoDao(database: AppDatabase): RecomendacaoDao {
        return database.recomendacaoDao()
    }

    @Provides
    fun provideParticipanteDao(database: AppDatabase): ParticipanteDao {
        return database.participanteDao()
    }
}
