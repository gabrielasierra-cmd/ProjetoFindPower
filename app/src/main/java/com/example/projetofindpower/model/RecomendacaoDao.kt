package com.example.projetofindpower.model

import androidx.room.*

@Dao
interface RecomendacaoDao {
    @Query("SELECT * FROM recomendacoes WHERE idUtilizador = :userId ORDER BY dataCriacao DESC")
    suspend fun buscarPorUtilizador(userId: String): List<Recomendacao>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun salvar(recomendacao: Recomendacao)

    @Delete
    suspend fun eliminar(recomendacao: Recomendacao)
}
