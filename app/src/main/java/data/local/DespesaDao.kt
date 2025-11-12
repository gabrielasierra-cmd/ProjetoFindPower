package data.local

class DespesaDao {
    suspend fun getByUser(userId: String): List<Despesa> {
        return emptyList()
    }

    suspend fun insert(despesa: Despesa) {
        // lógica de inserção
    }

    suspend fun delete(despesa: Despesa) {
        // lógica de remoção
    }
}