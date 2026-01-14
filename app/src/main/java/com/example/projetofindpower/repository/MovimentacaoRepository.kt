package com.example.projetofindpower.repository

import com.example.projetofindpower.model.Movimentacao
import com.example.projetofindpower.model.MovimentacaoDao
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import java.util.*
import javax.inject.Inject

class MovimentacaoRepository @Inject constructor(
    private val movimentacaoDao: MovimentacaoDao
) {

    private val firebaseDatabase = FirebaseDatabase.getInstance().reference

    suspend fun saveMovimentacao(mov: Movimentacao) {
        movimentacaoDao.insert(mov)
        firebaseDatabase.child("users")
            .child(mov.idUtilizador)
            .child("movimentacoes")
            .child(mov.idMovimentacao)
            .setValue(mov)
            .await()
    }

    // Função para EDITAR (serve para Despesa ou Receita)
    suspend fun updateMovimentacao(mov: Movimentacao) {
        movimentacaoDao.insert(mov) // O Room usa o mesmo 'insert' com OnConflictStrategy.REPLACE
        firebaseDatabase.child("users")
            .child(mov.idUtilizador)
            .child("movimentacoes")
            .child(mov.idMovimentacao)
            .setValue(mov)
            .await()
    }

    suspend fun getMovimentacoesByUser(userId: String): List<Movimentacao> {
        return try {
            val snapshot = firebaseDatabase.child("users").child(userId).child("movimentacoes").get().await()
            val listaFirebase = mutableListOf<Movimentacao>()

            snapshot.children.forEach { child ->
                child.getValue(Movimentacao::class.java)?.let { listaFirebase.add(it) }
            }

            if (listaFirebase.isNotEmpty()) {
                movimentacaoDao.insertAll(listaFirebase)
            }

            movimentacaoDao.getByUser(userId)
        } catch (e: Exception) {
            movimentacaoDao.getByUser(userId)
        }
    }

    // Função para buscar apenas DESPESAS
    suspend fun getDespesas(userId: String): List<Movimentacao> {
        val todas = getMovimentacoesByUser(userId)
        return todas.filter { it.natureza == "Despesa" }
    }

    // Função para buscar apenas RECEITAS
    suspend fun getReceitas(userId: String): List<Movimentacao> {
        val todas = getMovimentacoesByUser(userId)
        return todas.filter { it.natureza == "Receita" }
    }

    suspend fun getMovimentacoesByCategory(userId: String, categoria: String): List<Movimentacao> {
        getMovimentacoesByUser(userId) 
        return movimentacaoDao.getByUserAndCategory(userId, categoria)
    }

    suspend fun getMovimentacoesByMonth(userId: String, mes: Int, ano: Int): List<Movimentacao> {
        val todas = getMovimentacoesByUser(userId)
        val calendar = Calendar.getInstance()
        
        return todas.filter { mov ->
            try {
                val timestamp = mov.data.toLong()
                calendar.timeInMillis = timestamp
                val m = calendar.get(Calendar.MONTH) + 1
                val a = calendar.get(Calendar.YEAR)
                m == mes && a == ano
            } catch (e: Exception) {
                false
            }
        }
    }
}
