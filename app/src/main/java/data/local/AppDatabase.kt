package data.local

class AppDatabase {
    fun despesaDao(): DespesaDao {
        return DespesaDao()
    }
}