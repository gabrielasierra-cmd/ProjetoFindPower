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
class HistoricoMensalActivity : AppCompatActivity() {

    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var expenseRepository: DespesaRepository

    private lateinit var spinnerMes: Spinner
    private lateinit var spinnerAno: Spinner
    private lateinit var btnFiltrar: Button
    private lateinit var txtTotalMes: TextView
    private lateinit var recyclerDespesas: RecyclerView
    private lateinit var adapter: DespesaAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historico_mensal)

        inicializarComponentes()
        configurarSpinners()
        configurarRecycler()

        btnFiltrar.setOnClickListener {
            executarFiltro()
        }

        // Carrega o mês atual por padrão
        executarFiltro()
    }

    private fun inicializarComponentes() {
        spinnerMes = findViewById(R.id.spinnerMes)
        spinnerAno = findViewById(R.id.spinnerAno)
        btnFiltrar = findViewById(R.id.btnFiltrar)
        txtTotalMes = findViewById(R.id.txtTotalMes)
        recyclerDespesas = findViewById(R.id.recyclerDespesas)
    }

    private fun configurarSpinners() {
        // Configura meses
        val meses = arrayOf(
            "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
            "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"
        )
        val adapterMes = ArrayAdapter(this, android.R.layout.simple_spinner_item, meses)
        adapterMes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerMes.adapter = adapterMes

        // Configura anos (ex: últimos 5 anos e próximos 2)
        val anoAtual = Calendar.getInstance().get(Calendar.YEAR)
        val anos = (anoAtual - 5..anoAtual + 2).toList().map { it.toString() }
        val adapterAno = ArrayAdapter(this, android.R.layout.simple_spinner_item, anos)
        adapterAno.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerAno.adapter = adapterAno

        // Seleciona mês atual por padrão
        val mesAtual = Calendar.getInstance().get(Calendar.MONTH)
        spinnerMes.setSelection(mesAtual)
        spinnerAno.setSelection(5) // O ano atual na nossa lista gerada
    }

    private fun configurarRecycler() {
        adapter = DespesaAdapter(emptyList())
        recyclerDespesas.layoutManager = LinearLayoutManager(this)
        recyclerDespesas.adapter = adapter
    }

    private fun executarFiltro() {
        val userId = authRepository.getCurrentUser()?.uid ?: run {
            Toast.makeText(this, "Erro: Usuário não logado", Toast.LENGTH_SHORT).show()
            return
        }

        val mesSelecionado = spinnerMes.selectedItemPosition + 1
        val anoSelecionado = spinnerAno.selectedItem.toString().toInt()

        lifecycleScope.launch {
            try {
                val despesasFiltradas = expenseRepository.getExpensesByMonth(userId, mesSelecionado, anoSelecionado)
                
                adapter.atualizarLista(despesasFiltradas)

                val total = despesasFiltradas.sumOf { it.valor }
                txtTotalMes.text = "Total no mês: € ${String.format("%.2f", total)}"

                if (despesasFiltradas.isEmpty()) {
                    Toast.makeText(this@HistoricoMensalActivity, "Nenhuma despesa para este período", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@HistoricoMensalActivity, "Erro ao filtrar: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
