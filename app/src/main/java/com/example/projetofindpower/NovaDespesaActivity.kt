package com.example.projetofindpower

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.projetofindpower.model.Despesa
import com.example.projetofindpower.repository.AuthRepository
import com.example.projetofindpower.repository.DespesaRepository
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@AndroidEntryPoint
class NovaDespesaActivity : AppCompatActivity() {

    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var expenseRepository: DespesaRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nova_despesa)

        // Referências das Views
        val editValor = findViewById<TextInputEditText>(R.id.editValor)
        val editDescricao = findViewById<TextInputEditText>(R.id.editDescricao)
        val autoCompleteCategoria = findViewById<AutoCompleteTextView>(R.id.autoCompleteCategoria)
        val autoCompleteStatus = findViewById<AutoCompleteTextView>(R.id.autoCompleteStatus)
        val autoCompleteModo = findViewById<AutoCompleteTextView>(R.id.autoCompleteModo)
        val btnSalvar = findViewById<Button>(R.id.btnSalvarDespesa)

        // Configurar os 3 Menus Suspensos
        autoCompleteCategoria.setAdapter(ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, Despesa.LISTA_CATEGORIAS))
        autoCompleteStatus.setAdapter(ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, Despesa.LISTA_STATUS))
        autoCompleteModo.setAdapter(ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, Despesa.LISTA_MODOS))

        btnSalvar.setOnClickListener {
            val valor = editValor.text.toString()
            val desc = editDescricao.text.toString()
            val cat = autoCompleteCategoria.text.toString()
            val stat = autoCompleteStatus.text.toString()
            val modo = autoCompleteModo.text.toString()
            val uid = authRepository.getCurrentUser()?.uid

            if (valor.isNotEmpty() && uid != null) {
                val despesa = Despesa(
                    idDespesa = UUID.randomUUID().toString(),
                    idUtilizador = uid,
                    valor = valor.toDouble(),
                    tipo = cat,
                    descricao = desc,
                    statusPagamento = stat,
                    modoPagamento = modo,
                    data = System.currentTimeMillis().toString()
                )

                lifecycleScope.launch {
                    expenseRepository.saveExpense(despesa)
                    finish()
                }
            }
        }
    }


    private fun salvarNoFirebase(despesa: Despesa) {
        lifecycleScope.launch {
            try {
                expenseRepository.saveExpense(despesa)
                Toast.makeText(this@NovaDespesaActivity, "Salvo com sucesso!", Toast.LENGTH_SHORT).show()
                finish() // Fecha a activity e volta para a tela anterior
            } catch (e: Exception) {
                Toast.makeText(this@NovaDespesaActivity, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
