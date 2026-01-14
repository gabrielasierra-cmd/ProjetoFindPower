package com.example.projetofindpower.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable
import java.util.UUID

@Entity(tableName = "movimentacoes")
data class Movimentacao(
    @PrimaryKey
    val idMovimentacao: String = UUID.randomUUID().toString(),
    val idUtilizador: String? = null,
    val data: String = "",
    val valor: Double = 0.0,
    val tipo: TipoMovimentacao = TipoMovimentacao.DESPESA,
    val statusPagamento: StatusPagamento = StatusPagamento.PENDENTE,
    val descricao: String = "",
    val categoria: Categoria = Categoria.OUTROS, // Mudado para Enum
    val modoPagamento: String = ""
) : Serializable
