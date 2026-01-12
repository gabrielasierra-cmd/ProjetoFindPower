package com.example.projetofindpower.controller

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.projetofindpower.model.Despesa
import com.example.projetofindpower.repository.DespesaRepository
import kotlinx.coroutines.launch

class DespesaController(
    private val repository: DespesaRepository
) : ViewModel() {

    private val _despesas = MutableLiveData<List<Despesa>>()
    val despesas: LiveData<List<Despesa>> = _despesas

    fun carregarDespesas(userId: String) {
        viewModelScope.launch {
            _despesas.value = repository.getExpensesByUser(userId)
        }
    }
}
