package com.example.projetofindpower

import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate

class RelatoriosActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_relatorios)

        // ✅ Ajuste das barras do sistema
        val mainView = findViewById<android.view.View>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // ✅ Configurar o PieChart
        val pieChart = findViewById<PieChart>(R.id.pieChart)

        val entries = listOf(
            PieEntry(35f, "Lazer"),
            PieEntry(20f, "Poupança"),
            PieEntry(15f, "Contas Fixas"),
            PieEntry(10f, "Extras"),
            PieEntry(20f, "Viagens")
        )

        val dataSet = PieDataSet(entries, "Categorias")
        dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()
        dataSet.valueTextColor = Color.WHITE
        dataSet.valueTextSize = 14f

        val data = PieData(dataSet)

        pieChart.data = data
        pieChart.description.isEnabled = false
        pieChart.setUsePercentValues(true)
        pieChart.setEntryLabelColor(Color.BLACK)
        pieChart.setEntryLabelTextSize(12f)
        pieChart.centerText = "Despesas"
        pieChart.setCenterTextSize(18f)
        pieChart.animateY(1000)

        pieChart.invalidate()

        // ✅ Botão "Gerar Relatório"
        val btnGerarRelatorio = findViewById<Button>(R.id.btnGerarRelatorio)
        btnGerarRelatorio.setOnClickListener {
            Toast.makeText(this, "Relatório gerado com sucesso ✅", Toast.LENGTH_SHORT).show()
        }
    }
}
