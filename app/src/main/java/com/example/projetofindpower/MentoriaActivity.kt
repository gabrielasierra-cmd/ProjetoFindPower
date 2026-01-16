package com.example.projetofindpower

import android.os.Bundle
import android.view.View
import android.widget.EditText
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
    private lateinit var btnEnviar: com.google.android.material.floatingactionbutton.FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mentoria)

        txtRespostaIA = findViewById(R.id.txtRespostaIA)
        progressIA = findViewById(R.id.progressIA)
        editPergunta = findViewById(R.id.editPergunta)
        btnEnviar = findViewById(R.id.btnEnviar)
        
        findViewById<android.widget.Button>(R.id.btnFecharMentoria).setOnClickListener { finish() }

        // Mensagem inicial estática (Sem gastar tokens ou enviar dados)
        txtRespostaIA.text = "Olá! Sou seu Mentor de Finanças. O que gostarias de perguntar hoje?"

        // Enviar pergunta apenas quando o utilizador quiser
        btnEnviar.setOnClickListener {
            val pergunta = editPergunta.text.toString()
            if (pergunta.isNotBlank()) {
                enviarPergunta(pergunta)
            }
        }
    }

    private fun enviarPergunta(texto: String) {
        lifecycleScope.launch {
            try {
                progressIA.visibility = View.VISIBLE
                editPergunta.text.clear()
                txtRespostaIA.text = "A processar a tua pergunta..."

                val resposta = geminiRepository.perguntarIA(texto)
                
                if (resposta != null) {
                    txtRespostaIA.text = resposta
                } else {
                    txtRespostaIA.text = "Não consegui obter uma resposta. Tenta novamente."
                }
            } catch (e: Exception) {
                txtRespostaIA.text = "Erro na ligação: ${e.message}"
            } finally {
                progressIA.visibility = View.GONE
            }
        }
    }
}
