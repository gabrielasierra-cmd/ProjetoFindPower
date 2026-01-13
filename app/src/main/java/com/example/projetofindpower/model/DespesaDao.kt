package com.example.projetofindpower.model

import androidx.room.*

@Dao
interface DespesaDao {

    @Query("SELECT * FROM despesas WHERE idUtilizador = :userId")
    suspend fun getByUser(userId: String): List<Despesa>

    @Query("SELECT * FROM despesas WHERE idUtilizador = :userId AND tipo = :categoria")
    suspend fun getByUserAndCategory(userId: String, categoria: String): List<Despesa>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(despesa: Despesa)

    @Delete
    suspend fun delete(despesa: Despesa)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(despesas: List<Despesa>)
}
