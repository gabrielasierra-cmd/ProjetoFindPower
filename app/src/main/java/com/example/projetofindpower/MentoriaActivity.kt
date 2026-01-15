package com.example.projetofindpower

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.projetofindpower.controller.MovimentacaoController
import com.example.projetofindpower.network.GeminiRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MentoriaActivity : AppCompatActivity() {

    @Inject
    lateinit var controller: MovimentacaoController

    @Inject
    lateinit var geminiRepository: GeminiRepository

    private lateinit var txtRespostaIA: TextView
    private lateinit var progressIA: ProgressBar
    private lateinit var editPergunta: EditText
    private lateinit var btnEnviar: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mentoria)

        txtRespostaIA = findViewById(R.id.txtRespostaIA)
        progressIA = findViewById(R.id.progressIA)
        editPergunta = findViewById(R.id.editPergunta)
        btnEnviar = findViewById(R.id.btnEnviar)
        
        findViewById<android.widget.Button>(R.id.btnFecharMentoria).setOnClickListener { finish() }

        // Enviar pergunta ao clicar no botão
        btnEnviar.setOnClickListener {
            val pergunta = editPergunta.text.toString()
            if (pergunta.isNotBlank()) {
                enviarPergunta(pergunta)
            }
        }

        // Ao abrir, gera o conselho inicial automático
        carregarConselhoInicial()
    }

    private fun carregarConselhoInicial() {
        lifecycleScope.launch {
            try {
                progressIA.visibility = View.VISIBLE
                val movimentacoes = controller.buscarTodas()
                val (receitas, despesas, saldo) = controller.calcularResumo(movimentacoes)
                
                val resposta = geminiRepository.gerarConselhos(receitas, despesas, saldo)
                txtRespostaIA.text = resposta ?: "Olá! Sou seu mentor financeiro FinPower?"
            } catch (e: Exception) {
                txtRespostaIA.text = "Erro ao carregar conselho: ${e.message}"
            } finally {
                progressIA.visibility = View.GONE
            }
        }
    }

    private fun enviarPergunta(texto: String) {
        lifecycleScope.launch {
            try {
                progressIA.visibility = View.VISIBLE
                editPergunta.text.clear()
                txtRespostaIA.text = "A pensar..."

                val resposta = geminiRepository.perguntarIA(texto)
                
                if (resposta != null) {
                    txtRespostaIA.text = resposta
                } else {
                    txtRespostaIA.text = "O Gemini 2.0 não conseguiu responder. Tenta novamente."
                }
            } catch (e: Exception) {
                txtRespostaIA.text = "Erro: ${e.message}"
            } finally {
                progressIA.visibility = View.GONE
            }
        }
    }
}
