package io.poupai.app.features.incometax.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.poupai.app.core.network.Resource
import io.poupai.app.core.util.PreferencesManager
import io.poupai.app.domain.model.Investment
import io.poupai.app.domain.repository.InvestmentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TaxClassificationUiState(
    val investments: List<Investment> = emptyList(),
    val fiiIds: Set<String> = emptySet(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class TaxClassificationViewModel @Inject constructor(
    private val investmentRepository: InvestmentRepository,
    private val preferencesManager: PreferencesManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TaxClassificationUiState())
    val uiState: StateFlow<TaxClassificationUiState> = _uiState.asStateFlow()

    init {
        observeFiiIds()
        loadInvestments()
    }

    private fun observeFiiIds() {
        viewModelScope.launch {
            preferencesManager.fiiInvestmentIds.collect { ids ->
                _uiState.update { it.copy(fiiIds = ids) }
            }
        }
    }

    private fun loadInvestments() {
        viewModelScope.launch {
            investmentRepository.getInvestments().collect { result ->
                when (result) {
                    is Resource.Success -> _uiState.update {
                        it.copy(investments = result.data, isLoading = false)
                    }
                    is Resource.Error -> _uiState.update { it.copy(isLoading = false) }
                    Resource.Loading -> _uiState.update { state ->
                        if (state.investments.isEmpty()) state.copy(isLoading = true) else state
                    }
                }
            }
        }
    }

    fun toggleFii(investmentId: String, isFii: Boolean) {
        viewModelScope.launch {
            preferencesManager.setIsFii(investmentId, isFii)
        }
    }
}
