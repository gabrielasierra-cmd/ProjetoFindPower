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
import com.example.projetofindpower.controller.MovimentacaoController
import com.example.projetofindpower.model.TipoMovimentacao
import com.example.projetofindpower.network.ApiService
import com.example.projetofindpower.network.MovimentacaoExport
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class RelatoriosActivity : AppCompatActivity() {

    @Inject
    lateinit var controller: MovimentacaoController

    @Inject
    lateinit var apiService: ApiService

    private lateinit var pieChart: PieChart
    
    private val GOOGLE_SHEETS_SCRIPT_URL = "https://script.google.com/macros/s/AKfycbxi7RGRXgwtoo8d-6XJN7uYBnrIwWrVPbDoozPPt4-urcr-sphtLfZUr1HKqcw6liDP/exec"
    private val SPREADSHEET_VIEW_URL = "https://docs.google.com/spreadsheets/d/1mPO7zvq7dxvK3xTMVb2oDpSEDiZijMqfC6J8EbhFtiY/edit?usp=sharing"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_relatorios)

        configurarEdgeToEdge()
        inicializarUI()
    }

    override fun onResume() {
        super.onResume()
        atualizarGrafico()
    }

    private fun configurarEdgeToEdge() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun inicializarUI() {
        pieChart = findViewById(R.id.pieChart)
        
        findViewById<Button>(R.id.btnGerarRelatorio).setOnClickListener { atualizarGrafico() }
        
        findViewById<Button>(R.id.btnExportarPlanilha).setOnClickListener { btn ->
            btn.isEnabled = false
            exportarParaPlanilha(btn as Button) 
        }

        findViewById<Button>(R.id.btnAbrirHistoricoMensal).setOnClickListener {
            startActivity(Intent(this, HistoricoMensalActivity::class.java))
        }

        findViewById<Button>(R.id.btnAbrirHistoricoAnual).setOnClickListener {
            startActivity(Intent(this, HistoricoAnualActivity::class.java))
        }
    }

    private fun atualizarGrafico() {
        lifecycleScope.launch {
            try {
                val todas = controller.buscarTodas()
                if (todas.isEmpty()) {
                    exibirGraficoVazio("Nenhuma movimentação salva")
                    return@launch
                }

                val (receitasTotal, despesasTotal, saldo) = controller.calcularResumo(todas)

                val entries = mutableListOf<PieEntry>()
                if (receitasTotal > 0) entries.add(PieEntry(receitasTotal.toFloat(), "Receitas"))
                if (despesasTotal > 0) entries.add(PieEntry(despesasTotal.toFloat(), "Despesas"))

                if (entries.isEmpty()) {
                    exibirGraficoVazio("Dados com valor zero")
                    return@launch
                }

                configurarVisualGrafico(entries, receitasTotal, despesasTotal, saldo)

            } catch (e: Exception) {
                Log.e("RELATORIO", "Erro ao atualizar: ${e.message}")
                exibirGraficoVazio("Erro ao carregar dados")
            }
        }
    }

    private fun configurarVisualGrafico(entries: List<PieEntry>, receitas: Double, despesas: Double, saldo: Double) {
        val dataSet = PieDataSet(entries, "Resumo Financeiro")
        dataSet.colors = listOf(Color.parseColor("#4CAF50"), Color.parseColor("#F44336"))
        dataSet.sliceSpace = 5f
        dataSet.valueTextColor = Color.WHITE
        dataSet.valueTextSize = 16f

        pieChart.data = PieData(dataSet)
        pieChart.description.isEnabled = false
        pieChart.setUsePercentValues(true)
        pieChart.animateY(1000)

        val corSaldo = if (saldo >= 0) "#1B5E20" else "#B71C1C"
        pieChart.centerText = "BALANÇO\n\nReceitas: €${String.format("%.2f", receitas)}\nDespesas: €${String.format("%.2f", despesas)}\nSaldo: €${String.format("%.2f", saldo)}"
        pieChart.setCenterTextColor(Color.parseColor(corSaldo))
        pieChart.invalidate()
    }

    private fun exibirGraficoVazio(msg: String) {
        pieChart.setNoDataText(msg)
        pieChart.clear()
        pieChart.invalidate()
    }

    private fun exportarParaPlanilha(button: Button) {
        lifecycleScope.launch {
            try {
                val movimentacoes = controller.buscarTodas()
                if (movimentacoes.isEmpty()) {
                    Toast.makeText(this@RelatoriosActivity, "Sem dados", Toast.LENGTH_SHORT).show()
                    button.isEnabled = true
                    return@launch
                }

                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val dadosParaExportar = movimentacoes.map { mov ->
                    val prefixo = if (mov.tipo == TipoMovimentacao.RECEITA) "+" else "-"
                    MovimentacaoExport(
                        data = try { sdf.format(Date(mov.data.toLong())) } catch (e: Exception) { mov.data },
                        categoria = mov.categoria.name,
                        descricao = mov.descricao,
                        valor = "$prefixo €${String.format("%.2f", mov.valor)}",
                        modo = mov.modoPagamento,
                        status = mov.statusPagamento.name,
                        tipo = mov.tipo.name
                    )
                }.toMutableList()

                val response = apiService.enviarParaPlanilha(GOOGLE_SHEETS_SCRIPT_URL, dadosParaExportar)
                if (response.isSuccessful) mostrarPopupSucesso()

            } catch (e: Exception) {
                Toast.makeText(this@RelatoriosActivity, "Erro: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                button.isEnabled = true
            }
        }
    }

    private fun mostrarPopupSucesso() {
        AlertDialog.Builder(this)
            .setTitle("Sucesso!")
            .setMessage("Relatório exportado para o Google Sheets.\nSe não conseguir abrir a planilha, utilize o botão abaixo para copiar o link.")
            .setPositiveButton("Abrir Planilha") { _, _ ->
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(SPREADSHEET_VIEW_URL))
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(this, "Não foi possível abrir o navegador. Copie o link.", Toast.LENGTH_LONG).show()
                }
            }
            .setNeutralButton("Copiar Link") { _, _ ->
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Link Planilha", SPREADSHEET_VIEW_URL)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(this, "Link copiado para a área de transferência!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Fechar", null)
            .show()
    }
}
