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
        findViewById<LinearLayout>(R.id.btnLazer).setOnClickListener { buscarEFiltar("Lazer") }
        findViewById<LinearLayout>(R.id.btnEmergencia).setOnClickListener { buscarEFiltar("Emergência") }
        findViewById<LinearLayout>(R.id.btnContasFixas).setOnClickListener { buscarEFiltar("Contas Fixas") }
        findViewById<LinearLayout>(R.id.btnPoupanca).setOnClickListener { buscarEFiltar("Poupança") }
        findViewById<LinearLayout>(R.id.btnExtras).setOnClickListener { buscarEFiltar("Extras") }
        findViewById<LinearLayout>(R.id.btnViagens).setOnClickListener { buscarEFiltar("Viagens") }
        findViewById<LinearLayout>(R.id.btnTodasDespesas).setOnClickListener { buscarTodas() }
    }

    private fun buscarTodas() {
        val userId = authRepository.getCurrentUser()?.uid ?: run {
            Toast.makeText(this, "Usuário não autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val listaDespesas = expenseRepository.getExpensesByUser(userId)
                exibirPopupDetalhado("Todas as Despesas", listaDespesas)
            } catch (e: Exception) {
                Toast.makeText(this@CategoriasActivity, "Erro: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun buscarEFiltar(categoria: String) {
        val userId = authRepository.getCurrentUser()?.uid ?: return

        lifecycleScope.launch {
            try {
                // REFACTOR: Chamando o filtro direto do repositório
                val filtradas = expenseRepository.getExpensesByCategory(userId, categoria)
                exibirPopupDetalhado(categoria, filtradas)
            } catch (e: Exception) {
                Toast.makeText(this@CategoriasActivity, "Erro ao carregar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun exibirPopupDetalhado(titulo: String, lista: List<Despesa>) {
        val total = lista.sumOf { it.valor }
        val sdfMes = SimpleDateFormat("MMMM / yyyy", Locale("pt", "PT"))

        val builder = AlertDialog.Builder(this)
        builder.setTitle(titulo)

        if (lista.isEmpty()) {
            builder.setMessage("Nenhuma despesa encontrada.")
        } else {
            val corpoTexto = StringBuilder()
            corpoTexto.append("💰 TOTAL: €${String.format("%.2f", total)}\n")
            corpoTexto.append("━━━━━━━━━━━━━━━━━━━━\n\n")

            lista.forEach { despesa ->
                val mesFormatado = try {
                    sdfMes.format(Date(despesa.data.toLong()))
                } catch (e: Exception) {
                    despesa.data
                }

                corpoTexto.append("📅 Data: $mesFormatado\n")
                corpoTexto.append("📝 Categoria: ${despesa.tipo}\n")
                corpoTexto.append("📝 Descrição: ${despesa.descricao}\n")
                corpoTexto.append("💶 Valor: €${String.format("%.2f", despesa.valor)}\n")
                corpoTexto.append("────────────────────\n")
            }
            builder.setMessage(corpoTexto.toString())
        }

        builder.setPositiveButton("Fechar", null)
        builder.show()
    }
}
