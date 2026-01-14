package com.example.projetofindpower

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.projetofindpower.controller.MovimentacaoController
import com.google.ai.client.generativeai.GenerativeModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MentoriaActivity : AppCompatActivity() {

    @Inject
    lateinit var controller: MovimentacaoController

    private val GEMINI_API_KEY = "AIzaSyAv-eT-ixL9w7qLzENXOIlW0ZOXTq-qb2A"

    private lateinit var txtRespostaIA: TextView
    private lateinit var progressIA: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mentoria)

        txtRespostaIA = findViewById(R.id.txtRespostaIA)
        progressIA = findViewById(R.id.progressIA)
        
        findViewById<Button>(R.id.btnFecharMentoria).setOnClickListener { finish() }

        gerarMentoriaPersonalizada()
    }

    private fun gerarMentoriaPersonalizada() {
        lifecycleScope.launch {
            try {
                progressIA.visibility = View.VISIBLE
                txtRespostaIA.text = "A contactar o teu mentor financeiro..."

                val dados = controller.buscarTodas()
                val (receitas, despesas, saldo) = controller.calcularResumo(dados)
                
                val prompt = """
                    És um mentor financeiro experiente. 
                    Resumo do utilizador: Saldo €$saldo, Receitas €$receitas, Despesas €$despesas.
                    Dá 3 conselhos financeiros práticos e curtos em Português de Portugal.
                """.trimIndent()

                val generativeModel = GenerativeModel(
                    modelName = "gemini-1.5-flash", 
                    apiKey = GEMINI_API_KEY
                )

                val response = generativeModel.generateContent(prompt)
                txtRespostaIA.text = response.text ?: "O mentor está a refletir. Tenta novamente."

            } catch (e: Exception) {
                txtRespostaIA.text = "Erro 404 persistente.\n\n" +
                        "Causa provável: A 'Generative Language API' ainda está a ser propagada pelo Google.\n\n" +
                        "Ação: Aguarde 5 minutos e tente novamente sem fechar o app.\n\n" +
                        "Detalhes: ${e.message}"
            } finally {
                progressIA.visibility = View.GONE
            }
        }
    }
}
