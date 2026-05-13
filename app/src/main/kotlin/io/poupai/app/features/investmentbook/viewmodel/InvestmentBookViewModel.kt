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
                        totalEntries = result.data.totalEntries
                    )
                }

                is Resource.Error -> _uiState.update {
                    it.copy(
                        isLoading = false, errorMessage = result.message
                    )
                }

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

    fun onClearFilters() {
        _uiState.update {
            it.copy(
                selectedInvestmentId = null,
                selectedInvestmentName = null,
                selectedMonth = null,
                selectedYear = null
            )
        }
        loadEntries()
    }

    // ─── Formulário ───

    fun onShowAddSheet() = _uiState.update {
        it.copy(
            showAddSheet = true,
            isNewAsset = false,
            formInvestmentId = "",
            formInvestmentName = "",
            newAssetName = "",
            newAssetType = InvestmentType.RENDA_VARIAVEL,
            formType = EntryType.APORTE,
            formShares = "",
            formSharePrice = "",
            formNewCurrentValue = "",
            formAdjustedShares = "",
            formAdjustedAveragePrice = "",
            formNotes = "",
            formDate = LocalDate.now().toString(),
            formError = null
        )
    }

    fun onDismissSheet() = _uiState.update { it.copy(showAddSheet = false, formError = null) }

    fun onToggleNewAsset(isNew: Boolean) = _uiState.update {
        it.copy(
            isNewAsset = isNew,
            formInvestmentId = "",
            formInvestmentName = "",
            newAssetName = "",
            formError = null
        )
    }

    fun onFormInvestmentSelected(id: String, name: String) = _uiState.update {
        it.copy(
            formInvestmentId = id, formInvestmentName = name, formError = null
        )
    }

    fun onNewAssetNameChanged(v: String) =
        _uiState.update { it.copy(newAssetName = v, formError = null) }

    fun onNewAssetTypeChanged(type: InvestmentType) =
        _uiState.update { it.copy(newAssetType = type) }

    fun onFormTypeChanged(type: EntryType) = _uiState.update {
        it.copy(
            formType = type,
            formShares = "",
            formSharePrice = "",
            formNewCurrentValue = "",
            formAdjustedShares = "",
            formAdjustedAveragePrice = ""
        )
    }

    fun onFormSharesChanged(v: String) =
        _uiState.update { it.copy(formShares = v, formError = null) }

    fun onFormSharePriceChanged(v: String) =
        _uiState.update { it.copy(formSharePrice = v, formError = null) }

    fun onFormNewCurrentValueChanged(v: String) =
        _uiState.update { it.copy(formNewCurrentValue = v, formError = null) }

    fun onFormAdjustedSharesChanged(v: String) =
        _uiState.update { it.copy(formAdjustedShares = v, formError = null) }

    fun onFormAdjustedAvgPriceChanged(v: String) =
        _uiState.update { it.copy(formAdjustedAveragePrice = v, formError = null) }

    fun onFormNotesChanged(v: String) = _uiState.update { it.copy(formNotes = v) }
    fun onFormDateChanged(v: String) = _uiState.update { it.copy(formDate = v) }

    fun onSaveEntry() {
        val state = _uiState.value
        val shares = state.formShares.replace(",", ".").toDoubleOrNull()
        val price = state.formSharePrice.replace(",", ".").toDoubleOrNull()
        val newValue = state.formNewCurrentValue.replace(",", ".").toDoubleOrNull()
        val adjShares = state.formAdjustedShares.replace(",", ".").toDoubleOrNull()
        val adjAvgPrice = state.formAdjustedAveragePrice.replace(",", ".").toDoubleOrNull()

        // Validações por tipo
        when (state.formType) {
            EntryType.APORTE, EntryType.RESGATE -> {
                if (shares == null || shares <= 0) {
                    _uiState.update { it.copy(formError = "Informe a quantidade de cotas") }; return
                }
                if (price == null || price <= 0) {
                    _uiState.update { it.copy(formError = "Informe o preço por cota") }; return
                }
            }

            EntryType.ATUALIZACAO_VALOR -> {
                if (newValue == null || newValue < 0) {
                    _uiState.update { it.copy(formError = "Informe o novo valor") }; return
                }
            }

            EntryType.AJUSTE_POSICAO -> {
                if (adjShares == null || adjShares < 0) {
                    _uiState.update { it.copy(formError = "Informe a quantidade de cotas atual") }; return
                }
                if (adjAvgPrice == null || adjAvgPrice <= 0) {
                    _uiState.update { it.copy(formError = "Informe o preço médio atual") }; return
                }
            }
        }

        if (state.isNewAsset) {
            if (state.newAssetName.isBlank()) {
                _uiState.update { it.copy(formError = "Informe o nome do ativo") }; return
            }
            saveWithNewAsset(state, shares, price, newValue, adjShares, adjAvgPrice)
        } else {
            if (state.formInvestmentId.isBlank()) {
                _uiState.update { it.copy(formError = "Selecione o ativo") }; return
            }
            viewModelScope.launch {
                saveEntry(
                    state.formInvestmentId, state, shares, price, newValue, adjShares, adjAvgPrice
                )
            }
        }
    }

    private fun saveWithNewAsset(
        state: InvestmentBookUiState,
        shares: Double?, price: Double?, newValue: Double?,
        adjShares: Double?, adjAvgPrice: Double?,
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val investedValue = when (state.formType) {
                EntryType.AJUSTE_POSICAO -> (adjShares ?: 0.0) * (adjAvgPrice ?: 0.0)
                else -> if (shares != null && price != null) shares * price else 0.0
            }
            val currentValue = newValue ?: investedValue

            val createResult = investmentRepository.createInvestment(
                name = state.newAssetName.trim(), type = state.newAssetType,
                currentValue = currentValue, investedValue = investedValue,
                shares = if (state.formType == EntryType.AJUSTE_POSICAO) adjShares else shares,
                allocationTarget = null,
            )

            when (createResult) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isSaving = false) }
                    onDismissSheet()
                    loadInvestments()
                    loadEntries()
                }


                is Resource.Error -> _uiState.update {
                    it.copy(
                        isSaving = false, formError = createResult.message
                    )
                }

                is Resource.Loading -> Unit
            }
        }
    }

    private suspend fun saveEntry(
        investmentId: String, state: InvestmentBookUiState,
        shares: Double?, price: Double?, newValue: Double?,
        adjShares: Double?, adjAvgPrice: Double?,
    ) {
        _uiState.update { it.copy(isSaving = true) }
        val result = investmentRepository.addEntry(
            investmentId = investmentId,
            type = state.formType,
            shares = shares,
            sharePrice = price,
            newCurrentValue = newValue,
            adjustedShares = adjShares,
            adjustedAveragePrice = adjAvgPrice,
            notes = state.formNotes.ifBlank { null },
            date = state.formDate,
        )
        when (result) {
            is Resource.Success -> {
                _uiState.update { it.copy(isSaving = false) }
                onDismissSheet()
                loadInvestments()
                loadEntries()
            }
            is Resource.Error -> _uiState.update {
                it.copy(isSaving = false, formError = result.message)
            }
            is Resource.Loading -> Unit
        }
    }

    // ─── Exclusão ───

    fun onDeleteRequest(entry: InvestmentEntry) =
        _uiState.update { it.copy(showDeleteDialog = true, deletingEntry = entry) }

    fun onDeleteCancel() =
        _uiState.update { it.copy(showDeleteDialog = false, deletingEntry = null) }

    fun onDeleteConfirm() {
        val entry = _uiState.value.deletingEntry ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(showDeleteDialog = false, deletingEntry = null) }
            if (investmentRepository.deleteEntry(entry.id) is Resource.Success) loadEntries()
        }
    }
}