package com.example.projetofindpower.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "movimentacoes")
data class Movimentacao(
    @PrimaryKey
    val idMovimentacao: String = "",
    val idUtilizador: String = "",
    val data: String = "",
    val valor: Double = 0.0,
    val tipo: String = "", // Categoria
    val natureza: String = "Despesa", // "Despesa" ou "Receita"
    val descricao: String = "",
    val modoPagamento: String = "",
    val statusPagamento: String = ""
) : Serializable { // Adicionado Serializable para permitir passar via Intent
    companion object {
        val LISTA_CATEGORIAS_DESPESA = arrayOf("Lazer", "Emergência", "Contas Fixas", "Poupança", "Extras", "Viagens")
        val LISTA_CATEGORIAS_RECEITA = arrayOf("Salário", "Investimento", "Presente", "Venda", "Outros")
        val LISTA_STATUS = arrayOf("Pago", "Pendente", "Recebido")
        val LISTA_MODOS = arrayOf("Dinheiro", "Cartão", "Transferência", "MB Way")
    }
}
