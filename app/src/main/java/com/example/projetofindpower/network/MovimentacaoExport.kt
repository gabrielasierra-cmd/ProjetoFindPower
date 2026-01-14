package com.example.projetofindpower.network

data class MovimentacaoExport(
    val data: String,
    val categoria: String,
    val descricao: String,
    val valor: String,
    val modo: String,
    val status: String,
    val tipo: String // "Receita" ou "Despesa"
)
