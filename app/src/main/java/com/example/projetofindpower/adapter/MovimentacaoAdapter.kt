package com.example.projetofindpower.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.projetofindpower.R
import com.example.projetofindpower.model.Movimentacao
import java.text.SimpleDateFormat
import java.util.*

class MovimentacaoAdapter(private var lista: List<Movimentacao>) : RecyclerView.Adapter<MovimentacaoAdapter.MovimentacaoViewHolder>() {

    class MovimentacaoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtDescricao: TextView = view.findViewById(R.id.txtDescricao)
        val txtValor: TextView = view.findViewById(R.id.txtValor)
        val txtCategoria: TextView = view.findViewById(R.id.txtCategoria)
        val txtData: TextView = view.findViewById(R.id.txtData)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovimentacaoViewHolder {
        // Mantendo o layout item_despesa por enquanto, para não precisar criar outro XML agora
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_despesa, parent, false)
        return MovimentacaoViewHolder(view)
    }

    override fun onBindViewHolder(holder: MovimentacaoViewHolder, position: Int) {
        val mov = lista[position]
        holder.txtDescricao.text = mov.descricao
        holder.txtCategoria.text = mov.tipo

        // Lógica de Cor: Verde para Receita, Vermelho para Despesa
        if (mov.natureza == "Receita") {
            holder.txtValor.text = "+ € ${String.format("%.2f", mov.valor)}"
            holder.txtValor.setTextColor(Color.parseColor("#004D40")) // Verde
        } else {
            holder.txtValor.text = "- € ${String.format("%.2f", mov.valor)}"
            holder.txtValor.setTextColor(Color.parseColor("#D32F2F")) // Vermelho
        }

        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val dataFormatada = try {
            sdf.format(Date(mov.data.toLong()))
        } catch (e: Exception) {
            mov.data
        }
        holder.txtData.text = dataFormatada
    }

    override fun getItemCount() = lista.size

    fun atualizarLista(novaLista: List<Movimentacao>) {
        lista = novaLista
        notifyDataSetChanged()
    }
}
