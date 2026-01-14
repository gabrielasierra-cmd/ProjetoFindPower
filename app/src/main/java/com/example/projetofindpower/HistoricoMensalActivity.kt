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
class HistoricoMensalActivity : AppCompatActivity() {

    @Inject
    lateinit var controller: MovimentacaoController

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
    }

    override fun onResume() {
        super.onResume()
        executarFiltro()
    }

    private fun inicializarComponentes() {
        spinnerMes = findViewById(R.id.spinnerMes)
        spinnerAno = findViewById(R.id.spinnerAno)
        btnFiltrar = findViewById(R.id.btnFiltrar)
        txtTotalMes = findViewById(R.id.txtTotalMes)
        recyclerMovimentacoes = findViewById(R.id.recyclerDespesas)
    }

    private fun configurarSpinners() {
        val meses = arrayOf("Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho", "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro")
        spinnerMes.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, meses)
        
        val anoAtual = Calendar.getInstance().get(Calendar.YEAR)
        val anos = (anoAtual - 5..anoAtual + 2).toList().map { it.toString() }
        spinnerAno.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, anos)

        spinnerMes.setSelection(Calendar.getInstance().get(Calendar.MONTH))
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
            .setMessage("Deseja eliminar '${mov.descricao}'?")
            .setPositiveButton("Sim") { _, _ ->
                lifecycleScope.launch {
                    controller.eliminar(mov)
                    Toast.makeText(this@HistoricoMensalActivity, "Removido!", Toast.LENGTH_SHORT).show()
                    executarFiltro()
                }
            }
            .setNegativeButton("Não", null)
            .show()
    }

    private fun executarFiltro() {
        val mes = spinnerMes.selectedItemPosition + 1
        val ano = spinnerAno.selectedItem.toString().toInt()

        lifecycleScope.launch {
            try {
                val lista = controller.buscarPorMesEAno(mes, ano)
                adapter.atualizarLista(lista)
                
                val (_, _, saldo) = controller.calcularResumo(lista)
                txtTotalMes.text = "Saldo: € ${String.format("%.2f", saldo)}"
            } catch (e: Exception) {
                Toast.makeText(this@HistoricoMensalActivity, "Erro ao carregar", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
