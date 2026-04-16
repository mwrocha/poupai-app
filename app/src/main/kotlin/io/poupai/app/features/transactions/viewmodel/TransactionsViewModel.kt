package io.poupai.app.features.transactions.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.poupai.app.core.network.Resource
import io.poupai.app.domain.model.TransactionType
import io.poupai.app.domain.usecase.transaction.GetTransactionsUseCase
import io.poupai.app.features.transactions.state.TransactionsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransactionsViewModel @Inject constructor(
    private val getTransactionsUseCase: GetTransactionsUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionsUiState())
    val uiState: StateFlow<TransactionsUiState> = _uiState.asStateFlow()

    init {
        loadTransactions()
    }

    private fun loadTransactions() {
        viewModelScope.launch {
            getTransactionsUseCase().collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                    is Resource.Success -> {
                        val transactions = result.data
                        val income = transactions
                            .filter { it.type == TransactionType.INCOME }
                            .sumOf { it.amount }
                        val expense = transactions
                            .filter { it.type == TransactionType.EXPENSE }
                            .sumOf { it.amount }
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                balance = income - expense,
                                incomeTotal = income,
                                expenseTotal = expense,
                                recentTransactions = transactions.take(10),
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(isLoading = false, errorMessage = result.message)
                        }
                    }
                }
            }
        }
    }
}