package io.poupai.app.features.investments.viewmodel

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.poupai.app.core.network.Resource
import io.poupai.app.core.util.PreferencesManager
import io.poupai.app.domain.model.Investment
import io.poupai.app.domain.model.InvestmentType
import io.poupai.app.domain.repository.BrapiRepository
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
    private val brapiRepository: BrapiRepository,
    private val preferencesManager: PreferencesManager,
) : ViewModel(), DefaultLifecycleObserver {

    private val _uiState = MutableStateFlow(InvestmentsUiState())
    val uiState: StateFlow<InvestmentsUiState> = _uiState.asStateFlow()

    init {
        observeHideValues()
        loadAll()
    }

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
                        val investments = result.data
                        _uiState.update { it.copy(isLoading = false) }
                        updateWithMarketPrices(investments)
                    }
                    is Resource.Error -> _uiState.update {
                        it.copy(isLoading = false, errorMessage = result.message)
                    }
                }
            }
        }
    }

    // Busca preços de mercado para ativos de Renda Variável e Criptomoedas
    // Renda Fixa usa investedValue como currentValue (sem cotação de mercado)
    private suspend fun updateWithMarketPrices(investments: List<Investment>) {
        val tickerMap = investments
            .filter { it.type != InvestmentType.RENDA_FIXA && it.shares > 0 }
            .associate { it.name.trim().uppercase() to it }

        val quotes = if (tickerMap.isNotEmpty()) {
            brapiRepository.getQuotes(tickerMap.keys.toList())
        } else {
            emptyMap()
        }

        val updated = investments.map { inv ->
            val ticker = inv.name.trim().uppercase()
            val marketPrice = quotes[ticker]
            when {
                // Tem preço de mercado e cotas: recalcula currentValue
                marketPrice != null && inv.shares > 0 ->
                    inv.copy(currentValue = inv.shares * marketPrice)
                // Renda Fixa sem cotação: usa investedValue como valor atual
                inv.type == InvestmentType.RENDA_FIXA ->
                    inv.copy(currentValue = inv.investedValue)
                // Sem preço de mercado mas tem cotas: usa PM × cotas
                inv.shares > 0 && inv.averagePrice > 0 ->
                    inv.copy(currentValue = inv.shares * inv.averagePrice)
                else -> inv
            }
        }

        val grouped = updated.groupBy { it.type }
        _uiState.update {
            it.copy(
                rendaVariavel = grouped[InvestmentType.RENDA_VARIAVEL].orEmpty(),
                rendaFixa = grouped[InvestmentType.RENDA_FIXA].orEmpty(),
                criptomoedas = grouped[InvestmentType.CRIPTOMOEDAS].orEmpty(),
            )
        }
    }

    fun onDeleteInvestment(id: String) {
        viewModelScope.launch {
            investmentRepository.deleteInvestment(id)
            loadAll()
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