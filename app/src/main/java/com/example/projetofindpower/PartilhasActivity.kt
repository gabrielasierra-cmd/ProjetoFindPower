package com.example.projetofindpower

import android.content.Intent
import android.os.Bundle
import android.widget.Button
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
import com.example.projetofindpower.model.StatusPagamento
import com.example.projetofindpower.model.TipoMovimentacao
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PartilhasActivity : AppCompatActivity() {

    @Inject
    lateinit var controller: MovimentacaoController

    private lateinit var recyclerDividas: RecyclerView
    private lateinit var adapter: MovimentacaoAdapter
    private lateinit var txtTotalDividas: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_partilhas)

        inicializarComponentes()
        configurarRecycler()

        findViewById<Button>(R.id.btnPartilharAgora).setOnClickListener {
            abrirSelecaoParaPartilha()
        }

        carregarDividas()
    }

    private fun inicializarComponentes() {
        recyclerDividas = findViewById(R.id.recyclerPartilhas)
        txtTotalDividas = findViewById(R.id.txtTotalPartilhado)
    }

    private fun configurarRecycler() {
        adapter = MovimentacaoAdapter(
            lista = emptyList(),
            onEditClick = { mov -> 
                val intent = Intent(this, NovaMovimentacaoActivity::class.java)
                intent.putExtra("MOVIMENTACAO", mov)
                startActivity(intent)
            },
            onDeleteClick = { mov -> excluirMovimentacao(mov) }
        )
        recyclerDividas.layoutManager = LinearLayoutManager(this)
        recyclerDividas.adapter = adapter
    }

    private fun carregarDividas() {
        lifecycleScope.launch {
            try {
                val todas = controller.buscarTodas()
                // Dívidas são despesas pendentes
                val dividas = todas.filter { it.statusPagamento == StatusPagamento.PENDENTE }
                adapter.atualizarLista(dividas)
                
                val total = dividas.sumOf { it.valor }
                txtTotalDividas.text = "Total em Aberto: € ${String.format("%.2f", total)}"
            } catch (e: Exception) {
                Toast.makeText(this@PartilhasActivity, "Erro ao carregar", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun abrirSelecaoParaPartilha() {
        lifecycleScope.launch {
            val todas = controller.buscarTodas().filter { it.tipo == TipoMovimentacao.DESPESA }
            if (todas.isEmpty()) {
                Toast.makeText(this@PartilhasActivity, "Nenhuma despesa para partilhar", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val descricoes = todas.map { "${it.descricao} (€${it.valor})" }.toTypedArray()
            
            AlertDialog.Builder(this@PartilhasActivity)
                .setTitle("Escolha a despesa para partilhar")
                .setItems(descricoes) { _, which ->
                    realizarPartilha(todas[which])
                }
                .show()
        }
    }

    private fun realizarPartilha(mov: Movimentacao) {
        val textoPartilha = "Olá! Gostaria de partilhar esta despesa contigo:\n\n" +
                "📝 Descrição: ${mov.descricao}\n" +
                "💰 Valor: €${mov.valor}\n" +
                "📂 Categoria: ${mov.categoria}\n\n" +
                "Enviado via FindPower App ⚡"

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, textoPartilha)
            type = "text/plain"
        }

        startActivity(Intent.createChooser(sendIntent, "Partilhar via"))

        lifecycleScope.launch {
            controller.salvar(mov.copy(statusPagamento = StatusPagamento.PENDENTE))
            carregarDividas()
        }
    }

    private fun excluirMovimentacao(mov: Movimentacao) {
        lifecycleScope.launch {
            controller.eliminar(mov)
            carregarDividas()
            Toast.makeText(this@PartilhasActivity, "Removido!", Toast.LENGTH_SHORT).show()
        }
    }
}
