package com.example.projetofindpower.view

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import com.example.projetofindpower.controller.DespesaController

@Composable
fun ListaDespesasScreen(viewModel: DespesaController) {
    val despesas by viewModel.despesas.observeAsState(emptyList())

    Column {
        despesas.forEach { despesa ->
            Text(text = "${despesa.tipo}: €${despesa.valor}")
        }
    }
}
