package com.example.projetofindpower.controller

import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// Este módulo ensina o Hilt a criar dependências do Firebase
@Module
@InstallIn(SingletonComponent::class) // Garante que a dependência viva enquanto o app estiver rodando
object FirebaseModule {

    @Provides
    @Singleton // Garante que apenas uma instância de FirebaseAuth exista
    fun provideFirebaseAuth(): FirebaseAuth {
        // A "receita" para obter o FirebaseAuth
        return FirebaseAuth.getInstance()
    }
}