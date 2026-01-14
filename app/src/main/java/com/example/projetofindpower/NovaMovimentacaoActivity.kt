package com.example.projetofindpower

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.projetofindpower.controller.MovimentacaoController
import com.example.projetofindpower.model.Movimentacao
import com.example.projetofindpower.repository.AuthRepository
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@AndroidEntryPoint
class NovaMovimentacaoActivity : AppCompatActivity() {

    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var controller: MovimentacaoController // Usando o Controller

    private var naturezaSelecionada = "Despesa"
    private var movimentacaoParaEditar: Movimentacao? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nova_despesa)

        val toggleGroup = findViewById<MaterialButtonToggleGroup>(R.id.toggleGroup)
        val editValor = findViewById<TextInputEditText>(R.id.editValor)
        val editDescricao = findViewById<TextInputEditText>(R.id.editDescricao)
        val autoCompleteCategoria = findViewById<AutoCompleteTextView>(R.id.autoCompleteCategoria)
        val autoCompleteStatus = findViewById<AutoCompleteTextView>(R.id.autoCompleteStatus)
        val autoCompleteModo = findViewById<AutoCompleteTextView>(R.id.autoCompleteModo)
        val btnSalvar = findViewById<Button>(R.id.btnSalvarDespesa)

        configurarCategorias()
        autoCompleteStatus.setAdapter(ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, Movimentacao.LISTA_STATUS))
        autoCompleteModo.setAdapter(ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, Movimentacao.LISTA_MODOS))

        intent.getSerializableExtra("MOVIMENTACAO")?.let {
            movimentacaoParaEditar = it as Movimentacao
            naturezaSelecionada = movimentacaoParaEditar!!.natureza
            preencherCampos(editValor, editDescricao, autoCompleteCategoria, autoCompleteStatus, autoCompleteModo, toggleGroup)
            btnSalvar.text = "Atualizar"
        }

        toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                naturezaSelecionada = if (checkedId == R.id.btnSeletorReceita) "Receita" else "Despesa"
                configurarCategorias()
            }
        }

        btnSalvar.setOnClickListener {
            val valorText = editValor.text.toString()
            val uid = authRepository.getCurrentUser()?.uid

            if (valorText.isNotEmpty() && uid != null) {
                val mov = Movimentacao(
                    idMovimentacao = movimentacaoParaEditar?.idMovimentacao ?: UUID.randomUUID().toString(),
                    idUtilizador = uid,
                    valor = valorText.toDouble(),
                    tipo = autoCompleteCategoria.text.toString(),
                    natureza = naturezaSelecionada,
                    descricao = editDescricao.text.toString(),
                    statusPagamento = autoCompleteStatus.text.toString(),
                    modoPagamento = autoCompleteModo.text.toString(),
                    data = movimentacaoParaEditar?.data ?: System.currentTimeMillis().toString()
                )

                lifecycleScope.launch {
                    try {
                        if (movimentacaoParaEditar != null) {
                            controller.atualizar(mov)
                            Toast.makeText(this@NovaMovimentacaoActivity, "Atualizado!", Toast.LENGTH_SHORT).show()
                        } else {
                            controller.salvar(mov)
                            Toast.makeText(this@NovaMovimentacaoActivity, "Salvo com sucesso!", Toast.LENGTH_SHORT).show()
                        }
                        finish()
                    } catch (e: Exception) {
                        Toast.makeText(this@NovaMovimentacaoActivity, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun preencherCampos(v: TextInputEditText, d: TextInputEditText, c: AutoCompleteTextView, s: AutoCompleteTextView, m: AutoCompleteTextView, t: MaterialButtonToggleGroup) {
        movimentacaoParaEditar?.let { mov ->
            v.setText(mov.valor.toString())
            d.setText(mov.descricao)
            c.setText(mov.tipo, false)
            s.setText(mov.statusPagamento, false)
            m.setText(mov.modoPagamento, false)
            if (mov.natureza == "Receita") t.check(R.id.btnSeletorReceita) else t.check(R.id.btnSeletorDespesa)
            configurarCategorias()
        }
    }

    private fun configurarCategorias() {
        val lista = if (naturezaSelecionada == "Receita") Movimentacao.LISTA_CATEGORIAS_RECEITA else Movimentacao.LISTA_CATEGORIAS_DESPESA
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, lista)
        findViewById<AutoCompleteTextView>(R.id.autoCompleteCategoria).setAdapter(adapter)
    }
}
