package com.example.projetofindpower.controller

import com.example.projetofindpower.model.Movimentacao
import com.example.projetofindpower.repository.AuthRepository
import com.example.projetofindpower.repository.MovimentacaoRepository
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MovimentacaoController @Inject constructor(
    private val repository: MovimentacaoRepository,
    private val authRepository: AuthRepository
) {

    private fun getUserId(): String? = authRepository.getCurrentUser()?.uid

    suspend fun salvar(mov: Movimentacao) = repository.saveMovimentacao(mov)

    suspend fun atualizar(mov: Movimentacao) = repository.updateMovimentacao(mov)

    suspend fun excluir(mov: Movimentacao) = repository.deleteMovimentacao(mov)

    suspend fun buscarTodas(): List<Movimentacao> {
        val uid = getUserId() ?: return emptyList()
        return repository.getMovimentacoesByUser(uid)
    }

    suspend fun buscarPorCategoria(categoria: String): List<Movimentacao> {
        val todas = buscarTodas()
        return todas.filter { it.tipo.equals(categoria, ignoreCase = true) }
    }

    suspend fun buscarPorMesEAno(mes: Int, ano: Int): List<Movimentacao> {
        val todas = buscarTodas()
        val cal = Calendar.getInstance()
        return todas.filter { mov ->
            try {
                cal.timeInMillis = mov.data.toLong()
                (cal.get(Calendar.MONTH) + 1) == mes && cal.get(Calendar.YEAR) == ano
            } catch (e: Exception) { false }
        }
    }

    // NOVA LÓGICA ANUAL
    suspend fun buscarPorAno(ano: Int): List<Movimentacao> {
        val todas = buscarTodas()
        val cal = Calendar.getInstance()
        return todas.filter { mov ->
            try {
                cal.timeInMillis = mov.data.toLong()
                cal.get(Calendar.YEAR) == ano
            } catch (e: Exception) { false }
        }
    }

    suspend fun buscarDadosMesAtual(): List<Movimentacao> {
        val cal = Calendar.getInstance()
        return buscarPorMesEAno(cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR))
    }

    fun calcularResumo(lista: List<Movimentacao>): Triple<Double, Double, Double> {
        val receitas = lista.filter { it.natureza == "Receita" }.sumOf { it.valor }
        val despesas = lista.filter { it.natureza == "Despesa" }.sumOf { it.valor }
        return Triple(receitas, despesas, receitas - despesas)
    }
}
