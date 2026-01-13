package com.example.projetofindpower

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projetofindpower.adapter.DespesaAdapter
import com.example.projetofindpower.repository.AuthRepository
import com.example.projetofindpower.repository.DespesaRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class HistoricoAnualActivity : AppCompatActivity() {

    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var expenseRepository: DespesaRepository

    private lateinit var spinnerAno: Spinner
    private lateinit var btnFiltrar: Button
    private lateinit var txtTotalAno: TextView
    private lateinit var recyclerDespesas: RecyclerView
    private lateinit var adapter: DespesaAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historico_anual)

        spinnerAno = findViewById(R.id.spinnerAno)
        btnFiltrar = findViewById(R.id.btnFiltrar)
        txtTotalAno = findViewById(R.id.txtTotalAno)
        recyclerDespesas = findViewById(R.id.recyclerDespesas)

        configurarSpinnerAno()
        configurarRecycler()

        btnFiltrar.setOnClickListener { executarFiltro() }
        executarFiltro()
    }

    private fun configurarSpinnerAno() {
        val anoAtual = Calendar.getInstance().get(Calendar.YEAR)
        val anos = (anoAtual - 5..anoAtual + 2).toList().map { it.toString() }
        val adapterAno = ArrayAdapter(this, android.R.layout.simple_spinner_item, anos)
        adapterAno.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerAno.adapter = adapterAno
        spinnerAno.setSelection(5) 
    }

    private fun configurarRecycler() {
        adapter = DespesaAdapter(emptyList())
        recyclerDespesas.layoutManager = LinearLayoutManager(this)
        recyclerDespesas.adapter = adapter
    }

    private fun executarFiltro() {
        val userId = authRepository.getCurrentUser()?.uid ?: return
        val anoSelecionado = spinnerAno.selectedItem.toString().toInt()

        lifecycleScope.launch {
            try {
                // Buscamos todas e filtramos por ano no repositório (vou usar uma lógica similar ao mês)
                val todas = expenseRepository.getExpensesByUser(userId)
                val filtradas = todas.filter { despesa ->
                    val cal = Calendar.getInstance()
                    try {
                        cal.timeInMillis = despesa.data.toLong()
                        cal.get(Calendar.YEAR) == anoSelecionado
                    } catch (e: Exception) { false }
                }

                adapter.atualizarLista(filtradas)
                val total = filtradas.sumOf { it.valor }
                txtTotalAno.text = "Total no ano: € ${String.format("%.2f", total)}"
            } catch (e: Exception) {
                Toast.makeText(this@HistoricoAnualActivity, "Erro: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
