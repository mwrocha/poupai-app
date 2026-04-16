package io.poupai.app.features.finances.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.poupai.app.core.network.Resource
import io.poupai.app.domain.repository.FinanceRepository
import io.poupai.app.features.finances.state.FinancesUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FinancesViewModel @Inject constructor(
    private val financeRepository: FinanceRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(FinancesUiState())
    val uiState: StateFlow<FinancesUiState> = _uiState.asStateFlow()

    init {
        loadFinances()
    }

    private fun loadFinances() {
        viewModelScope.launch {
            financeRepository.getFinanceSummary().collect { result ->
                when (result) {
                    is Resource.Loading -> _uiState.update { it.copy(isLoading = true) }
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                incomeHistory = result.data.incomeHistory,
                                expenseHistory = result.data.expenseHistory,
                                profitHistory = result.data.profitHistory,
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                    }
                }
            }
        }
    }
}
