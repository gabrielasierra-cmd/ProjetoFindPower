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
import com.example.projetofindpower.network.DespesaExport
import com.example.projetofindpower.repository.AuthRepository
import com.example.projetofindpower.repository.DespesaRepository
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
    lateinit var expenseRepository: DespesaRepository

    @Inject
    lateinit var apiService: ApiService

    private lateinit var pieChart: PieChart
    
    private val GOOGLE_SHEETS_SCRIPT_URL = "https://script.google.com/macros/s/AKfycbxi7RGRXgwtoo8d-6XJN7uYBnrIwWrVPbDoozPPt4-urcr-sphtLfZUr1HKqcw6liDP/exec"
    
    // URL PÚBLICA DA PLANILHA
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
        btnAbrirHistoricoMensal.setOnClickListener { startActivity(Intent(this, HistoricoMensalActivity::class.java)) }
        btnAbrirHistoricoAnual.setOnClickListener { startActivity(Intent(this, HistoricoAnualActivity::class.java)) }
        
        btnExportarPlanilha.setOnClickListener { 
            btnExportarPlanilha.isEnabled = false
            exportarParaPlanilha(btnExportarPlanilha) 
        }
    }

    private fun exportarParaPlanilha(button: Button) {
        val userId = authRepository.getCurrentUser()?.uid ?: run {
            button.isEnabled = true
            return
        }

        lifecycleScope.launch {
            try {
                Log.d("PLANILHA", "Buscando despesas...")
                val despesas = expenseRepository.getExpensesByUser(userId)
                
                if (despesas.isEmpty()) {
                    Toast.makeText(this@RelatoriosActivity, "Sem dados para exportar", Toast.LENGTH_SHORT).show()
                    button.isEnabled = true
                    return@launch
                }

                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val dadosFormatados = despesas.map { despesa ->
                    val dataFormatada = try {
                        sdf.format(Date(despesa.data.toLong()))
                    } catch (e: Exception) { despesa.data }

                    DespesaExport(
                        data = dataFormatada,
                        categoria = despesa.tipo,
                        descricao = despesa.descricao,
                        valor = String.format("%.2f", despesa.valor).replace(".", ","),
                        modo = despesa.modoPagamento,
                        status = despesa.statusPagamento
                    )
                }

                Log.d("PLANILHA", "Enviando para o Google...")
                val response = apiService.enviarParaPlanilha(GOOGLE_SHEETS_SCRIPT_URL, dadosFormatados)

                if (response.isSuccessful) {
                    runOnUiThread {
                        val builder = AlertDialog.Builder(this@RelatoriosActivity)
                        builder.setTitle("Sucesso!")
                        builder.setMessage("Dados enviados. O que deseja fazer?")
                        
                        builder.setPositiveButton("Abrir no Navegador") { _, _ ->
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
                } else {
                    Toast.makeText(this@RelatoriosActivity, "Erro no servidor: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("PLANILHA", "Erro: ${e.message}")
            } finally {
                button.isEnabled = true
            }
        }
    }

    private fun atualizarGrafico() {
        val currentUser = authRepository.getCurrentUser() ?: return
        lifecycleScope.launch {
            try {
                val listaDespesas = expenseRepository.getExpensesByUser(currentUser.uid)
                if (listaDespesas.isEmpty()) return@launch

                val entries = listaDespesas.groupBy { it.tipo }
                    .mapValues { it.value.sumOf { d -> d.valor }.toFloat() }
                    .map { PieEntry(it.value, it.key) }

                val dataSet = PieDataSet(entries, "")
                dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()
                
                pieChart.data = PieData(dataSet)
                pieChart.centerText = "Total\n€ ${String.format("%.2f", listaDespesas.sumOf { it.valor })}"
                pieChart.invalidate()
            } catch (e: Exception) { }
        }
    }
}
