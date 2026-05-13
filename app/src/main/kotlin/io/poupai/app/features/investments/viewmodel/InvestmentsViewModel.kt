package io.poupai.app.features.investments.viewmodel

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.poupai.app.core.network.Resource
import io.poupai.app.core.util.PreferencesManager
import io.poupai.app.domain.model.InvestmentType
import io.poupai.app.domain.repository.InvestmentRepository
import io.poupai.app.features.investments.state.InvestmentsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InvestmentsViewModel @Inject constructor(
    private val investmentRepository: InvestmentRepository,
    private val preferencesManager: PreferencesManager,
) : ViewModel(), DefaultLifecycleObserver {

    private val _uiState = MutableStateFlow(InvestmentsUiState())
    val uiState: StateFlow<InvestmentsUiState> = _uiState.asStateFlow()

    init {
        observeHideValues()
        loadAll()
    }

    // Recarrega sempre que a tela volca ao foco (ex: ao voltar do InvestmentBook)
    override fun onResume(owner: LifecycleOwner) {
        loadAll()
    }

    private fun observeHideValues() {
        viewModelScope.launch {
            preferencesManager.hideValues.collect { hide ->
                _uiState.update { it.copy(hideValues = hide) }
            }
        }
    }

    fun toggleHideValues() {
        viewModelScope.launch { preferencesManager.saveHideValues(!_uiState.value.hideValues) }
    }

    fun loadAll() {
        loadInvestments()
        loadBenchmark()
        loadRebalance()
    }

    private fun loadInvestments() {
        viewModelScope.launch {
            investmentRepository.getInvestments().collect { result ->
                when (result) {
                    is Resource.Loading -> _uiState.update { it.copy(isLoading = true) }
                    is Resource.Success -> {
                        val grouped = result.data.groupBy { it.type }
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                rendaVariavel = grouped[InvestmentType.RENDA_VARIAVEL].orEmpty(),
                                rendaFixa = grouped[InvestmentType.RENDA_FIXA].orEmpty(),
                                criptomoedas = grouped[InvestmentType.CRIPTOMOEDAS].orEmpty(),
                            )
                        }
                    }
                    is Resource.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
            }
        }
    }

    private fun loadBenchmark() {
        viewModelScope.launch {
            val result = investmentRepository.getBenchmark()
            if (result is Resource.Success) _uiState.update { it.copy(benchmark = result.data) }
        }
    }

    private fun loadRebalance() {
        viewModelScope.launch {
            val result = investmentRepository.getRebalance()
            if (result is Resource.Success) _uiState.update { it.copy(rebalance = result.data) }
        }
    }
}