package ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import viewmodel.DespesaViewModel

class ListaDespesasScreen {

    @Composable
    fun Conteudo(viewModel: DespesaViewModel) {
        val despesas by viewModel.despesas.observeAsState(emptyList())

        Column {
            for (despesa in despesas) {
                Text(text = "${despesa.tipo}: €${despesa.valor}")
            }
        }
    }
}
