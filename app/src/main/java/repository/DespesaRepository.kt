package repository

import data.local.Despesa
import data.local.DespesaDao
import data.remote.DespesaApi

class DespesaRepository(
    private val dao: DespesaDao,
    private val api: DespesaApi
) {
    suspend fun getDespesas(userId: String): List<Despesa> {
        val local = dao.getByUser(userId)
        return if (local.isNotEmpty()) local else api.getDespesas(userId)
    }

    suspend fun criarDespesa(despesa: Despesa) {
        dao.insert(despesa)
        api.criarDespesa(despesa)
    }
}