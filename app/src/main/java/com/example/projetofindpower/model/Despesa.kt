package com.example.projetofindpower.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "despesas")
data class Despesa(
    @PrimaryKey
    val idDespesa: String = "",
    val idUtilizador: String = "",
    val data: String = "",
    val valor: Double = 0.0,
    val tipo: String = "",
    val pagamento: String = "",
    val descricao: String = "",
    val modoPagamento: String = "",
    val statusPagamento: String = ""
)
{
    companion object {
        val LISTA_CATEGORIAS = arrayOf(
            "Lazer",
            "Emergência",
            "Contas Fixas",
            "Poupança",
            "Extras",
            "Viagens"
        )
        val LISTA_STATUS = arrayOf("Pago", "Pendente")
        val LISTA_MODOS = arrayOf("Dinheiro", "Cartão de Crédito", "Cartão de Débito", "Transferência", "MB Way")
    }

}