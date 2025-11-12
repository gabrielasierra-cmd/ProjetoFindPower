package com.example.projetofindpower.model

class AppDatabase private constructor() {

    private val despesaDaoInstance = DespesaDao()

    fun despesaDao(): DespesaDao = despesaDaoInstance

    companion object {
        // Singleton — garante uma única instância no app
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(): AppDatabase {
            return instance ?: synchronized(this) {
                val newInstance = AppDatabase()
                instance = newInstance
                newInstance
            }
        }
    }
}
