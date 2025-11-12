package com.example.projetofindpower.model

class DespesaApi {

    suspend fun getDespesas(userId: String): List<Despesa> {
        // Simulação de chamada remota
        println("Buscando despesas do usuário $userId na API...")
        return emptyList()
    }

    suspend fun criarDespesa(despesa: Despesa): Boolean {
        // Simulação de envio para servidor
        println("Enviando despesa ${despesa.idDespesa} para API...")
        return true
    }
}
