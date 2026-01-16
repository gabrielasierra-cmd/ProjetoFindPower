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
import com.github.mikephil.charting.utils.ColorTemplate
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

    private val TAG = "RELATORIO_ERRO"
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
                val despesas = todas.filter { it.tipo == TipoMovimentacao.DESPESA }

                if (despesas.isEmpty()) {
                    exibirGraficoVazio("Nenhuma despesa registada")
                    return@launch
                }

                // Agrupar por categoria e somar os valores
                val despesasPorCategoria = despesas.groupBy { it.categoria }
                    .mapValues { entry -> entry.value.sumOf { it.valor } }

                val entries = despesasPorCategoria.map { (categoria, total) ->
                    PieEntry(total.toFloat(), categoria.name)
                }

                val totalDespesas = despesas.sumOf { it.valor }
                configurarVisualGrafico(entries, totalDespesas)
                
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao atualizar gráfico: ${e.message}", e)
            }
        }
    }

    private fun configurarVisualGrafico(entries: List<PieEntry>, totalDespesas: Double) {
        val dataSet = PieDataSet(entries, "Despesas por Categoria")
        
        // Usar uma paleta de cores alegre e variada
        val colors = mutableListOf<Int>()
        for (c in ColorTemplate.VORDIPLOM_COLORS) colors.add(c)
        for (c in ColorTemplate.JOYFUL_COLORS) colors.add(c)
        for (c in ColorTemplate.COLORFUL_COLORS) colors.add(c)
        dataSet.colors = colors
        
        dataSet.valueTextSize = 14f
        dataSet.valueTextColor = Color.BLACK

        pieChart.data = PieData(dataSet)
        pieChart.description.isEnabled = false
        pieChart.animateY(1000)
        
        // Centralizar o total de despesas
        pieChart.centerText = "Total Despesas:\n€${String.format("%.2f", totalDespesas)}"
        pieChart.setCenterTextSize(16f)
        
        // Configurações extras de legibilidade
        pieChart.legend.isEnabled = true
        pieChart.legend.isWordWrapEnabled = true
        pieChart.setEntryLabelColor(Color.BLACK)
        
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
                if (response.isSuccessful) {
                    mostrarPopupSucesso()
                } else {
                    Log.e(TAG, "Erro ao exportar: ${response.code()} | ${response.errorBody()?.string()}")
                    Toast.makeText(this@RelatoriosActivity, "Erro ao exportar planilha", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exceção na exportação: ${e.message}", e)
                Toast.makeText(this@RelatoriosActivity, "Erro de ligação", Toast.LENGTH_SHORT).show()
            } finally {
                button.isEnabled = true
            }
        }
    }

    private fun mostrarPopupSucesso() {
        AlertDialog.Builder(this)
            .setTitle("Sucesso!")
            .setMessage("Relatório exportado para o Google Sheets.")
            .setPositiveButton("Abrir") { _, _ ->
                try {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(SPREADSHEET_VIEW_URL)))
                } catch (e: Exception) {
                    Log.e(TAG, "Erro ao abrir link da planilha", e)
                }
            }
            .setNeutralButton("Copiar Link") { _, _ ->
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("Link", SPREADSHEET_VIEW_URL))
                Toast.makeText(this, "Link cobiado!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Fechar", null)
            .show()
    }
}
