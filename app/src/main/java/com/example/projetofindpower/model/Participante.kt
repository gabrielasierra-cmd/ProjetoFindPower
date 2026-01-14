package com.example.projetofindpower.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "participantes")
data class Participante(
    @PrimaryKey
    val idParticipante: String = UUID.randomUUID().toString(),
    val nome: String = "",
    val emailTelefone: String = ""
)
