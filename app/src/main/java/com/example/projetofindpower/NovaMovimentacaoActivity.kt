package com.example.projetofindpower

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.projetofindpower.controller.MovimentacaoController
import com.example.projetofindpower.model.*
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class NovaMovimentacaoActivity : AppCompatActivity() {

    @Inject
    lateinit var controller: MovimentacaoController

    private lateinit var editValor: EditText
    private lateinit var editDescricao: EditText
    private lateinit var autoCategoria: AutoCompleteTextView
    private lateinit var autoStatus: AutoCompleteTextView
    private lateinit var autoModo: AutoCompleteTextView
    private lateinit var layoutPartilha: TextInputLayout
    private lateinit var toggleTipo: MaterialButtonToggleGroup
    private lateinit var btnSalvar: Button
    
    private var tipoSelecionado = TipoMovimentacao.DESPESA
    private var movimentacaoParaEditar: Movimentacao? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nova_despesa)

        inicializarViews()

        movimentacaoParaEditar = intent.getSerializableExtra("MOVIMENTACAO") as? Movimentacao
        movimentacaoParaEditar?.let { preencherDados(it) }

        btnSalvar.setOnClickListener { salvar() }
        
        toggleTipo.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                tipoSelecionado = if (checkedId == R.id.btnSeletorReceita) 
                    TipoMovimentacao.RECEITA else TipoMovimentacao.DESPESA
                atualizarInterfacePorTipo()
            }
        }
        
        configurarDropdownsIniciais()
    }

    private fun inicializarViews() {
        editValor = findViewById(R.id.editValor)
        editDescricao = findViewById(R.id.editDescricao)
        autoCategoria = findViewById(R.id.autoCompleteCategoria)
        autoStatus = findViewById(R.id.autoCompleteStatus)
        autoModo = findViewById(R.id.autoCompleteModo)
        layoutPartilha = findViewById(R.id.layoutPartilha)
        toggleTipo = findViewById(R.id.toggleGroup)
        btnSalvar = findViewById(R.id.btnSalvarDespesa)
    }

    private fun configurarDropdownsIniciais() {
        atualizarInterfacePorTipo()
        
        val status = StatusPagamento.values().map { it.name }.toTypedArray()
        autoStatus.setAdapter(ArrayAdapter(this, android.R.layout.simple_list_item_1, status))
        
        val modos = arrayOf("Dinheiro", "Cartão", "Transferência", "MB Way")
        autoModo.setAdapter(ArrayAdapter(this, android.R.layout.simple_list_item_1, modos))
    }

    private fun atualizarInterfacePorTipo() {
        // Atualizar Categorias
        val categorias = if (tipoSelecionado == TipoMovimentacao.RECEITA)
            arrayOf("SALARIO", "INVESTIMENTO", "PRESENTE", "VENDA", "OUTROS")
        else
            arrayOf("LAZER", "EMERGENCIA", "CONTAS_FIXAS", "POUPANCA", "EXTRAS", "VIAGENS", "OUTROS")

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, categorias)
        autoCategoria.setAdapter(adapter)

        // Esconder campo de partilha se for Receita
        if (tipoSelecionado == TipoMovimentacao.RECEITA) {
            layoutPartilha.visibility = View.GONE
        } else {
            layoutPartilha.visibility = View.VISIBLE
        }
    }

    private fun preencherDados(mov: Movimentacao) {
        editValor.setText(mov.valor.toString())
        editDescricao.setText(mov.descricao)
        autoCategoria.setText(mov.categoria.name, false)
        autoStatus.setText(mov.statusPagamento.name, false)
        autoModo.setText(mov.modoPagamento, false)
        
        tipoSelecionado = mov.tipo
        if (tipoSelecionado == TipoMovimentacao.RECEITA) 
            toggleTipo.check(R.id.btnSeletorReceita) 
        else 
            toggleTipo.check(R.id.btnSeletorDespesa)
            
        atualizarInterfacePorTipo()
    }

    private fun salvar() {
        val valorStr = editValor.text.toString()
        if (valorStr.isBlank()) return

        lifecycleScope.launch {
            val categoriaSelecionada = try { 
                Categoria.valueOf(autoCategoria.text.toString()) 
            } catch (e: Exception) { 
                Categoria.OUTROS 
            }

            val mov = Movimentacao(
                idMovimentacao = movimentacaoParaEditar?.idMovimentacao ?: UUID.randomUUID().toString(),
                idUtilizador = FirebaseAuth.getInstance().currentUser?.uid,
                valor = valorStr.toDouble(),
                descricao = editDescricao.text.toString(),
                tipo = tipoSelecionado,
                categoria = categoriaSelecionada,
                statusPagamento = try { StatusPagamento.valueOf(autoStatus.text.toString()) } catch (e: Exception) { StatusPagamento.PENDENTE },
                modoPagamento = autoModo.text.toString(),
                data = System.currentTimeMillis().toString()
            )
            controller.salvar(mov)
            finish()
        }
    }
}
