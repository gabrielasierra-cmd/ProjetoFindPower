package com.example.projetofindpower.repository

import com.example.projetofindpower.model.Despesa
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class DespesaRepository @Inject constructor() {

    private val database = FirebaseDatabase.getInstance().reference

    /**
     * Salva a despesa no Firebase Realtime Database
     */
    suspend fun saveExpense(despesa: Despesa) {
        database.child("users")
            .child(despesa.idUtilizador)
            .child("expenses")
            .child(despesa.idDespesa)
            .setValue(despesa)
            .await()
    }

    /**
     * Recupera todas as despesas de um utilizador específico
     */
    suspend fun getExpensesByUser(userId: String): List<Despesa> {
        val snapshot = database.child("users").child(userId).child("expenses").get().await()
        val listaDespesas = mutableListOf<Despesa>()

        snapshot.children.forEach { child ->
            child.getValue(Despesa::class.java)?.let {
                listaDespesas.add(it)
            }
        }
        return listaDespesas
    }
}
