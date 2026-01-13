package com.example.projetofindpower.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import com.example.projetofindpower.controller.DespesaController
import com.example.projetofindpower.model.Despesa

@Composable
fun ListaDespesasScreen(viewModel: DespesaController) {
    val despesas by viewModel.despesas.observeAsState(emptyList())

    var despesaParaExcluir by remember { mutableStateOf<Despesa?>(null) }

    Column {
        despesas.forEach { despesa ->

            Row {
                Text(text = "${despesa.tipo}: €${despesa.valor}")

                // ✅ CORRIGIDO: Modifier.weight()
                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "Excluir",
                    modifier = Modifier.clickable {
                        despesaParaExcluir = despesa
                    }
                )
            }
        }
    }

    despesaParaExcluir?.let { despesa ->
        AlertDialog(
            onDismissRequest = { despesaParaExcluir = null },
            title = { Text("Excluir Despesa") },
            text = { Text("Tem certeza que deseja excluir esta despesa?") },
            confirmButton = {
                Text(
                    "Excluir",
                    modifier = Modifier.clickable {
                        viewModel.deleteDespesa(despesa)
                        despesaParaExcluir = null
                    }
                )
            },
            dismissButton = {
                Text(
                    "Cancelar",
                    modifier = Modifier.clickable {
                        despesaParaExcluir = null
                    }
                )
            }
        )
    }
}

private fun DespesaController.deleteDespesa(despesa: Despesa) {}
