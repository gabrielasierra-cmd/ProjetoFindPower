package com.example.projetofindpower

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.projetofindpower.model.Despesa
import com.example.projetofindpower.repository.AuthRepository
import com.example.projetofindpower.repository.DespesaRepository
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
    lateinit var expenseRepository: DespesaRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_categorias)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        // Vincula os cliques dos IDs do seu XML às categorias correspondentes
        findViewById<LinearLayout>(R.id.btnLazer).setOnClickListener { buscarEFiltar("Lazer") }
        findViewById<LinearLayout>(R.id.btnEmergencia).setOnClickListener { buscarEFiltar("Emergência") }
        findViewById<LinearLayout>(R.id.btnContasFixas).setOnClickListener { buscarEFiltar("Contas Fixas") }
        findViewById<LinearLayout>(R.id.btnPoupanca).setOnClickListener { buscarEFiltar("Poupança") }
        findViewById<LinearLayout>(R.id.btnExtras).setOnClickListener { buscarEFiltar("Extras") }
        findViewById<LinearLayout>(R.id.btnViagens).setOnClickListener { buscarEFiltar("Viagens") }
    }

    private fun buscarEFiltar(categoria: String) {
        val userId = authRepository.getCurrentUser()?.uid ?: return

        lifecycleScope.launch {
            try {
                // Busca despesas do Firebase através do Repositório
                val todasDespesas = expenseRepository.getExpensesByUser(userId)

                // Filtra as despesas pela categoria clicada
                val filtradas = todasDespesas.filter { it.tipo == categoria }

                exibirPopupDetalhado(categoria, filtradas)
            } catch (e: Exception) {
                Toast.makeText(this@CategoriasActivity, "Erro ao carregar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun exibirPopupDetalhado(categoria: String, lista: List<Despesa>) {
        val total = lista.sumOf { it.valor }

        // Formata para exibir o mês por extenso (ex: Janeiro / 2026)
        val sdfMes = SimpleDateFormat("MMMM / yyyy", Locale("pt", "PT"))

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Resumo: $categoria")

        if (lista.isEmpty()) {
            builder.setMessage("Nenhuma despesa encontrada para esta categoria.")
        } else {
            val corpoTexto = StringBuilder()
            corpoTexto.append("💰 TOTAL ACUMULADO: €${String.format("%.2f", total)}\n")
            corpoTexto.append("━━━━━━━━━━━━━━━━━━━━\n\n")

            lista.forEach { despesa ->
                // Tenta formatar a data que está salva como String/Timestamp
                val mesFormatado = try {
                    sdfMes.format(Date(despesa.data.toLong()))
                } catch (e: Exception) {
                    despesa.data
                }

                corpoTexto.append("📅 Mês: $mesFormatado\n")
                corpoTexto.append("📝 Descrição: ${despesa.descricao}\n")
                corpoTexto.append("💶 Valor: €${String.format("%.2f", despesa.valor)}\n")
                corpoTexto.append("💳 Modo: ${despesa.modoPagamento}\n")
                corpoTexto.append("📌 Status: ${despesa.statusPagamento}\n")
                corpoTexto.append("────────────────────\n")
            }
            builder.setMessage(corpoTexto.toString())
        }

        builder.setPositiveButton("Fechar", null)
        builder.show()
    }
}
