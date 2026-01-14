package com.example.projetofindpower

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.projetofindpower.model.Movimentacao
import com.example.projetofindpower.repository.AuthRepository
import com.example.projetofindpower.repository.MovimentacaoRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class CategoriasActivity : AppCompatActivity() {

    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var movimentacaoRepository: MovimentacaoRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_categorias)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        // Categorias de Despesa
        findViewById<LinearLayout>(R.id.btnLazer).setOnClickListener { buscarEFiltar("Lazer") }
        findViewById<LinearLayout>(R.id.btnEmergencia).setOnClickListener { buscarEFiltar("Emergência") }
        findViewById<LinearLayout>(R.id.btnContasFixas).setOnClickListener { buscarEFiltar("Contas Fixas") }
        findViewById<LinearLayout>(R.id.btnPoupanca).setOnClickListener { buscarEFiltar("Poupança") }
        findViewById<LinearLayout>(R.id.btnExtras).setOnClickListener { buscarEFiltar("Extras") }
        findViewById<LinearLayout>(R.id.btnViagens).setOnClickListener { buscarEFiltar("Viagens") }
        
        // Botão Geral
        findViewById<LinearLayout>(R.id.btnTodasDespesas).setOnClickListener { buscarTodas() }
    }

    private fun buscarTodas() {
        val userId = authRepository.getCurrentUser()?.uid ?: run {
            Toast.makeText(this, "Usuário não autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val lista = movimentacaoRepository.getMovimentacoesByUser(userId)
                exibirPopupDetalhado("Todas as Movimentações", lista)
            } catch (e: Exception) {
                Toast.makeText(this@CategoriasActivity, "Erro: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun buscarEFiltar(categoria: String) {
        val userId = authRepository.getCurrentUser()?.uid ?: return

        lifecycleScope.launch {
            try {
                val filtradas = movimentacaoRepository.getMovimentacoesByCategory(userId, categoria)
                exibirPopupDetalhado(categoria, filtradas)
            } catch (e: Exception) {
                Toast.makeText(this@CategoriasActivity, "Erro ao carregar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun exibirPopupDetalhado(titulo: String, lista: List<Movimentacao>) {
        // Calcula o saldo das movimentações exibidas
        val receitas = lista.filter { it.natureza == "Receita" }.sumOf { it.valor }
        val despesas = lista.filter { it.natureza == "Despesa" }.sumOf { it.valor }
        val saldo = receitas - despesas

        val sdfMes = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "PT"))

        val builder = AlertDialog.Builder(this)
        builder.setTitle(titulo)

        if (lista.isEmpty()) {
            builder.setMessage("Nenhuma movimentação encontrada.")
        } else {
            val corpoTexto = StringBuilder()
            corpoTexto.append("📊 RESUMO: €${String.format("%.2f", saldo)}\n")
            corpoTexto.append("━━━━━━━━━━━━━━━━━━━━\n\n")

            lista.forEach { mov ->
                val dataFormatada = try {
                    sdfMes.format(Date(mov.data.toLong()))
                } catch (e: Exception) {
                    mov.data
                }

                val prefixo = if (mov.natureza == "Receita") "🟢 +" else "🔴 -"
                
                corpoTexto.append("$prefixo €${String.format("%.2f", mov.valor)}\n")
                corpoTexto.append("📅 Data: $dataFormatada\n")
                corpoTexto.append("📝 Descrição: ${mov.descricao}\n")
                corpoTexto.append("────────────────────\n")
            }
            builder.setMessage(corpoTexto.toString())
        }

        builder.setPositiveButton("Fechar", null)
        builder.show()
    }
}
