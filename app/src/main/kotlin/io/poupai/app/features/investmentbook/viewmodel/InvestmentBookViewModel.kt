package io.poupai.app.features.investmentbook.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.poupai.app.core.network.InvestmentEvents
import io.poupai.app.core.network.Resource
import io.poupai.app.domain.model.EntryType
import io.poupai.app.domain.model.InvestmentEntry
import io.poupai.app.domain.model.InvestmentType
import io.poupai.app.domain.repository.InvestmentRepository
import io.poupai.app.features.investmentbook.state.InvestmentBookUiState
import io.poupai.app.features.investmentbook.state.InvestmentEntryFormState
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
        val state = _uiState.value.listState
        viewModelScope.launch {
            _uiState.update { it.copy(listState = it.listState.copy(isLoading = true)) }
            when (val result = investmentRepository.getEntries(
                investmentId = state.selectedInvestmentId,
                year = state.selectedYear,
                month = state.selectedMonth,
            )) {
                is Resource.Success -> _uiState.update {
                    it.copy(listState = it.listState.copy(
                        isLoading = false,
                        entries = result.data.entries,
                        totalAported = result.data.totalAported,
                        totalRescued = result.data.totalRescued,
                        totalEntries = result.data.totalEntries,
                    ))
                }
                is Resource.Error -> _uiState.update {
                    it.copy(listState = it.listState.copy(isLoading = false, errorMessage = result.message))
                }
                is Resource.Loading -> Unit
            }
        }
    }

    private fun loadInvestments() {
        viewModelScope.launch {
            val result = investmentRepository.getInvestments().first { it !is Resource.Loading }
            if (result is Resource.Success) _uiState.update {
                it.copy(listState = it.listState.copy(investments = result.data))
            }
        }
    }

    // ─── Filtros ───

    fun onFilterInvestment(id: String?, name: String?) {
        _uiState.update { it.copy(listState = it.listState.copy(selectedInvestmentId = id, selectedInvestmentName = name)) }
        loadEntries()
    }

    fun onClearFilters() {
        _uiState.update {
            it.copy(listState = it.listState.copy(
                selectedInvestmentId = null,
                selectedInvestmentName = null,
                selectedMonth = null,
                selectedYear = null,
            ))
        }
        loadEntries()
    }

    // ─── Formulário ───

    fun onShowAddSheet() = _uiState.update {
        it.copy(formState = InvestmentEntryFormState(showSheet = true))
    }

    fun onDismissSheet() = _uiState.update {
        it.copy(formState = it.formState.copy(showSheet = false, generalError = null))
    }

    fun onToggleNewAsset(isNew: Boolean) = _uiState.update {
        it.copy(formState = it.formState.copy(
            isNewAsset = isNew,
            formInvestmentId = "",
            formInvestmentName = "",
            newAssetName = "",
            generalError = null,
        ))
    }

    fun onFormInvestmentSelected(id: String, name: String) = _uiState.update {
        it.copy(formState = it.formState.copy(
            formInvestmentId = id,
            formInvestmentName = name,
            fieldErrors = it.formState.fieldErrors - "formInvestmentId",
            generalError = null,
        ))
    }

    fun onNewAssetNameChanged(v: String) = updateFormField("newAssetName", v) {
        copy(newAssetName = v, generalError = null)
    }

    fun onNewAssetTypeChanged(type: InvestmentType) = _uiState.update {
        it.copy(formState = it.formState.copy(newAssetType = type))
    }

    fun onFormTypeChanged(type: EntryType) = _uiState.update {
        it.copy(formState = it.formState.copy(
            formType = type,
            formShares = "",
            formSharePrice = "",
            formNewCurrentValue = "",
            fieldErrors = emptyMap(),
        ))
    }

    fun onFormSharesChanged(v: String) = updateFormField("formShares", v) {
        copy(formShares = v, generalError = null)
    }

    fun onFormSharePriceChanged(v: String) = updateFormField("formSharePrice", v) {
        copy(formSharePrice = v, generalError = null)
    }

    fun onFormNewCurrentValueChanged(v: String) = updateFormField("formNewCurrentValue", v) {
        copy(formNewCurrentValue = v, generalError = null)
    }

    fun onFormNotesChanged(v: String) = _uiState.update {
        it.copy(formState = it.formState.copy(formNotes = v))
    }

    fun onFormDateChanged(v: String) = updateFormField("formDate", v) {
        copy(formDate = v, generalError = null)
    }

    // ─── Validação reativa ───

    private fun updateFormField(field: String, value: String, update: InvestmentEntryFormState.() -> InvestmentEntryFormState) {
        _uiState.update { state ->
            val currentForm = state.formState
            val updatedForm = currentForm.update()
            val newFieldErrors = if (currentForm.hasSubmittedOnce) {
                val error = validateField(field, value)
                if (error != null) updatedForm.fieldErrors + (field to error)
                else updatedForm.fieldErrors - field
            } else {
                updatedForm.fieldErrors
            }
            state.copy(formState = updatedForm.copy(fieldErrors = newFieldErrors))
        }
    }

    private fun validateField(field: String, value: String): String? {
        return when (field) {
            "newAssetName" -> if (value.isBlank()) "Informe o nome do ativo" else null
            "formInvestmentId" -> if (value.isBlank()) "Selecione o ativo" else null
            "formShares" -> {
                val num = value.replace(",", ".").toDoubleOrNull()
                if (num == null || num <= 0) "Informe a quantidade de cotas" else null
            }
            "formSharePrice" -> {
                val num = value.replace(",", ".").toDoubleOrNull()
                if (num == null || num <= 0) "Informe o preço por cota" else null
            }
            "formNewCurrentValue" -> {
                val num = value.replace(",", ".").toDoubleOrNull()
                if (num == null || num < 0) "Informe o novo valor" else null
            }
            "formDate" -> {
                try { LocalDate.parse(value); null }
                catch (_: Exception) { "Data inválida (use yyyy-MM-dd)" }
            }
            else -> null
        }
    }

    private fun validateAll(form: InvestmentEntryFormState): Map<String, String> {
        val errors = mutableMapOf<String, String>()
        when (form.formType) {
            EntryType.APORTE, EntryType.RESGATE -> {
                validateField("formShares", form.formShares)?.let { errors["formShares"] = it }
                validateField("formSharePrice", form.formSharePrice)?.let { errors["formSharePrice"] = it }
            }
            EntryType.ATUALIZACAO_VALOR -> {
                validateField("formNewCurrentValue", form.formNewCurrentValue)?.let { errors["formNewCurrentValue"] = it }
            }
        }
        if (form.isNewAsset) {
            validateField("newAssetName", form.newAssetName)?.let { errors["newAssetName"] = it }
        } else {
            validateField("formInvestmentId", form.formInvestmentId)?.let { errors["formInvestmentId"] = it }
        }
        validateField("formDate", form.formDate)?.let { errors["formDate"] = it }
        return errors
    }

    // ─── Salvar ───

    fun onSaveEntry() {
        val state = _uiState.value
        val form = state.formState

        val allErrors = validateAll(form)
        val hasErrors = allErrors.isNotEmpty()

        _uiState.update {
            it.copy(formState = it.formState.copy(
                hasSubmittedOnce = true,
                fieldErrors = allErrors,
                generalError = if (hasErrors) "Verifique os campos acima" else null,
            ))
        }

        if (hasErrors) return

        val shares = form.formShares.replace(",", ".").toDoubleOrNull()
        val price = form.formSharePrice.replace(",", ".").toDoubleOrNull()
        val newValue = form.formNewCurrentValue.replace(",", ".").toDoubleOrNull()

        if (form.isNewAsset) {
            saveWithNewAsset(form, shares, price, newValue)
        } else {
            viewModelScope.launch {
                saveEntry(form.formInvestmentId, form, shares, price, newValue)
            }
        }
    }

    private fun saveWithNewAsset(
        form: InvestmentEntryFormState,
        shares: Double?, price: Double?, newValue: Double?,
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(formState = it.formState.copy(isSaving = true)) }
            val investedValue = if (shares != null && price != null) shares * price else 0.0
            val currentValue = newValue ?: investedValue

            val createResult = investmentRepository.createInvestment(
                name = form.newAssetName.trim(), type = form.newAssetType,
                currentValue = currentValue, investedValue = investedValue,
                shares = shares,
                allocationTarget = null,
            )

            when (createResult) {
                is Resource.Success -> {
                    _uiState.update { it.copy(formState = it.formState.copy(isSaving = false)) }
                    onDismissSheet()
                    loadInvestments()
                    loadEntries()
                    InvestmentEvents.notifyEntriesChanged()
                }
                is Resource.Error -> _uiState.update {
                    it.copy(formState = it.formState.copy(isSaving = false, generalError = createResult.message))
                }
                is Resource.Loading -> Unit
            }
        }
    }

    private suspend fun saveEntry(
        investmentId: String, form: InvestmentEntryFormState,
        shares: Double?, price: Double?, newValue: Double?,
    ) {
        _uiState.update { it.copy(formState = it.formState.copy(isSaving = true)) }
        val result = investmentRepository.addEntry(
            investmentId = investmentId,
            type = form.formType,
            shares = shares,
            sharePrice = price,
            newCurrentValue = newValue,
            adjustedShares = null,
            adjustedAveragePrice = null,
            notes = form.formNotes.ifBlank { null },
            date = form.formDate,
        )
        when (result) {
            is Resource.Success -> {
                _uiState.update { it.copy(formState = it.formState.copy(isSaving = false)) }
                onDismissSheet()
                loadInvestments()
                loadEntries()
                InvestmentEvents.notifyEntriesChanged()
            }
            is Resource.Error -> _uiState.update {
                it.copy(formState = it.formState.copy(isSaving = false, generalError = result.message))
            }
            is Resource.Loading -> Unit
        }
    }

    // ─── Exclusão ───

    fun onDeleteRequest(entry: InvestmentEntry) = _uiState.update {
        it.copy(listState = it.listState.copy(showDeleteDialog = true, deletingEntry = entry))
    }

    fun onDeleteCancel() = _uiState.update {
        it.copy(listState = it.listState.copy(showDeleteDialog = false, deletingEntry = null))
    }

    fun onDeleteConfirm() {
        val entry = _uiState.value.listState.deletingEntry ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(listState = it.listState.copy(showDeleteDialog = false, deletingEntry = null)) }
            if (investmentRepository.deleteEntry(entry.id) is Resource.Success) {
                loadEntries()
                InvestmentEvents.notifyEntriesChanged()
            }
        }
    }
}
