package com.example.projetofindpower.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projetofindpower.controller.MovimentacaoController
import com.example.projetofindpower.model.Movimentacao
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ListaDespesasScreen(viewModel: MovimentacaoController) {
    val movimentacoes by viewModel.movimentacoes.observeAsState(emptyList())
    val isLoading by viewModel.isLoading.observeAsState(false)

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Minhas Movimentações",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (isLoading) {
            Text("Carregando dados...")
        } else if (movimentacoes.isEmpty()) {
            Text("Nenhuma movimentação encontrada.")
        } else {
            LazyColumn {
                items(movimentacoes) { mov ->
                    MovimentacaoItem(mov)
                }
            }
        }
    }
}

@Composable
fun MovimentacaoItem(mov: Movimentacao) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = mov.descricao, fontWeight = FontWeight.Bold)
                Text(text = mov.tipo, fontSize = 12.sp, color = Color.Gray)
                
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val dataStr = try { sdf.format(Date(mov.data.toLong())) } catch (e: Exception) { mov.data }
                Text(text = dataStr, fontSize = 10.sp, color = Color.LightGray)
            }

            val valorCor = if (mov.natureza == "Receita") Color(0xFF004D40) else Color(0xFFD32F2F)
            val prefixo = if (mov.natureza == "Receita") "+" else "-"
            
            Text(
                text = "$prefixo €${String.format("%.2f", mov.valor)}",
                color = valorCor,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}
