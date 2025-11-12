package com.example.projetofindpower.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "despesas")
data class Despesa(
    @PrimaryKey val idDespesa: String,
    val idUtilizador: String,
    val data: String,
    val valor: Double,
    val tipo: String,
    val statusPagamento: String
)
