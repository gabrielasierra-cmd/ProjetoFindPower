package com.example.projetofindpower.controller

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.projetofindpower.model.Movimentacao
import com.example.projetofindpower.repository.MovimentacaoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MovimentacaoController @Inject constructor(
    private val repository: MovimentacaoRepository
) : ViewModel() {

    private val _movimentacoes = MutableLiveData<List<Movimentacao>>()
    val movimentacoes: LiveData<List<Movimentacao>> = _movimentacoes

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun carregarMovimentacoes(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _movimentacoes.value = repository.getMovimentacoesByUser(userId)
            } catch (e: Exception) {
                // Tratar erro se necessário
            } finally {
                _isLoading.value = false
            }
        }
    }
}
