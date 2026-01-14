package com.example.projetofindpower.controller

import android.util.Log
import com.example.projetofindpower.model.Categoria
import com.example.projetofindpower.model.Movimentacao
import com.example.projetofindpower.model.TipoMovimentacao
import com.example.projetofindpower.repository.MovimentacaoRepository
import com.google.firebase.auth.FirebaseAuth
import java.util.Calendar
import javax.inject.Inject

class MovimentacaoController @Inject constructor(
    private val repository: MovimentacaoRepository
) {

    suspend fun salvar(mov: Movimentacao) = repository.salvarMovimentacao(mov)

    suspend fun eliminar(mov: Movimentacao) = repository.eliminarMovimentacao(mov)

    suspend fun buscarTodas(): List<Movimentacao> {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        val lista = repository.buscarTodasPorUtilizador(uid)
        Log.d("DEBUG_MOV", "Total de movimentações encontradas para o UID $uid: ${lista.size}")
        return lista
    }

    suspend fun buscarPorCategoria(categoria: Categoria): List<Movimentacao> {
        return buscarTodas().filter { it.categoria == categoria }
    }

    suspend fun buscarPorAno(ano: Int): List<Movimentacao> {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        val todas = repository.buscarTodasPorUtilizador(uid)
        val cal = Calendar.getInstance()
        return todas.filter { 
            try {
                cal.timeInMillis = it.data.toLong()
                cal.get(Calendar.YEAR) == ano
            } catch (e: Exception) { false }
        }
    }

    suspend fun buscarPorMesEAno(mes: Int, ano: Int): List<Movimentacao> {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        return repository.buscarPorMes(uid, mes, ano)
    }

    suspend fun buscarDadosMesAtual(): List<Movimentacao> {
        val cal = Calendar.getInstance()
        return buscarPorMesEAno(cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR))
    }

    fun calcularResumo(lista: List<Movimentacao>): Triple<Double, Double, Double> {
        val receitas = lista.filter { it.tipo == TipoMovimentacao.RECEITA }.sumOf { it.valor }
        val despesas = lista.filter { it.tipo == TipoMovimentacao.DESPESA }.sumOf { it.valor }
        val saldo = receitas - despesas
        return Triple(receitas, despesas, saldo)
    }
}
