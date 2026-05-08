package io.poupai.app.features.transactions.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.poupai.app.core.network.Resource
import io.poupai.app.core.util.PreferencesManager
import io.poupai.app.domain.model.Transaction
import io.poupai.app.domain.model.TransactionType
import io.poupai.app.domain.repository.TransactionRepository
import io.poupai.app.domain.usecase.transaction.AddTransactionUseCase
import io.poupai.app.domain.usecase.transaction.GetTransactionsUseCase
import io.poupai.app.features.transactions.state.TransactionFilter
import io.poupai.app.features.transactions.state.TransactionsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class TransactionsViewModel @Inject constructor(
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val addTransactionUseCase: AddTransactionUseCase,
    private val transactionRepository: TransactionRepository,
    private val preferencesManager: PreferencesManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionsUiState())
    val uiState: StateFlow<TransactionsUiState> = _uiState.asStateFlow()

    init {
        observeHideValues()
        loadTransactions()
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

    fun loadTransactions() {
        viewModelScope.launch {
            getTransactionsUseCase().collect { result ->
                when (result) {
                    is Resource.Loading -> _uiState.update { it.copy(isLoading = true) }
                    is Resource.Success -> {
                        val transactions = result.data
                        val income = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
                        val expense = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
                        _uiState.update {
                            it.copy(isLoading = false, balance = income - expense,
                                incomeTotal = income, expenseTotal = expense, allTransactions = transactions)
                        }
                    }
                    is Resource.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
            }
        }
    }

    fun onFilterChanged(filter: TransactionFilter) = _uiState.update { it.copy(activeFilter = filter) }

    fun onPreviousMonth() {
        val state = _uiState.value
        val date = LocalDate.of(state.selectedYear, state.selectedMonth, 1).minusMonths(1)
        _uiState.update { it.copy(selectedMonth = date.monthValue, selectedYear = date.year) }
    }

    fun onNextMonth() {
        val state = _uiState.value
        val date = LocalDate.of(state.selectedYear, state.selectedMonth, 1).plusMonths(1)
        _uiState.update { it.copy(selectedMonth = date.monthValue, selectedYear = date.year) }
    }

    fun onDeleteRequest(transaction: Transaction) =
        _uiState.update { it.copy(showDeleteDialog = true, transactionToDelete = transaction) }

    fun onDeleteCancel() =
        _uiState.update { it.copy(showDeleteDialog = false, transactionToDelete = null) }

    fun onDeleteConfirm() {
        val transaction = _uiState.value.transactionToDelete ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(showDeleteDialog = false, transactionToDelete = null, deletingId = transaction.id) }
            when (transactionRepository.deleteTransaction(transaction.id)) {
                is Resource.Success -> loadTransactions()
                is Resource.Error -> _uiState.update { it.copy(deletingId = null, errorMessage = "Erro ao deletar transação") }
                is Resource.Loading -> Unit
            }
            _uiState.update { it.copy(deletingId = null) }
        }
    }

    fun onShowAddSheet() {
        val today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        _uiState.update {
            it.copy(showAddSheet = true, formTitle = "", formAmount = "",
                formType = TransactionType.EXPENSE, formCategory = "", formDate = today, formError = null)
        }
    }

    fun onDismissSheet() = _uiState.update { it.copy(showAddSheet = false) }
    fun onFormTitleChanged(value: String) = _uiState.update { it.copy(formTitle = value, formError = null) }
    fun onFormAmountChanged(value: String) = _uiState.update { it.copy(formAmount = value, formError = null) }
    fun onFormTypeChanged(type: TransactionType) = _uiState.update { it.copy(formType = type) }
    fun onFormCategoryChanged(value: String) = _uiState.update { it.copy(formCategory = value, formError = null) }
    fun onFormDateChanged(value: String) = _uiState.update { it.copy(formDate = value, formError = null) }

    fun onAddTransaction() {
        val state = _uiState.value
        if (!state.isFormValid) { _uiState.update { it.copy(formError = "Preencha todos os campos") }; return }
        viewModelScope.launch {
            _uiState.update { it.copy(formIsLoading = true) }
            val result = addTransactionUseCase(
                title = state.formTitle,
                amount = state.formAmount.replace(",", ".").toDouble(),
                type = state.formType,
                category = state.formCategory,
                date = convertDate(state.formDate),
            )
            when (result) {
                is Resource.Success -> { _uiState.update { it.copy(showAddSheet = false, formIsLoading = false) }; loadTransactions() }
                is Resource.Error -> _uiState.update { it.copy(formIsLoading = false, formError = result.message) }
                is Resource.Loading -> Unit
            }
        }
    }

    private fun convertDate(date: String): String {
        if (date.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) return date
        return try {
            LocalDate.parse(date, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        } catch (e: Exception) {
            LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        }
    }
}