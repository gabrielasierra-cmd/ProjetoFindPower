package com.example.projetofindpower.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.projetofindpower.R
import com.example.projetofindpower.model.Movimentacao
import com.example.projetofindpower.model.TipoMovimentacao
import java.text.SimpleDateFormat
import java.util.*

class MovimentacaoAdapter(
    private var lista: List<Movimentacao>,
    private val onEditClick: (Movimentacao) -> Unit,
    private val onDeleteClick: (Movimentacao) -> Unit
) : RecyclerView.Adapter<MovimentacaoAdapter.MovimentacaoViewHolder>() {

    class MovimentacaoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtDescricao: TextView = view.findViewById(R.id.txtDescricao)
        val txtValor: TextView = view.findViewById(R.id.txtValor)
        val txtCategoria: TextView = view.findViewById(R.id.txtCategoria)
        val txtData: TextView = view.findViewById(R.id.txtData)
        val btnEdit: ImageView = view.findViewById(R.id.btnEditarStatus)
        val btnDelete: ImageView = view.findViewById(R.id.btnExcluirItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovimentacaoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_despesa, parent, false)
        return MovimentacaoViewHolder(view)
    }

    override fun onBindViewHolder(holder: MovimentacaoViewHolder, position: Int) {
        val mov = lista[position]
        
        holder.txtDescricao.text = mov.descricao
        holder.txtCategoria.text = "${mov.categoria} (${mov.statusPagamento})"

        if (mov.tipo == TipoMovimentacao.RECEITA) {
            holder.txtValor.text = "+ € ${String.format("%.2f", mov.valor)}"
            holder.txtValor.setTextColor(Color.parseColor("#004D40")) // Verde escuro
        } else {
            holder.txtValor.text = "- € ${String.format("%.2f", mov.valor)}"
            holder.txtValor.setTextColor(Color.parseColor("#D32F2F")) // Vermelho
        }

        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val dataStr = try { 
            val timestamp = mov.data.toLongOrNull() ?: System.currentTimeMillis()
            sdf.format(Date(timestamp)) 
        } catch (e: Exception) { 
            mov.data 
        }
        holder.txtData.text = dataStr

        holder.btnEdit.setOnClickListener { onEditClick(mov) }
        holder.btnDelete.setOnClickListener { onDeleteClick(mov) }
    }

    override fun getItemCount() = lista.size

    fun atualizarLista(novaLista: List<Movimentacao>) {
        lista = novaLista
        notifyDataSetChanged()
    }
}
