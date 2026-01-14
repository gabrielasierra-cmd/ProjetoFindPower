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
            movimentacaoDao.inserir(mov)
            mov.idUtilizador?.let { uid ->
                firebaseDatabase.child("users")
                    .child(uid)
                    .child("movimentacoes")
                    .child(mov.idMovimentacao)
                    .setValue(mov)
                    .await()
            }
        } catch (e: Exception) {
            Log.e("REPO", "Erro ao salvar: ${e.message}")
        }
    }

    suspend fun eliminarMovimentacao(mov: Movimentacao) {
        try {
            movimentacaoDao.eliminar(mov)
            mov.idUtilizador?.let { uid ->
                firebaseDatabase.child("users")
                    .child(uid)
                    .child("movimentacoes")
                    .child(mov.idMovimentacao)
                    .removeValue()
                    .await()
            }
        } catch (e: Exception) {
            Log.e("REPO", "Erro ao eliminar: ${e.message}")
        }
    }

    suspend fun buscarTodasPorUtilizador(userId: String): List<Movimentacao> {
        if (userId.isBlank()) return emptyList()

        // 1. Tenta sincronizar Firebase -> Room primeiro
        try {
            val snapshot = firebaseDatabase.child("users").child(userId).child("movimentacoes").get().await()
            snapshot.children.forEach { child ->
                try {
                    val map = child.value as? Map<String, Any>
                    if (map != null) {
                        val catStr = map["categoria"] as? String ?: "OUTROS"
                        // Mapeamento manual preventivo para o Firebase também
                        val catEnum = when(catStr.uppercase()) {
                            "CONTAS", "CONTAS FIXAS", "CONTAS_FIXAS" -> Categoria.CONTAS_FIXAS
                            "LAZER" -> Categoria.LAZER
                            "EMERGENCIA", "EMERGÊNCIA" -> Categoria.EMERGENCIA
                            "POUPANÇA", "POUPANCA" -> Categoria.POUPANCA
                            "EXTRAS" -> Categoria.EXTRAS
                            "VIAGENS" -> Categoria.VIAGENS
                            "SALÁRIO", "SALARIO" -> Categoria.SALARIO
                            "INVESTIMENTO" -> Categoria.INVESTIMENTO
                            "PRESENTE" -> Categoria.PRESENTE
                            "VENDA" -> Categoria.VENDA
                            else -> Categoria.OUTROS
                        }

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
                } catch (e: Exception) { 
                    Log.e("REPO", "Erro ao converter item Firebase: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e("REPO", "Erro ao buscar Firebase: ${e.message}")
        }

        // 2. Retorna do Room (O Converters.kt agora protege contra o erro 'Contas')
        return try {
            movimentacaoDao.buscarPorUtilizador(userId)
        } catch (e: Exception) {
            Log.e("REPO", "Erro fatal no Room: ${e.message}")
            emptyList()
        }
    }

    suspend fun buscarPorMes(userId: String, mes: Int, ano: Int): List<Movimentacao> {
        val todas = buscarTodasPorUtilizador(userId)
        val calendar = Calendar.getInstance()
        return todas.filter { mov ->
            try {
                calendar.timeInMillis = mov.data.toLong()
                val m = calendar.get(Calendar.MONTH) + 1
                val a = calendar.get(Calendar.YEAR)
                m == mes && a == ano
            } catch (e: Exception) { false }
        }
    }
}
