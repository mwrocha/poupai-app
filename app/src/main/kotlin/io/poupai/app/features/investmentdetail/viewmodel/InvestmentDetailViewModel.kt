package io.poupai.app.features.investmentdetail.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.poupai.app.core.network.InvestmentEvents
import io.poupai.app.core.network.Resource
import io.poupai.app.core.util.PreferencesManager
import io.poupai.app.domain.repository.InvestmentRepository
import io.poupai.app.features.investmentdetail.state.InvestmentDetailUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InvestmentDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val investmentRepository: InvestmentRepository,
    private val preferencesManager: PreferencesManager,
) : ViewModel() {

    private val investmentId: String = checkNotNull(savedStateHandle["id"]) {
        "investmentId é obrigatório na rota"
    }

    private val _uiState = MutableStateFlow(InvestmentDetailUiState())
    val uiState: StateFlow<InvestmentDetailUiState> = _uiState.asStateFlow()

    init {
        observeHideValues()
        observeEvents()
        loadData()
    }

    private fun observeHideValues() {
        viewModelScope.launch {
            preferencesManager.hideValues.collect { hide ->
                _uiState.update { it.copy(hideValues = hide) }
            }
        }
    }

    private fun observeEvents() {
        viewModelScope.launch {
            InvestmentEvents.entriesChanged.collect { loadData() }
        }
    }

    fun loadData() {
        loadInvestment()
        loadEntries()
        loadDividends()
    }

    private fun loadInvestment() {
        viewModelScope.launch {
            investmentRepository.getInvestments().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        val inv = result.data.find { it.id == investmentId }
                        _uiState.update {
                            it.copy(
                                investment = inv,
                                isLoading = false,
                                errorMessage = if (inv == null) "Ativo não encontrado" else null,
                            )
                        }
                    }
                    is Resource.Error -> _uiState.update {
                        it.copy(isLoading = false, errorMessage = result.message)
                    }
                    Resource.Loading -> _uiState.update { state ->
                        if (state.investment == null) state.copy(isLoading = true) else state
                    }
                }
            }
        }
    }

    private fun loadEntries() {
        viewModelScope.launch {
            val result = investmentRepository.getEntries(investmentId = investmentId)
            if (result is Resource.Success) {
                _uiState.update {
                    it.copy(
                        entries = result.data.entries,
                        totalAported = result.data.totalAported,
                        totalRescued = result.data.totalRescued,
                    )
                }
            }
        }
    }

    private fun loadDividends() {
        viewModelScope.launch {
            val result = investmentRepository.getDividends()
            if (result is Resource.Success) {
                val filtered = result.data.dividends
                    .filter { it.investmentId == investmentId }
                    .sortedByDescending { it.date }
                val total = filtered.sumOf { it.amount }
                _uiState.update { it.copy(dividends = filtered, totalDividends = total) }
            }
        }
    }

    fun refresh() {
        if (_uiState.value.isRefreshing) return
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            loadData()
            delay(1200)
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    fun toggleHideValues() {
        viewModelScope.launch {
            preferencesManager.saveHideValues(!_uiState.value.hideValues)
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
