package com.example.projetofindpower.model

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        User::class,
        Movimentacao::class,
        Recomendacao::class,
        Participante::class,
        DespesaParticipante::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun movimentacaoDao(): MovimentacaoDao
    abstract fun userDao(): UserDao
    abstract fun recomendacaoDao(): RecomendacaoDao
    abstract fun participanteDao(): ParticipanteDao
}
