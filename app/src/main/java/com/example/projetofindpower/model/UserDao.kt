package com.example.projetofindpower.model

import androidx.room.*

@Dao
interface UserDao {
    @Query("SELECT * FROM utilizadores WHERE idUtilizador = :id LIMIT 1")
    suspend fun buscarPorId(id: String): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun salvar(user: User)

    @Update
    suspend fun atualizar(user: User)
}
