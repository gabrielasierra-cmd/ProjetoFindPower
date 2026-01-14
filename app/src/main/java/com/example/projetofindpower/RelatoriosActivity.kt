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
    lateinit var controller: MovimentacaoController // Usando o Controller

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
        findViewById<Button>(R.id.btnAbrirHistoricoMensal).setOnClickListener { 
            startActivity(Intent(this, HistoricoMensalActivity::class.java)) 
        }
        findViewById<Button>(R.id.btnAbrirHistoricoAnual).setOnClickListener { 
            startActivity(Intent(this, HistoricoAnualActivity::class.java)) 
        }
        findViewById<Button>(R.id.btnExportarPlanilha).setOnClickListener { btn ->
            btn.isEnabled = false
            exportarParaPlanilha(btn as Button) 
        }
    }

    private fun atualizarGrafico() {
        lifecycleScope.launch {
            try {
                // O Controller já nos entrega apenas os dados do mês atual filtrados
                val listaMes = controller.buscarDadosMesAtual()
                
                if (listaMes.isEmpty()) {
                    exibirGraficoVazio()
                    return@launch
                }

                // Usamos o Controller para calcular o resumo financeiro
                val (receitasTotal, despesasTotal, saldo) = controller.calcularResumo(listaMes)

                // Montamos as fatias do gráfico por categoria (apenas despesas)
                val entries = listaMes.filter { it.natureza == "Despesa" }
                    .groupBy { it.tipo }
                    .mapValues { it.value.sumOf { d -> d.valor }.toFloat() }
                    .filter { it.value > 0 }
                    .map { PieEntry(it.value, it.key) }

                if (entries.isEmpty()) {
                    exibirGraficoVazio("Nenhuma despesa neste mês")
                    return@launch
                }

                configurarVisualGrafico(entries, receitasTotal, despesasTotal, saldo)

            } catch (e: Exception) {
                Log.e("RELATORIO", "Erro: ${e.message}")
            }
        }
    }

    private fun configurarVisualGrafico(entries: List<PieEntry>, receitas: Double, despesas: Double, saldo: Double) {
        val dataSet = PieDataSet(entries, "")
        dataSet.colors = listOf(
            Color.parseColor("#1A237E"), Color.parseColor("#2E7D32"), 
            Color.parseColor("#E65100"), Color.parseColor("#4A148C"),
            Color.parseColor("#006064"), Color.parseColor("#BF360C")
        )
        dataSet.sliceSpace = 4f
        dataSet.valueTextColor = Color.WHITE
        dataSet.valueTextSize = 13f

        pieChart.data = PieData(dataSet)
        pieChart.description.isEnabled = false
        pieChart.isDrawHoleEnabled = true
        pieChart.setHoleColor(Color.WHITE)
        pieChart.animateXY(800, 800)

        val mesNome = obterNomeMesAtual()
        val corSaldo = if (saldo >= 0) "#1B5E20" else "#B71C1C"
        
        pieChart.centerText = "RESUMO $mesNome\n\n" +
                             "Receitas: €${String.format("%.2f", receitas)}\n" +
                             "Despesas: €${String.format("%.2f", despesas)}\n" +
                             "Saldo: €${String.format("%.2f", saldo)}"
        
        pieChart.setCenterTextColor(Color.parseColor(corSaldo))
        pieChart.setCenterTextSize(14f)
        pieChart.invalidate()
    }

    private fun exibirGraficoVazio(msg: String = "Sem dados para este mês") {
        pieChart.setNoDataText(msg)
        pieChart.clear()
        pieChart.invalidate()
    }

    private fun obterNomeMesAtual(): String {
        val cal = Calendar.getInstance()
        return arrayOf("JANEIRO", "FEVEREIRO", "MARÇO", "ABRIL", "MAIO", "JUNHO", "JULHO", "AGOSTO", "SETEMBRO", "OUTUBRO", "NOVEMBRO", "DEZEMBRO")[cal.get(Calendar.MONTH)]
    }

    private fun exportarParaPlanilha(button: Button) {
        lifecycleScope.launch {
            try {
                // Buscamos todas via Controller
                val movimentacoes = controller.buscarTodas()
                if (movimentacoes.isEmpty()) {
                    Toast.makeText(this@RelatoriosActivity, "Sem dados", Toast.LENGTH_SHORT).show()
                    button.isEnabled = true
                    return@launch
                }

                val (totalReceitas, totalDespesas, saldoGeral) = controller.calcularResumo(movimentacoes)
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

                dadosParaExportar.add(MovimentacaoExport("---", "---", "SALDO TOTAL", "€ ${String.format("%.2f", saldoGeral)}".replace(".", ","), "---", "---", "TOTAL"))

                val response = apiService.enviarParaPlanilha(GOOGLE_SHEETS_SCRIPT_URL, dadosParaExportar)

                if (response.isSuccessful) {
                    mostrarPopupSucesso()
                }
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
            .setMessage("Relatório exportado para o Google Sheets.")
            .setPositiveButton("Abrir Planilha") { _, _ ->
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(SPREADSHEET_VIEW_URL)))
            }
            .setNegativeButton("Fechar", null)
            .show()
    }
}
