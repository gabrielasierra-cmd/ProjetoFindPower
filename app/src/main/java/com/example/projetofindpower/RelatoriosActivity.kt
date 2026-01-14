package com.example.projetofindpower

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.projetofindpower.network.ApiService
import com.example.projetofindpower.network.MovimentacaoExport
import com.example.projetofindpower.repository.AuthRepository
import com.example.projetofindpower.repository.MovimentacaoRepository
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class RelatoriosActivity : AppCompatActivity() {

    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var movimentacaoRepository: MovimentacaoRepository

    @Inject
    lateinit var apiService: ApiService

    private lateinit var pieChart: PieChart
    
    private val GOOGLE_SHEETS_SCRIPT_URL = "https://script.google.com/macros/s/AKfycbxi7RGRXgwtoo8d-6XJN7uYBnrIwWrVPbDoozPPt4-urcr-sphtLfZUr1HKqcw6liDP/exec"
    private val SPREADSHEET_VIEW_URL = "https://docs.google.com/spreadsheets/d/1mPO7zvq7dxvK3xTMVb2oDpSEDiZijMqfC6J8EbhFtiY/edit?usp=sharing"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_relatorios)

        val mainView = findViewById<android.view.View>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        pieChart = findViewById(R.id.pieChart)
        val btnGerarRelatorio = findViewById<Button>(R.id.btnGerarRelatorio)
        val btnAbrirHistoricoMensal = findViewById<Button>(R.id.btnAbrirHistoricoMensal)
        val btnAbrirHistoricoAnual = findViewById<Button>(R.id.btnAbrirHistoricoAnual)
        val btnExportarPlanilha = findViewById<Button>(R.id.btnExportarPlanilha)

        atualizarGrafico()

        btnGerarRelatorio.setOnClickListener { atualizarGrafico() }
        
        btnAbrirHistoricoMensal.setOnClickListener { 
            startActivity(Intent(this, HistoricoMensalActivity::class.java)) 
        }
        
        btnAbrirHistoricoAnual.setOnClickListener { 
            startActivity(Intent(this, HistoricoAnualActivity::class.java)) 
        }

        btnExportarPlanilha.setOnClickListener { 
            btnExportarPlanilha.isEnabled = false
            exportarParaPlanilha(btnExportarPlanilha) 
        }
    }

    private fun atualizarGrafico() {
        val currentUser = authRepository.getCurrentUser() ?: return
        lifecycleScope.launch {
            try {
                val lista = movimentacaoRepository.getMovimentacoesByUser(currentUser.uid)
                if (lista.isEmpty()) {
                    pieChart.setNoDataText("Sem dados")
                    pieChart.invalidate()
                    return@launch
                }

                val receitasTotal = lista.filter { it.natureza == "Receita" }.sumOf { it.valor }
                val despesasTotal = lista.filter { it.natureza == "Despesa" }.sumOf { it.valor }
                val saldo = receitasTotal - despesasTotal

                val entries = lista.filter { it.natureza == "Despesa" }
                    .groupBy { it.tipo }
                    .mapValues { it.value.sumOf { d -> d.valor }.toFloat() }
                    .map { PieEntry(it.value, it.key) }

                val dataSet = PieDataSet(entries, "")
                dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()
                
                pieChart.data = PieData(dataSet)
                val corSaldo = if (saldo >= 0) "#004D40" else "#D32F2F"
                pieChart.centerText = "Receitas: €${String.format("%.2f", receitasTotal)}\n" +
                                     "Despesas: €${String.format("%.2f", despesasTotal)}\n" +
                                     "Saldo: €${String.format("%.2f", saldo)}"
                pieChart.setCenterTextColor(Color.parseColor(corSaldo))
                pieChart.invalidate()
            } catch (e: Exception) {
                Log.e("RELATORIO", "Erro: ${e.message}")
            }
        }
    }

    private fun exportarParaPlanilha(button: Button) {
        val userId = authRepository.getCurrentUser()?.uid ?: return
        lifecycleScope.launch {
            try {
                val movimentacoes = movimentacaoRepository.getMovimentacoesByUser(userId)
                if (movimentacoes.isEmpty()) {
                    Toast.makeText(this@RelatoriosActivity, "Sem dados", Toast.LENGTH_SHORT).show()
                    button.isEnabled = true
                    return@launch
                }

                val totalReceitas = movimentacoes.filter { it.natureza == "Receita" }.sumOf { it.valor }
                val totalDespesas = movimentacoes.filter { it.natureza == "Despesa" }.sumOf { it.valor }
                val saldoGeral = totalReceitas - totalDespesas

                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                
                val dadosParaExportar = movimentacoes.map { mov ->
                    val prefixo = if (mov.natureza == "Receita") "+" else "-"
                    val valorFormatado = "$prefixo ${String.format("%.2f", mov.valor)}".replace(".", ",")

                    MovimentacaoExport(
                        data = try { sdf.format(Date(mov.data.toLong())) } catch (e: Exception) { mov.data },
                        categoria = mov.tipo,
                        descricao = mov.descricao,
                        valor = valorFormatado,
                        modo = mov.modoPagamento,
                        status = mov.statusPagamento,
                        tipo = mov.natureza
                    )
                }.toMutableList()

                // Linha de Saldo
                dadosParaExportar.add(
                    MovimentacaoExport(
                        data = "---",
                        categoria = "---",
                        descricao = "SALDO ATUAL",
                        valor = "€ ${String.format("%.2f", saldoGeral)}".replace(".", ","),
                        modo = "---",
                        status = "---",
                        tipo = if (saldoGeral >= 0) "POSITIVO" else "NEGATIVO"
                    )
                )

                val response = apiService.enviarParaPlanilha(GOOGLE_SHEETS_SCRIPT_URL, dadosParaExportar)

                if (response.isSuccessful) {
                    runOnUiThread {
                        val builder = AlertDialog.Builder(this@RelatoriosActivity)
                        builder.setTitle("Sucesso!")
                        builder.setMessage("Relatório exportado com sucesso.\n\nCaso a planilha não abra automaticamente, deseja copiar o link?")
                        
                        builder.setPositiveButton("Abrir") { _, _ ->
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(SPREADSHEET_VIEW_URL))
                            startActivity(intent)
                        }
                        
                        builder.setNeutralButton("Copiar Link") { _, _ ->
                            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Link Planilha", SPREADSHEET_VIEW_URL)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(this@RelatoriosActivity, "Link copiado!", Toast.LENGTH_SHORT).show()
                        }
                        
                        builder.setNegativeButton("Fechar", null)
                        
                        builder.show()
                    }
                }
            } catch (e: Exception) {
                Log.e("PLANILHA", "Erro: ${e.message}")
                Toast.makeText(this@RelatoriosActivity, "Erro ao exportar: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                button.isEnabled = true
            }
        }
    }
}
