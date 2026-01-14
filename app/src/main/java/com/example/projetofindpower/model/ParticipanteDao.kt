package com.example.projetofindpower.model

import androidx.room.*

@Dao
interface ParticipanteDao {
    @Query("SELECT * FROM participantes")
    suspend fun buscarTodos(): List<Participante>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun salvar(participante: Participante)

    @Query("SELECT p.* FROM participantes p INNER JOIN despesa_participante dp ON p.idParticipante = dp.idParticipante WHERE dp.idDespesa = :idDespesa")
    suspend fun buscarPorDespesa(idDespesa: String): List<Participante>
}
