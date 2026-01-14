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
import com.example.projetofindpower.adapter.MovimentacaoAdapter
import com.example.projetofindpower.repository.AuthRepository
import com.example.projetofindpower.repository.MovimentacaoRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class HistoricoAnualActivity : AppCompatActivity() {

    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var movimentacaoRepository: MovimentacaoRepository

    private lateinit var spinnerAno: Spinner
    private lateinit var btnFiltrar: Button
    private lateinit var txtTotalAno: TextView
    private lateinit var recyclerMovimentacoes: RecyclerView
    private lateinit var adapter: MovimentacaoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historico_anual)

        spinnerAno = findViewById(R.id.spinnerAno)
        btnFiltrar = findViewById(R.id.btnFiltrar)
        txtTotalAno = findViewById(R.id.txtTotalAno)
        recyclerMovimentacoes = findViewById(R.id.recyclerDespesas)

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
        adapter = MovimentacaoAdapter(emptyList())
        recyclerMovimentacoes.layoutManager = LinearLayoutManager(this)
        recyclerMovimentacoes.adapter = adapter
    }

    private fun executarFiltro() {
        val userId = authRepository.getCurrentUser()?.uid ?: return
        val anoSelecionado = spinnerAno.selectedItem.toString().toInt()

        lifecycleScope.launch {
            try {
                val todas = movimentacaoRepository.getMovimentacoesByUser(userId)
                val filtradas = todas.filter { mov ->
                    val cal = Calendar.getInstance()
                    try {
                        cal.timeInMillis = mov.data.toLong()
                        cal.get(Calendar.YEAR) == anoSelecionado
                    } catch (e: Exception) { false }
                }

                adapter.atualizarLista(filtradas)
                
                val receitasTotal = filtradas.filter { it.natureza == "Receita" }.sumOf { it.valor }
                val despesasTotal = filtradas.filter { it.natureza == "Despesa" }.sumOf { it.valor }
                val saldoAnual = receitasTotal - despesasTotal

                txtTotalAno.text = "Saldo Anual: € ${String.format("%.2f", saldoAnual)}"
            } catch (e: Exception) {
                Toast.makeText(this@HistoricoAnualActivity, "Erro: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
