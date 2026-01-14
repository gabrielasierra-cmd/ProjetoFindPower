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
class HistoricoMensalActivity : AppCompatActivity() {

    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var movimentacaoRepository: MovimentacaoRepository

    private lateinit var spinnerMes: Spinner
    private lateinit var spinnerAno: Spinner
    private lateinit var btnFiltrar: Button
    private lateinit var txtTotalMes: TextView
    private lateinit var recyclerMovimentacoes: RecyclerView
    private lateinit var adapter: MovimentacaoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historico_mensal)

        inicializarComponentes()
        configurarSpinners()
        configurarRecycler()

        btnFiltrar.setOnClickListener { executarFiltro() }
        executarFiltro()
    }

    private fun inicializarComponentes() {
        spinnerMes = findViewById(R.id.spinnerMes)
        spinnerAno = findViewById(R.id.spinnerAno)
        btnFiltrar = findViewById(R.id.btnFiltrar)
        txtTotalMes = findViewById(R.id.txtTotalMes)
        recyclerMovimentacoes = findViewById(R.id.recyclerDespesas) // Mantendo o ID por conveniência
    }

    private fun configurarSpinners() {
        val meses = arrayOf("Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho", "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro")
        val adapterMes = ArrayAdapter(this, android.R.layout.simple_spinner_item, meses)
        adapterMes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerMes.adapter = adapterMes

        val anoAtual = Calendar.getInstance().get(Calendar.YEAR)
        val anos = (anoAtual - 5..anoAtual + 2).toList().map { it.toString() }
        val adapterAno = ArrayAdapter(this, android.R.layout.simple_spinner_item, anos)
        adapterAno.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerAno.adapter = adapterAno

        spinnerMes.setSelection(Calendar.getInstance().get(Calendar.MONTH))
        spinnerAno.setSelection(5)
    }

    private fun configurarRecycler() {
        adapter = MovimentacaoAdapter(emptyList())
        recyclerMovimentacoes.layoutManager = LinearLayoutManager(this)
        recyclerMovimentacoes.adapter = adapter
    }

    private fun executarFiltro() {
        val userId = authRepository.getCurrentUser()?.uid ?: return

        val mesSelecionado = spinnerMes.selectedItemPosition + 1
        val anoSelecionado = spinnerAno.selectedItem.toString().toInt()

        lifecycleScope.launch {
            try {
                val lista = movimentacaoRepository.getMovimentacoesByMonth(userId, mesSelecionado, anoSelecionado)
                adapter.atualizarLista(lista)

                val receitasTotal = lista.filter { it.natureza == "Receita" }.sumOf { it.valor }
                val despesasTotal = lista.filter { it.natureza == "Despesa" }.sumOf { it.valor }
                val saldo = receitasTotal - despesasTotal

                txtTotalMes.text = "Saldo no mês: € ${String.format("%.2f", saldo)}"
                
                if (lista.isEmpty()) {
                    Toast.makeText(this@HistoricoMensalActivity, "Nenhuma movimentação para este período", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@HistoricoMensalActivity, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
