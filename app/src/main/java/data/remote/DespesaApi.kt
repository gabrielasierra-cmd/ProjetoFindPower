package data.remote

import data.local.Despesa

class DespesaApi {

    suspend fun getDespesas(userId: String): List<Despesa> {
        // Aqui podes simular uma resposta ou conectar com a API real depois
        return emptyList()
    }

    suspend fun criarDespesa(despesa: Despesa): Boolean {
        // Aqui podes simular envio ou preparar para usar Retrofit depois
        return true
    }
}
