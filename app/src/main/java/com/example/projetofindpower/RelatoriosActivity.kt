package com.example.projetofindpower

import android.graphics.Color
import android.os.Bundle
import android.widget.Button // CORRIGIDO: Import correto para XML
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.projetofindpower.repository.AuthRepository
import com.example.projetofindpower.repository.DespesaRepository
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class RelatoriosActivity : AppCompatActivity() {

    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var expenseRepository: DespesaRepository

    private lateinit var pieChart: PieChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_relatorios)

        // Configuração para ajuste de barras do sistema (status bar/navigation bar)
        val mainView = findViewById<android.view.View>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Referência do Gráfico
        pieChart = findViewById(R.id.pieChart)

        // CORREÇÃO AQUI: Referência correta do botão que está no XML
        val btnGerarRelatorio = findViewById<Button>(R.id.btnGerarRelatorio)

        // Configura o texto de "Sem dados" caso o Firebase demore ou esteja vazio
        pieChart.setNoDataText("Carregando dados...")
        pieChart.setNoDataTextColor(Color.GRAY)

        // Carregar dados reais ao abrir a tela
        atualizarGrafico()

        btnGerarRelatorio.setOnClickListener {
            atualizarGrafico()
        }
    }

    private fun atualizarGrafico() {
        val userId = authRepository.getCurrentUser()?.uid ?: return

        lifecycleScope.launch {
            try {
                val listaDespesas = expenseRepository.getExpensesByUser(userId)

                if (listaDespesas.isEmpty()) {
                    pieChart.setNoDataText("Nenhuma despesa cadastrada.")
                    pieChart.data = null
                    pieChart.invalidate()
                    Toast.makeText(this@RelatoriosActivity, "Sem despesas para exibir", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                // 2. Agrupar os dados por categoria e somar
                val dadosAgrupados = listaDespesas.groupBy { it.tipo }
                    .mapValues { entry -> entry.value.sumOf { it.valor }.toFloat() }

                // 3. Criar entradas
                val entries = dadosAgrupados.map { (categoria, total) ->
                    PieEntry(total, categoria)
                }

                // 4. Configurar conjunto de dados
                val dataSet = PieDataSet(entries, "") // Título vazio para ficar mais limpo
                dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()
                dataSet.valueTextColor = Color.WHITE
                dataSet.valueTextSize = 14f
                dataSet.sliceSpace = 2f

                // 5. Aplicar e formatar gráfico
                val data = PieData(dataSet)
                pieChart.data = data
                pieChart.description.isEnabled = false
                pieChart.setUsePercentValues(true)
                pieChart.setEntryLabelColor(Color.BLACK)
                pieChart.setEntryLabelTextSize(12f)

                // Texto no centro do gráfico
                val totalGeral = listaDespesas.sumOf { it.valor }
                pieChart.centerText = "Total Geral\n€ ${String.format("%.2f", totalGeral)}"
                pieChart.setCenterTextSize(16f)

                // Animação e Refresh
                pieChart.animateY(1000)
                pieChart.invalidate()

                Toast.makeText(this@RelatoriosActivity, "Relatório atualizado! ✅", Toast.LENGTH_SHORT).show()

            } catch (e: Exception) {
                Toast.makeText(this@RelatoriosActivity, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
