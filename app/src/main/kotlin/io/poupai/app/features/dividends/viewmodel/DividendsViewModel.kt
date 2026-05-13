package io.poupai.app.features.dividends.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.poupai.app.core.network.Resource
import io.poupai.app.domain.model.Dividend
import io.poupai.app.domain.model.DividendType
import io.poupai.app.domain.repository.InvestmentRepository
import io.poupai.app.features.dividends.state.DividendsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
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

    fun loadDividends() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = investmentRepository.getDividends(
                year = _uiState.value.selectedYear,
                month = _uiState.value.selectedMonth,
            )) {
                is Resource.Success -> _uiState.update {
                    it.copy(
                        isLoading = false,
                        dividends = result.data.dividends,
                        totalReceived = result.data.totalReceived,
                        totalReceivedThisYear = result.data.totalReceivedThisYear,
                        totalReceivedThisMonth = result.data.totalReceivedThisMonth,
                        projectedAnnual = result.data.projectedAnnual,
                    )
                }
                is Resource.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
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

    fun onFilterMonth(month: Int?, year: Int?) {
        _uiState.update { it.copy(selectedMonth = month, selectedYear = year) }
        loadDividends()
    }

    fun onClearFilter() {
        _uiState.update { it.copy(selectedMonth = null, selectedYear = null) }
        loadDividends()
    }

    // ─── Formulário ───

    fun onShowAddSheet() = _uiState.update {
        it.copy(showAddSheet = true, formInvestmentId = "", formInvestmentName = "",
            formAmount = "", formType = DividendType.DIVIDENDO,
            formDate = LocalDate.now().toString(), formError = null)
    }

    fun onDismissSheet() = _uiState.update { it.copy(showAddSheet = false, formError = null) }

    fun onFormInvestmentSelected(id: String, name: String) =
        _uiState.update { it.copy(formInvestmentId = id, formInvestmentName = name, formError = null) }

    fun onFormAmountChanged(v: String) = _uiState.update { it.copy(formAmount = v, formError = null) }
    fun onFormTypeChanged(t: DividendType) = _uiState.update { it.copy(formType = t) }
    fun onFormDateChanged(v: String) = _uiState.update { it.copy(formDate = v) }

    fun onSaveDividend() {
        val state = _uiState.value
        val amount = state.formAmount.replace(",", ".").toDoubleOrNull()
        when {
            state.formInvestmentId.isBlank() -> { _uiState.update { it.copy(formError = "Selecione o ativo") }; return }
            amount == null || amount <= 0 -> { _uiState.update { it.copy(formError = "Valor inválido") }; return }
            state.formDate.isBlank() -> { _uiState.update { it.copy(formError = "Informe a data") }; return }
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            when (investmentRepository.addDividend(
                investmentId = state.formInvestmentId,
                amount = amount!!,
                type = state.formType,
                date = state.formDate,
            )) {
                is Resource.Success -> { onDismissSheet(); loadDividends() }
                is Resource.Error -> _uiState.update { it.copy(isSaving = false, formError = "Erro ao salvar") }
                is Resource.Loading -> Unit
            }
        }
    }

    // ─── Exclusão ───

    fun onDeleteRequest(dividend: Dividend) =
        _uiState.update { it.copy(showDeleteDialog = true, deletingDividend = dividend) }

    fun onDeleteCancel() = _uiState.update { it.copy(showDeleteDialog = false, deletingDividend = null) }

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