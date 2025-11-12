package com.example.projetofindpower.model

class DespesaDao {

    private val despesas = mutableListOf<Despesa>()

    suspend fun getByUser(userId: String): List<Despesa> {
        // Simulação: filtra por id do usuário
        return despesas.filter { it.idUtilizador == userId }
    }

    suspend fun insert(despesa: Despesa) {
        despesas.add(despesa)
    }

    suspend fun delete(despesa: Despesa) {
        despesas.remove(despesa)
    }
}
