package com.example.projetofindpower.repository

import com.example.projetofindpower.model.Despesa
import com.example.projetofindpower.model.DespesaDao
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import java.util.*
import javax.inject.Inject

class DespesaRepository @Inject constructor(
    private val despesaDao: DespesaDao
) {

    private val firebaseDatabase = FirebaseDatabase.getInstance().reference

    suspend fun saveExpense(despesa: Despesa) {
        despesaDao.insert(despesa)
        firebaseDatabase.child("users")
            .child(despesa.idUtilizador)
            .child("expenses")
            .child(despesa.idDespesa)
            .setValue(despesa)
            .await()
    }

    suspend fun getExpensesByUser(userId: String): List<Despesa> {
        return try {
            val snapshot = firebaseDatabase.child("users").child(userId).child("expenses").get().await()
            val listaFirebase = mutableListOf<Despesa>()

            snapshot.children.forEach { child ->
                child.getValue(Despesa::class.java)?.let { listaFirebase.add(it) }
            }

            if (listaFirebase.isNotEmpty()) {
                despesaDao.insertAll(listaFirebase)
            }

            despesaDao.getByUser(userId)
        } catch (e: Exception) {
            despesaDao.getByUser(userId)
        }
    }

    suspend fun getExpensesByCategory(userId: String, categoria: String): List<Despesa> {
        getExpensesByUser(userId) 
        return despesaDao.getByUserAndCategory(userId, categoria)
    }

    // NOVO MÉTODO: Filtrar por Mês e Ano
    suspend fun getExpensesByMonth(userId: String, mes: Int, ano: Int): List<Despesa> {
        // Garantimos que temos os dados mais recentes
        val todas = getExpensesByUser(userId)
        
        val calendar = Calendar.getInstance()
        
        return todas.filter { despesa ->
            try {
                // Tenta converter o timestamp String para Long e depois extrair mês/ano
                val timestamp = despesa.data.toLong()
                calendar.timeInMillis = timestamp
                
                val m = calendar.get(Calendar.MONTH) + 1 // Calendar.MONTH começa em 0
                val a = calendar.get(Calendar.YEAR)
                
                m == mes && a == ano
            } catch (e: Exception) {
                false // Se a data estiver em formato inválido, ignora
            }
        }
    }
}
