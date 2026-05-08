package io.poupai.app.features.investments.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.poupai.app.core.network.Resource
import io.poupai.app.core.util.PreferencesManager
import io.poupai.app.domain.model.Investment
import io.poupai.app.domain.model.InvestmentType
import io.poupai.app.domain.repository.InvestmentRepository
import io.poupai.app.domain.usecase.investment.AddInvestmentUseCase
import io.poupai.app.domain.usecase.investment.GetInvestmentsUseCase
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
    private val getInvestmentsUseCase: GetInvestmentsUseCase,
    private val addInvestmentUseCase: AddInvestmentUseCase,
    private val investmentRepository: InvestmentRepository,
    private val preferencesManager: PreferencesManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(InvestmentsUiState())
    val uiState: StateFlow<InvestmentsUiState> = _uiState.asStateFlow()

    init {
        observeHideValues()
        loadInvestments()
    }

    private fun observeHideValues() {
        viewModelScope.launch {
            preferencesManager.hideValues.collect { hide ->
                _uiState.update { it.copy(hideValues = hide) }
            }
        }
    }

    fun toggleHideValues() {
        viewModelScope.launch {
            preferencesManager.saveHideValues(!_uiState.value.hideValues)
        }
    }

    fun loadInvestments() {
        viewModelScope.launch {
            getInvestmentsUseCase().collect { result ->
                when (result) {
                    is Resource.Loading -> _uiState.update { it.copy(isLoading = true) }
                    is Resource.Success -> {
                        val grouped = result.data.groupBy { it.type }
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                rendaVariavel = grouped[InvestmentType.RENDA_VARIAVEL].orEmpty(),
                                rendaFixa = grouped[InvestmentType.RENDA_FIXA].orEmpty(),
                                criptomoedas = grouped[InvestmentType.CRIPTOMOEDAS].orEmpty(),
                            )
                        }
                    }
                    is Resource.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
            }
        }
    }

    fun onShowAddSheet() = _uiState.update { it.copy(showAddSheet = true) }

    fun onDismissSheet() = _uiState.update {
        it.copy(showAddSheet = false, formName = "", formType = InvestmentType.RENDA_VARIAVEL,
            formCurrentValue = "", formInvestedValue = "", formError = null)
    }

    fun onFormNameChanged(value: String) = _uiState.update { it.copy(formName = value, formError = null) }
    fun onFormTypeChanged(type: InvestmentType) = _uiState.update { it.copy(formType = type) }
    fun onFormCurrentValueChanged(value: String) = _uiState.update { it.copy(formCurrentValue = value, formError = null) }
    fun onFormInvestedValueChanged(value: String) = _uiState.update { it.copy(formInvestedValue = value, formError = null) }

    fun onAddInvestment() {
        val state = _uiState.value
        val invested = state.formInvestedValue.replace(",", ".").toDoubleOrNull()
        val current = state.formCurrentValue.replace(",", ".").toDoubleOrNull()
        when {
            state.formName.isBlank() -> { _uiState.update { it.copy(formError = "Informe o nome do ativo") }; return }
            invested == null || invested <= 0 -> { _uiState.update { it.copy(formError = "Valor investido inválido") }; return }
            current == null || current <= 0 -> { _uiState.update { it.copy(formError = "Valor atual inválido") }; return }
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, formError = null) }
            when (val result = addInvestmentUseCase(name = state.formName.trim(), type = state.formType, currentValue = current!!, investedValue = invested!!)) {
                is Resource.Success -> { onDismissSheet(); loadInvestments() }
                is Resource.Error -> _uiState.update { it.copy(isSaving = false, formError = result.message) }
                is Resource.Loading -> Unit
            }
        }
    }

    fun onEditRequest(investment: Investment) = _uiState.update {
        it.copy(showEditSheet = true, editingInvestment = investment, editName = investment.name,
            editCurrentValue = investment.currentValue.toString(), editInvestedValue = investment.investedValue.toString(), editError = null)
    }

    fun onDismissEditSheet() = _uiState.update {
        it.copy(showEditSheet = false, editingInvestment = null, editName = "",
            editCurrentValue = "", editInvestedValue = "", editError = null)
    }

    fun onEditNameChanged(value: String) = _uiState.update { it.copy(editName = value, editError = null) }
    fun onEditCurrentValueChanged(value: String) = _uiState.update { it.copy(editCurrentValue = value, editError = null) }
    fun onEditInvestedValueChanged(value: String) = _uiState.update { it.copy(editInvestedValue = value, editError = null) }

    fun onUpdateInvestment() {
        val state = _uiState.value
        val investment = state.editingInvestment ?: return
        val invested = state.editInvestedValue.replace(",", ".").toDoubleOrNull()
        val current = state.editCurrentValue.replace(",", ".").toDoubleOrNull()
        when {
            state.editName.isBlank() -> { _uiState.update { it.copy(editError = "Informe o nome do ativo") }; return }
            invested == null || invested <= 0 -> { _uiState.update { it.copy(editError = "Valor investido inválido") }; return }
            current == null || current <= 0 -> { _uiState.update { it.copy(editError = "Valor atual inválido") }; return }
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isUpdating = true, editError = null) }
            when (val result = investmentRepository.updateInvestment(id = investment.id, name = state.editName.trim(),
                type = investment.type, currentValue = current!!, investedValue = invested!!)) {
                is Resource.Success -> { onDismissEditSheet(); loadInvestments() }
                is Resource.Error -> _uiState.update { it.copy(isUpdating = false, editError = result.message) }
                is Resource.Loading -> Unit
            }
        }
    }

    fun onDeleteRequest(investment: Investment) = _uiState.update {
        it.copy(showDeleteDialog = true, deletingInvestment = investment)
    }

    fun onDeleteCancel() = _uiState.update { it.copy(showDeleteDialog = false, deletingInvestment = null) }

    fun onDeleteConfirm() {
        val investment = _uiState.value.deletingInvestment ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(showDeleteDialog = false, deletingInvestment = null, isDeleting = true) }
            when (investmentRepository.deleteInvestment(investment.id)) {
                is Resource.Success -> { _uiState.update { it.copy(isDeleting = false) }; loadInvestments() }
                is Resource.Error -> _uiState.update { it.copy(isDeleting = false, errorMessage = "Erro ao excluir investimento") }
                is Resource.Loading -> Unit
            }
        }
    }
}