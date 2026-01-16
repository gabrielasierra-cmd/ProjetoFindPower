package com.example.projetofindpower.repository

import android.util.Log
import com.example.projetofindpower.model.*
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import java.util.*
import javax.inject.Inject

class MovimentacaoRepository @Inject constructor(
    private val movimentacaoDao: MovimentacaoDao
) {

    private val firebaseDatabase = FirebaseDatabase.getInstance().reference

    suspend fun salvarMovimentacao(mov: Movimentacao) {
        try {
            // 1. SALVAR LOCALMENTE
            movimentacaoDao.inserir(mov)
            Log.d("REPO_DEBUG", "1. Salvo no Room (Local)")

            // 2. SALVAR NO FIREBASE
            val uid = mov.idUtilizador
            if (!uid.isNullOrEmpty()) {
                Log.d("REPO_DEBUG", "2. A enviar para Firebase: users/$uid/movimentacoes/${mov.idMovimentacao}")
                
                // Adicionado .await() no final para garantir a conclusão
                firebaseDatabase.child("users")
                    .child(uid)
                    .child("movimentacoes")
                    .child(mov.idMovimentacao)
                    .setValue(mov)
                    .await() 
                
                Log.d("REPO_DEBUG", "3. SUCESSO: Gravado no Firebase!")
            } else {
                Log.e("REPO_DEBUG", "2. ERRO: UID está nulo.")
            }
        } catch (e: Exception) {
            Log.e("REPO_DEBUG", "ERRO GERAL: ${e.message}")
        }
    }

    suspend fun eliminarMovimentacao(mov: Movimentacao) {
        try {
            movimentacaoDao.eliminar(mov)
            mov.idUtilizador?.let { uid ->
                firebaseDatabase.child("users").child(uid).child("movimentacoes")
                    .child(mov.idMovimentacao).removeValue().await()
            }
        } catch (e: Exception) { Log.e("REPO", "Erro ao eliminar: ${e.message}") }
    }

    suspend fun buscarTodasPorUtilizador(userId: String): List<Movimentacao> {
        if (userId.isBlank()) return emptyList()
        try {
            val snapshot = firebaseDatabase.child("users").child(userId).child("movimentacoes").get().await()
            snapshot.children.forEach { child ->
                val map = child.value as? Map<String, Any>
                if (map != null) {
                    val catStr = map["categoria"] as? String ?: "OUTROS"
                    val catEnum = try { Categoria.valueOf(catStr.uppercase()) } catch(e: Exception) { Categoria.OUTROS }
                    val mov = Movimentacao(
                        idMovimentacao = map["idMovimentacao"] as? String ?: UUID.randomUUID().toString(),
                        idUtilizador = userId,
                        valor = (map["valor"] as? Number)?.toDouble() ?: 0.0,
                        descricao = map["descricao"] as? String ?: "",
                        tipo = try { TipoMovimentacao.valueOf(map["tipo"] as? String ?: "DESPESA") } catch (e: Exception) { TipoMovimentacao.DESPESA },
                        categoria = catEnum,
                        statusPagamento = try { StatusPagamento.valueOf(map["statusPagamento"] as? String ?: "PENDENTE") } catch (e: Exception) { StatusPagamento.PENDENTE },
                        modoPagamento = map["modoPagamento"] as? String ?: "",
                        data = map["data"] as? String ?: ""
                    )
                    movimentacaoDao.inserir(mov)
                }
            }
        } catch (e: Exception) { Log.e("REPO", "Erro sync: ${e.message}") }
        return movimentacaoDao.buscarPorUtilizador(userId)
    }

    suspend fun buscarPorMes(userId: String, mes: Int, ano: Int): List<Movimentacao> {
        val todas = buscarTodasPorUtilizador(userId)
        val calendar = Calendar.getInstance()
        return todas.filter { mov ->
            try {
                calendar.timeInMillis = mov.data.toLong()
                (calendar.get(Calendar.MONTH) + 1) == mes && calendar.get(Calendar.YEAR) == ano
            } catch (e: Exception) { false }
        }
    }
}
