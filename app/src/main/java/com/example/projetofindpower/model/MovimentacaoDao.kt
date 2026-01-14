package com.example.projetofindpower.model

import androidx.room.*

@Dao
interface MovimentacaoDao {

    @Query("SELECT * FROM movimentacoes WHERE idUtilizador = :userId ORDER BY data DESC")
    suspend fun buscarPorUtilizador(userId: String): List<Movimentacao>

    @Query("SELECT * FROM movimentacoes WHERE idUtilizador = :userId AND tipo = :tipo")
    suspend fun buscarPorTipo(userId: String, tipo: TipoMovimentacao): List<Movimentacao>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(movimentacao: Movimentacao)

    @Delete
    suspend fun eliminar(movimentacao: Movimentacao)

    @Query("DELETE FROM movimentacoes WHERE idUtilizador = :userId")
    suspend fun eliminarTudo(userId: String)
}
