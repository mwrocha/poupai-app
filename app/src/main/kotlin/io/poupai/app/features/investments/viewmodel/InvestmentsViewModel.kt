package io.poupai.app.features.investments.viewmodel

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.poupai.app.core.network.InvestmentEvents
import io.poupai.app.core.network.Resource
import io.poupai.app.core.util.PreferencesManager
import io.poupai.app.domain.model.Investment
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
        observeInvestmentEvents()
        observeCategoryTargets()
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

    private fun observeInvestmentEvents() {
        viewModelScope.launch {
            InvestmentEvents.entriesChanged.collect { loadAll() }
        }
    }

    private fun observeCategoryTargets() {
        viewModelScope.launch {
            preferencesManager.targetRV.collect { rv ->
                _uiState.update { it.copy(categoryTargetRV = rv) }
            }
        }
        viewModelScope.launch {
            preferencesManager.targetRF.collect { rf ->
                _uiState.update { it.copy(categoryTargetRF = rf) }
            }
        }
        viewModelScope.launch {
            preferencesManager.targetCripto.collect { cripto ->
                _uiState.update { it.copy(categoryTargetCripto = cripto) }
            }
        }
    }

    fun updateAllocationTarget(id: String, target: Double) {
        viewModelScope.launch {
            _uiState.update { it.copy(savingAllocationTargetId = id) }
            val result = investmentRepository.updateAllocationTarget(id, target)
            _uiState.update { it.copy(savingAllocationTargetId = null) }
            if (result is Resource.Error) {
                _uiState.update { it.copy(errorMessage = result.message) }
            } else {
                loadRebalance()
            }
        }
    }

    fun saveCategoryTarget(type: InvestmentType, target: Double) {
        viewModelScope.launch {
            when (type) {
                InvestmentType.RENDA_VARIAVEL -> preferencesManager.saveTargetRV(target)
                InvestmentType.RENDA_FIXA -> preferencesManager.saveTargetRF(target)
                InvestmentType.CRIPTOMOEDAS -> preferencesManager.saveTargetCripto(target)
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
                        val recalculated = recalculateProfitability(result.data)
                        val grouped = recalculated.groupBy { it.type }
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                rendaVariavel = grouped[InvestmentType.RENDA_VARIAVEL].orEmpty(),
                                rendaFixa = grouped[InvestmentType.RENDA_FIXA].orEmpty(),
                                criptomoedas = grouped[InvestmentType.CRIPTOMOEDAS].orEmpty(),
                            )
                        }
                    }
                    is Resource.Error -> _uiState.update {
                        it.copy(isLoading = false, errorMessage = result.message)
                    }
                }
            }
        }
    }

    /**
     * Recalcula a rentabilidade local de cada ativo com base no currentValue e investedValue
     * retornados pelo backend. Substitui a integração com brapi.dev.
     *
     * profitability = ((currentValue - investedValue) / investedValue) * 100
     * Se investedValue == 0, profitability = 0.
     */
    private fun recalculateProfitability(investments: List<Investment>): List<Investment> {
        return investments.map { inv ->
            val profitability = if (inv.investedValue == 0.0) 0.0
            else ((inv.currentValue - inv.investedValue) / inv.investedValue) * 100.0
            inv.copy(profitability = profitability)
        }
    }

    fun onDeleteInvestment(id: String) {
        viewModelScope.launch {
            investmentRepository.deleteInvestment(id)
            loadAll()
        }
    }

    // ─── Edit sheet ───

    fun onShowEditSheet(investment: Investment) {
        _uiState.update {
            it.copy(
                showEditSheet = true,
                editingInvestment = investment,
                editFormName = investment.name,
                editFormShares = if (investment.shares > 0) investment.shares.toString() else "",
                editFormAveragePrice = if (investment.averagePrice > 0) investment.averagePrice.toString() else "",
                editFormInvestedValue = if (investment.investedValue > 0) investment.investedValue.toString() else "",
                editFormError = null,
                isSavingEdit = false,
            )
        }
    }

    fun onDismissEditSheet() {
        _uiState.update { it.copy(showEditSheet = false, editingInvestment = null, editFormError = null) }
    }

    fun onEditNameChanged(v: String) { _uiState.update { it.copy(editFormName = v) } }
    fun onEditSharesChanged(v: String) { _uiState.update { it.copy(editFormShares = v) } }
    fun onEditAveragePriceChanged(v: String) { _uiState.update { it.copy(editFormAveragePrice = v) } }
    fun onEditInvestedValueChanged(v: String) { _uiState.update { it.copy(editFormInvestedValue = v) } }

    fun onSaveEdit() {
        val state = _uiState.value
        val investment = state.editingInvestment ?: return
        val name = state.editFormName.trim()
        if (name.isBlank()) {
            _uiState.update { it.copy(editFormError = "Nome é obrigatório") }
            return
        }
        val investedValue = state.editFormInvestedValue.replace(",", ".").toDoubleOrNull()
        if (investedValue == null || investedValue < 0) {
            _uiState.update { it.copy(editFormError = "Valor investido inválido") }
            return
        }
        val sharesStr = state.editFormShares.trim()
        val shares = if (sharesStr.isEmpty()) 0.0 else sharesStr.replace(",", ".").toDoubleOrNull()
        if (shares == null || shares < 0) {
            _uiState.update { it.copy(editFormError = "Cotas inválidas") }
            return
        }
        val avgPriceStr = state.editFormAveragePrice.trim()
        val averagePrice = if (avgPriceStr.isEmpty()) 0.0 else avgPriceStr.replace(",", ".").toDoubleOrNull()
        if (averagePrice == null || averagePrice < 0) {
            _uiState.update { it.copy(editFormError = "Preço médio inválido") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSavingEdit = true, editFormError = null) }
            val result = investmentRepository.editInvestment(
                id = investment.id,
                name = name,
                shares = shares,
                averagePrice = averagePrice,
                investedValue = investedValue,
            )
            when (result) {
                is Resource.Success -> {
                    _uiState.update { it.copy(showEditSheet = false, editingInvestment = null, isSavingEdit = false) }
                    loadAll()
                }
                is Resource.Error -> _uiState.update { it.copy(isSavingEdit = false, editFormError = result.message) }
                else -> _uiState.update { it.copy(isSavingEdit = false) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
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
