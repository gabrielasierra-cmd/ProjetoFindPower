package viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projetofindpower.data.local.Despesa
import com.example.projetofindpower.repository.DespesaRepository   // ← usa este nombre
import kotlinx.coroutines.launch

class DespesaViewModel(
    private val repository: DespesaRepository   // ← pásalo por constructor
) : ViewModel() {

    private val _despesas = MutableLiveData<List<Despesa>>()
    val despesas: LiveData<List<Despesa>> = _despesas

    fun getDespesas(userId: String) {
        viewModelScope.launch {
            val resultado = repository.getDespesas(userId)
            _despesas.value = resultado
        }
    }

    fun criarDespesa(despesa: Despesa) {
        viewModelScope.launch {
            repository.criarDespesa(despesa)
            getDespesas(despesa.idUtilizador)
        }
    }
}