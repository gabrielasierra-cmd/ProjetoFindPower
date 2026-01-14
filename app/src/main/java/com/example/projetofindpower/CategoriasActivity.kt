package com.example.projetofindpower

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projetofindpower.adapter.MovimentacaoAdapter
import com.example.projetofindpower.controller.MovimentacaoController
import com.example.projetofindpower.model.Categoria
import com.example.projetofindpower.model.Movimentacao
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CategoriasActivity : AppCompatActivity() {

    @Inject
    lateinit var controller: MovimentacaoController

    private var ultimaCategoriaSelecionada: Categoria? = null
    private var alertDialogAtual: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_categorias)

        setupClickListeners()
    }

    override fun onResume() {
        super.onResume()
        executarBusca(ultimaCategoriaSelecionada)
    }

    private fun setupClickListeners() {
        // Agora passamos o Enum diretamente
        findViewById<LinearLayout>(R.id.btnLazer).setOnClickListener { buscarEFiltar(Categoria.LAZER) }
        findViewById<LinearLayout>(R.id.btnEmergencia).setOnClickListener { buscarEFiltar(Categoria.EMERGENCIA) }
        findViewById<LinearLayout>(R.id.btnContasFixas).setOnClickListener { buscarEFiltar(Categoria.CONTAS_FIXAS) }
        findViewById<LinearLayout>(R.id.btnPoupanca).setOnClickListener { buscarEFiltar(Categoria.POUPANCA) }
        findViewById<LinearLayout>(R.id.btnExtras).setOnClickListener { buscarEFiltar(Categoria.EXTRAS) }
        findViewById<LinearLayout>(R.id.btnViagens).setOnClickListener { buscarEFiltar(Categoria.VIAGENS) }
        findViewById<LinearLayout>(R.id.btnTodasDespesas).setOnClickListener { buscarTodas() }
    }

    private fun buscarTodas() {
        ultimaCategoriaSelecionada = null
        executarBusca(null)
    }

    private fun buscarEFiltar(categoria: Categoria) {
        ultimaCategoriaSelecionada = categoria
        executarBusca(categoria)
    }

    private fun executarBusca(categoria: Categoria?) {
        lifecycleScope.launch {
            try {
                val lista = if (categoria == null) {
                    controller.buscarTodas()
                } else {
                    controller.buscarPorCategoria(categoria)
                }
                
                exibirListaComIcones(categoria?.name ?: "Todas", lista)
            } catch (e: Exception) {
                Toast.makeText(this@CategoriasActivity, "Erro: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun exibirListaComIcones(titulo: String, lista: List<Movimentacao>) {
        if (alertDialogAtual?.isShowing == true) alertDialogAtual?.dismiss()

        if (lista.isEmpty()) {
            AlertDialog.Builder(this).setTitle(titulo).setMessage("Nenhuma movimentação.").setPositiveButton("Ok", null).show()
            return
        }

        val recyclerView = RecyclerView(this).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            layoutManager = LinearLayoutManager(this@CategoriasActivity)
            setPadding(20, 20, 20, 20)
        }

        recyclerView.adapter = MovimentacaoAdapter(
            lista = lista,
            onEditClick = { mov ->
                val intent = Intent(this, NovaMovimentacaoActivity::class.java)
                intent.putExtra("MOVIMENTACAO", mov)
                startActivity(intent)
            },
            onDeleteClick = { mov -> confirmarExclusao(mov) }
        )

        val (_, _, saldo) = controller.calcularResumo(lista)

        val builder = AlertDialog.Builder(this)
        builder.setTitle("$titulo (Saldo: €${String.format("%.2f", saldo)})")
        builder.setView(recyclerView)
        builder.setPositiveButton("Fechar") { _, _ ->  }
        
        alertDialogAtual = builder.show()
    }

    private fun confirmarExclusao(mov: Movimentacao) {
        AlertDialog.Builder(this)
            .setTitle("Confirmar")
            .setMessage("Deseja eliminar '${mov.descricao}'?")
            .setPositiveButton("Sim") { _, _ ->
                lifecycleScope.launch {
                    controller.eliminar(mov)
                    Toast.makeText(this@CategoriasActivity, "Removido!", Toast.LENGTH_SHORT).show()
                    executarBusca(ultimaCategoriaSelecionada)
                }
            }
            .setNegativeButton("Não", null)
            .show()
    }
}
