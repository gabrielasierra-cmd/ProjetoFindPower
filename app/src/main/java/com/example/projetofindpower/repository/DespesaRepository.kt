package com.example.projetofindpower.repository

import com.example.projetofindpower.model.Despesa
import com.example.projetofindpower.model.DespesaDao
import com.example.projetofindpower.model.DespesaApi

class DespesaRepository(
    private val dao: DespesaDao,
    private val api: DespesaApi
) {

    suspend fun getDespesas(userId: String): List<Despesa> {
        val local = dao.getByUser(userId)
        return local.ifEmpty {
            val remoto = api.getDespesas(userId)
            remoto.forEach { dao.insert(it) }
            remoto
        }
    }

    suspend fun criarDespesa(despesa: Despesa) {
        dao.insert(despesa)
        api.criarDespesa(despesa)
    }
    suspend fun deleteDespesa(despesa: Despesa) {
        dao.delete(despesa)
    }
}
