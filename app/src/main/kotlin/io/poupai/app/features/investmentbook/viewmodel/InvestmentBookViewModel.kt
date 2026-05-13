package io.poupai.app.features.investmentbook.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.poupai.app.core.network.Resource
import io.poupai.app.domain.model.EntryType
import io.poupai.app.domain.model.InvestmentEntry
import io.poupai.app.domain.model.InvestmentType
import io.poupai.app.domain.repository.InvestmentRepository
import io.poupai.app.features.investmentbook.state.InvestmentBookUiState
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
class InvestmentBookViewModel @Inject constructor(
    private val investmentRepository: InvestmentRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(InvestmentBookUiState())
    val uiState: StateFlow<InvestmentBookUiState> = _uiState.asStateFlow()

    init {
        loadAll()
    }

    fun loadAll() {
        loadEntries()
        loadInvestments()
    }

    fun loadEntries() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = investmentRepository.getEntries(
                investmentId = state.selectedInvestmentId,
                year = state.selectedYear,
                month = state.selectedMonth,
            )) {
                is Resource.Success -> _uiState.update {
                    it.copy(
                        isLoading = false,
                        entries = result.data.entries,
                        totalAported = result.data.totalAported,
                        totalRescued = result.data.totalRescued,
                        totalEntries = result.data.totalEntries,
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
            if (result is Resource.Success) _uiState.update { it.copy(investments = result.data) }
        }
    }

    // ─── Filtros ───

    fun onFilterInvestment(id: String?, name: String?) {
        _uiState.update { it.copy(selectedInvestmentId = id, selectedInvestmentName = name) }
        loadEntries()
    }

    fun onFilterMonth(month: Int?, year: Int?) {
        _uiState.update { it.copy(selectedMonth = month, selectedYear = year) }
        loadEntries()
    }

    fun onClearFilters() {
        _uiState.update {
            it.copy(selectedInvestmentId = null, selectedInvestmentName = null,
                selectedMonth = null, selectedYear = null)
        }
        loadEntries()
    }

    // ─── Formulário ───

    fun onShowAddSheet() = _uiState.update {
        it.copy(showAddSheet = true, isNewAsset = false,
            formInvestmentId = "", formInvestmentName = "",
            newAssetName = "", newAssetType = InvestmentType.RENDA_VARIAVEL,
            formType = EntryType.APORTE, formShares = "", formSharePrice = "",
            formNewCurrentValue = "", formNotes = "",
            formDate = LocalDate.now().toString(), formError = null)
    }

    fun onDismissSheet() = _uiState.update { it.copy(showAddSheet = false, formError = null) }

    fun onToggleNewAsset(isNew: Boolean) = _uiState.update {
        it.copy(isNewAsset = isNew, formInvestmentId = "", formInvestmentName = "",
            newAssetName = "", formError = null)
    }

    // Ativo existente
    fun onFormInvestmentSelected(id: String, name: String) =
        _uiState.update { it.copy(formInvestmentId = id, formInvestmentName = name, formError = null) }

    // Novo ativo
    fun onNewAssetNameChanged(v: String) = _uiState.update { it.copy(newAssetName = v, formError = null) }
    fun onNewAssetTypeChanged(type: InvestmentType) = _uiState.update { it.copy(newAssetType = type) }

    // Lançamento
    fun onFormTypeChanged(type: EntryType) =
        _uiState.update { it.copy(formType = type, formShares = "", formSharePrice = "", formNewCurrentValue = "") }
    fun onFormSharesChanged(v: String) = _uiState.update { it.copy(formShares = v, formError = null) }
    fun onFormSharePriceChanged(v: String) = _uiState.update { it.copy(formSharePrice = v, formError = null) }
    fun onFormNewCurrentValueChanged(v: String) = _uiState.update { it.copy(formNewCurrentValue = v, formError = null) }
    fun onFormNotesChanged(v: String) = _uiState.update { it.copy(formNotes = v) }
    fun onFormDateChanged(v: String) = _uiState.update { it.copy(formDate = v) }

    fun onSaveEntry() {
        val state = _uiState.value

        // Valida campos de aporte/resgate
        val shares = state.formShares.replace(",", ".").toDoubleOrNull()
        val price = state.formSharePrice.replace(",", ".").toDoubleOrNull()
        val newValue = state.formNewCurrentValue.replace(",", ".").toDoubleOrNull()

        when (state.formType) {
            EntryType.APORTE, EntryType.RESGATE -> {
                if (shares == null || shares <= 0) { _uiState.update { it.copy(formError = "Informe a quantidade de cotas") }; return }
                if (price == null || price <= 0) { _uiState.update { it.copy(formError = "Informe o preço por cota") }; return }
            }
            EntryType.ATUALIZACAO_VALOR -> {
                if (newValue == null || newValue < 0) { _uiState.update { it.copy(formError = "Informe o novo valor") }; return }
            }
        }

        if (state.isNewAsset) {
            // Cria ativo novo e depois lança
            if (state.newAssetName.isBlank()) { _uiState.update { it.copy(formError = "Informe o nome do ativo") }; return }
            saveWithNewAsset(state, shares, price, newValue)
        } else {
            // Lança em ativo existente
            if (state.formInvestmentId.isBlank()) { _uiState.update { it.copy(formError = "Selecione o ativo") }; return }
            saveEntry(state.formInvestmentId, state, shares, price, newValue)
        }
    }

    private fun saveWithNewAsset(
        state: InvestmentBookUiState,
        shares: Double?, price: Double?, newValue: Double?,
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            // Calcula valor inicial baseado nos campos do aporte
            val investedValue = if (shares != null && price != null) shares * price else 0.0
            val currentValue = newValue ?: investedValue

            val createResult = investmentRepository.createInvestment(
                name = state.newAssetName.trim(),
                type = state.newAssetType,
                currentValue = currentValue,
                investedValue = investedValue,
                shares = shares,
                allocationTarget = null,
            )

            when (createResult) {
                is Resource.Success -> {
                    val newInvestmentId = createResult.data.id
                    saveEntry(newInvestmentId, state, shares, price, newValue)
                    // Recarrega lista de ativos
                    loadInvestments()
                }
                is Resource.Error -> _uiState.update { it.copy(isSaving = false, formError = createResult.message) }
                is Resource.Loading -> Unit
            }
        }
    }

    private fun saveEntry(investmentId: String, state: InvestmentBookUiState, shares: Double?, price: Double?, newValue: Double?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val result = investmentRepository.addEntry(
                investmentId = investmentId,
                type = state.formType,
                shares = shares,
                sharePrice = price,
                newCurrentValue = newValue,
                notes = state.formNotes.ifBlank { null },
                date = state.formDate,
            )
            when (result) {
                is Resource.Success -> { _uiState.update { it.copy(isSaving = false) }; onDismissSheet(); loadEntries() }
                is Resource.Error -> _uiState.update { it.copy(isSaving = false, formError = result.message) }
                is Resource.Loading -> Unit
            }
        }
    }

    // ─── Exclusão ───

    fun onDeleteRequest(entry: InvestmentEntry) =
        _uiState.update { it.copy(showDeleteDialog = true, deletingEntry = entry) }

    fun onDeleteCancel() = _uiState.update { it.copy(showDeleteDialog = false, deletingEntry = null) }

    fun onDeleteConfirm() {
        val entry = _uiState.value.deletingEntry ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(showDeleteDialog = false, deletingEntry = null) }
            if (investmentRepository.deleteEntry(entry.id) is Resource.Success) loadEntries()
        }
    }
}