package com.example.projetofindpower.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "recomendacoes")
data class Recomendacao(
    @PrimaryKey
    val idRecomendacao: String = UUID.randomUUID().toString(),
    val idUtilizador: String? = null,
    val tipo: String = "Orcamento", // Orcamento, Poupanca, Investimento, Alerta
    val conteudo: String = "",
    val dataCriacao: Long = System.currentTimeMillis()
)
