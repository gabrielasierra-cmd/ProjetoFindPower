package com.example.projetofindpower

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projetofindpower.adapter.MovimentacaoAdapter
import com.example.projetofindpower.controller.MovimentacaoController
import com.example.projetofindpower.model.Movimentacao
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class HistoricoAnualActivity : AppCompatActivity() {

    @Inject
    lateinit var controller: MovimentacaoController

    private lateinit var spinnerAno: Spinner
    private lateinit var btnFiltrar: Button
    private lateinit var txtTotalAno: TextView
    private lateinit var recyclerMovimentacoes: RecyclerView
    private lateinit var adapter: MovimentacaoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historico_anual)

        inicializarUI()
        configurarSpinnerAno()
        configurarRecycler()

        btnFiltrar.setOnClickListener { executarFiltro() }
    }

    override fun onResume() {
        super.onResume()
        executarFiltro()
    }

    private fun inicializarUI() {
        spinnerAno = findViewById(R.id.spinnerAno)
        btnFiltrar = findViewById(R.id.btnFiltrar)
        txtTotalAno = findViewById(R.id.txtTotalAno)
        recyclerMovimentacoes = findViewById(R.id.recyclerDespesas)
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
        adapter = MovimentacaoAdapter(
            lista = emptyList(),
            onEditClick = { mov -> 
                val intent = Intent(this, NovaMovimentacaoActivity::class.java)
                intent.putExtra("MOVIMENTACAO", mov)
                startActivity(intent)
            },
            onDeleteClick = { mov -> confirmarExclusao(mov) }
        )
        recyclerMovimentacoes.layoutManager = LinearLayoutManager(this)
        recyclerMovimentacoes.adapter = adapter
    }

    private fun confirmarExclusao(mov: Movimentacao) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar")
            .setMessage("Deseja realmente eliminar '${mov.descricao}'?")
            .setPositiveButton("Sim") { _, _ ->
                lifecycleScope.launch {
                    try {
                        controller.eliminar(mov)
                        Toast.makeText(this@HistoricoAnualActivity, "Removido!", Toast.LENGTH_SHORT).show()
                        executarFiltro()
                    } catch (e: Exception) {
                        Toast.makeText(this@HistoricoAnualActivity, "Erro ao eliminar", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun executarFiltro() {
        val anoSelecionado = spinnerAno.selectedItem.toString().toInt()
        lifecycleScope.launch {
            try {
                val listaAnual = controller.buscarPorAno(anoSelecionado)
                adapter.atualizarLista(listaAnual)
                val (_, _, saldo) = controller.calcularResumo(listaAnual)
                txtTotalAno.text = "Saldo Anual: € ${String.format("%.2f", saldo)}"
            } catch (e: Exception) {
                Toast.makeText(this@HistoricoAnualActivity, "Erro ao carregar dados", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
