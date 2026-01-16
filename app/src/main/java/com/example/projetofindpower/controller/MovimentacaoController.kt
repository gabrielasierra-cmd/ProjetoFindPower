package com.example.projetofindpower.controller

import android.util.Log
import com.example.projetofindpower.BuildConfig
import com.example.projetofindpower.model.Categoria
import com.example.projetofindpower.model.Movimentacao
import com.example.projetofindpower.model.TipoMovimentacao
import com.example.projetofindpower.network.*
import com.example.projetofindpower.repository.MovimentacaoRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Calendar
import javax.inject.Inject

class MovimentacaoController @Inject constructor(
    private val repository: MovimentacaoRepository
) {

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://fcm.googleapis.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val fcmService = retrofit.create(FcmApiService::class.java)
    
    private val ACCESS_TOKEN = BuildConfig.FCM_ACCESS_TOKEN

    suspend fun salvar(mov: Movimentacao) {
        repository.salvarMovimentacao(mov)
        if (mov.tipo == TipoMovimentacao.DESPESA) {
            enviarPushIndividual(mov)
        }
    }

    private fun enviarPushIndividual(mov: Movimentacao) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // DELAY DE 5 SEGUNDOS: Dá tempo para fechar a app e ver a notificação no ecrã inicial
                Log.d("FCM_DEBUG", "Aguardando 5 segundos antes de enviar o push...")
                delay(5000)

                val tokenRef = FirebaseDatabase.getInstance().getReference("users").child(uid).child("fcmToken")
                val snapshot = tokenRef.get().await()
                val userToken = snapshot.getValue(String::class.java)

                if (userToken != null) {
                    val request = FcmRequest(
                        message = Message(
                            token = userToken,
                            notification = FcmNotification(
                                title = "Despesa Registada 💸",
                                body = "${mov.valor}€ em ${mov.descricao ?: "Geral"}"
                            )
                        )
                    )

                    Log.d("FCM_DEBUG", "A enviar Push com Token que começa por: ${ACCESS_TOKEN.take(10)}...")
                    
                    val response = fcmService.sendPushNotification("Bearer $ACCESS_TOKEN", request)
                    
                    if (response.isSuccessful) {
                        Log.d("FCM_DEBUG", "Push enviado com sucesso!")
                    } else {
                        val erro = response.errorBody()?.string()
                        Log.e("FCM_DEBUG", "Erro Google: ${response.code()} - $erro")
                    }
                }
            } catch (e: Exception) {
                Log.e("FCM_DEBUG", "Erro: ${e.message}")
            }
        }
    }

    suspend fun buscarTodas(): List<Movimentacao> {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        return repository.buscarTodasPorUtilizador(uid)
    }

    suspend fun buscarPorCategoria(categoria: Categoria): List<Movimentacao> {
        val todas = buscarTodas()
        return todas.filter { it.categoria == categoria }
    }

    suspend fun eliminar(mov: Movimentacao) = repository.eliminarMovimentacao(mov)

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
