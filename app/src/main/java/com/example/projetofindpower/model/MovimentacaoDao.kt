package com.example.projetofindpower.model

import androidx.room.*

@Dao
interface MovimentacaoDao {

    @Query("SELECT * FROM movimentacoes WHERE idUtilizador = :userId")
    suspend fun getByUser(userId: String): List<Movimentacao>

    @Query("SELECT * FROM movimentacoes WHERE idUtilizador = :userId AND tipo = :categoria")
    suspend fun getByUserAndCategory(userId: String, categoria: String): List<Movimentacao>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(movimentacao: Movimentacao)

    @Delete
    suspend fun delete(movimentacao: Movimentacao)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(movimentacoes: List<Movimentacao>)
}
