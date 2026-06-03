package io.poupai.app.features.dividends.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.poupai.app.core.network.Resource
import io.poupai.app.domain.model.Dividend
import io.poupai.app.domain.model.DividendType
import io.poupai.app.domain.repository.InvestmentRepository
import io.poupai.app.features.dividends.state.DividendsUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import io.poupai.app.core.util.DateFormatter
import javax.inject.Inject

@HiltViewModel
class DividendsViewModel @Inject constructor(
    private val investmentRepository: InvestmentRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DividendsUiState())
    val uiState: StateFlow<DividendsUiState> = _uiState.asStateFlow()

    init {
        loadAll()
    }

    fun loadAll() {
        loadDividends()
        loadInvestments()
    }

    fun refresh() {
        if (_uiState.value.isRefreshing) return
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            loadAll()
            delay(1200)
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    private fun loadDividends() {
        viewModelScope.launch {
            // Mantém conteúdo visível em refreshes subsequentes (sem flash).
            _uiState.update { state ->
                if (state.allDividends.isEmpty()) state.copy(isLoading = true) else state
            }
            when (val result = investmentRepository.getDividends()) {
                is Resource.Success -> _uiState.update {
                    it.copy(
                        isLoading = false,
                        allDividends = result.data.dividends.sortedByDescending { d -> d.date },
                        totalReceived = result.data.totalReceived,
                        totalReceivedThisYear = result.data.totalReceivedThisYear,
                        totalReceivedThisMonth = result.data.totalReceivedThisMonth,
                        projectedAnnual = result.data.projectedAnnual,
                    )
                }
                is Resource.Error -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = result.message)
                }
                is Resource.Loading -> Unit
            }
        }
    }

    private fun loadInvestments() {
        viewModelScope.launch {
            val result = investmentRepository.getInvestments().first { it !is Resource.Loading }
            if (result is Resource.Success) {
                _uiState.update { it.copy(investments = result.data) }
            }
        }
    }

    /** Filtro de ano para as analytics (null = todos). Operação client-side. */
    fun onSelectYear(year: Int?) {
        _uiState.update { it.copy(selectedYear = year) }
    }

    // ─── Formulário ───

    fun onShowAddSheet() = _uiState.update {
        it.copy(
            showAddSheet = true, formInvestmentId = "", formInvestmentName = "",
            formAmount = "", formType = DividendType.DIVIDENDO,
            formDate = DateFormatter.todayDisplay(), formError = null,
        )
    }

    fun onDismissSheet() = _uiState.update { it.copy(showAddSheet = false, formError = null) }

    fun onFormInvestmentSelected(id: String, name: String) =
        _uiState.update { it.copy(formInvestmentId = id, formInvestmentName = name, formError = null) }

    fun onFormAmountChanged(v: String) = _uiState.update { it.copy(formAmount = io.poupai.app.core.util.NumberMasks.decimal(v), formError = null) }
    fun onFormTypeChanged(t: DividendType) = _uiState.update { it.copy(formType = t) }
    fun onFormDateChanged(v: String) {
        val masked = DateFormatter.applyMask(v)
        _uiState.update { it.copy(formDate = masked) }
    }

    fun onSaveDividend() {
        val state = _uiState.value
        val amount = state.formAmount.replace(",", ".").toDoubleOrNull()
        when {
            state.formInvestmentId.isBlank() -> {
                _uiState.update { it.copy(formError = "Selecione o ativo") }; return
            }
            amount == null || amount <= 0 -> {
                _uiState.update { it.copy(formError = "Valor inválido") }; return
            }
            state.formDate.isBlank() || !DateFormatter.isValidDisplay(state.formDate) -> {
                _uiState.update { it.copy(formError = "Data inválida (use dd/mm/aaaa)") }; return
            }
        }
        val isoDate = DateFormatter.displayToIso(state.formDate) ?: state.formDate
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            when (investmentRepository.addDividend(
                investmentId = state.formInvestmentId,
                amount = amount!!,
                type = state.formType,
                date = isoDate,
            )) {
                is Resource.Success -> { onDismissSheet(); loadDividends() }
                is Resource.Error -> _uiState.update {
                    it.copy(isSaving = false, formError = "Erro ao salvar")
                }
                is Resource.Loading -> Unit
            }
        }
    }

    // ─── Exclusão ───

    fun onDeleteRequest(dividend: Dividend) =
        _uiState.update { it.copy(showDeleteDialog = true, deletingDividend = dividend) }

    fun onDeleteCancel() =
        _uiState.update { it.copy(showDeleteDialog = false, deletingDividend = null) }

    fun onDeleteConfirm() {
        val dividend = _uiState.value.deletingDividend ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(showDeleteDialog = false, deletingDividend = null) }
            when (investmentRepository.deleteDividend(dividend.id)) {
                is Resource.Success -> loadDividends()
                else -> Unit
            }
        }
    }
}
