package com.example.projetofindpower.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.projetofindpower.R
import com.example.projetofindpower.model.Despesa
import java.text.SimpleDateFormat
import java.util.*

class DespesaAdapter(private var lista: List<Despesa>) : RecyclerView.Adapter<DespesaAdapter.DespesaViewHolder>() {

    class DespesaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtDescricao: TextView = view.findViewById(R.id.txtDescricao)
        val txtValor: TextView = view.findViewById(R.id.txtValor)
        val txtCategoria: TextView = view.findViewById(R.id.txtCategoria)
        val txtData: TextView = view.findViewById(R.id.txtData)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DespesaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_despesa, parent, false)
        return DespesaViewHolder(view)
    }

    override fun onBindViewHolder(holder: DespesaViewHolder, position: Int) {
        val despesa = lista[position]
        holder.txtDescricao.text = despesa.descricao
        holder.txtValor.text = "€ ${String.format("%.2f", despesa.valor)}"
        holder.txtCategoria.text = despesa.tipo

        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val dataFormatada = try {
            sdf.format(Date(despesa.data.toLong()))
        } catch (e: Exception) {
            despesa.data
        }
        holder.txtData.text = dataFormatada
    }

    override fun getItemCount() = lista.size

    fun atualizarLista(novaLista: List<Despesa>) {
        lista = novaLista
        notifyDataSetChanged()
    }
}
