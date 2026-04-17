package io.poupai.app.features.dashboard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.poupai.app.core.network.Resource
import io.poupai.app.core.util.PreferencesManager
import io.poupai.app.domain.model.TransactionType
import io.poupai.app.domain.repository.TransactionRepository
import io.poupai.app.features.dashboard.state.DashboardUiState
import io.poupai.app.features.dashboard.state.MonthData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val transactionRepository: TransactionRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboard()
    }

    fun loadDashboard() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Dados do usuário do DataStore
            val firstName = preferencesManager.getFirstNameSync()?.ifBlank { null } ?: "Usuário"
            val profileImageUrl = preferencesManager.getProfileImageUrlSync()
            _uiState.update { it.copy(userName = firstName, profileImageUrl = profileImageUrl) }

            // Sincroniza transações com a API e monta o gráfico
            transactionRepository.getTransactions().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        val transactions = result.data
                        val totalIncome = transactions
                            .filter { it.type == TransactionType.INCOME }
                            .sumOf { it.amount }
                        val totalExpense = transactions
                            .filter { it.type == TransactionType.EXPENSE }
                            .sumOf { it.amount }

                        // Monta dados dos últimos 12 meses
                        val monthlyData = buildMonthlyData(transactions)

                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                totalSaved = totalIncome - totalExpense,
                                monthlyData = monthlyData,
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(isLoading = false, errorMessage = result.message)
                        }
                    }
                    is Resource.Loading -> Unit
                }
            }
        }
    }

    private fun buildMonthlyData(
        transactions: List<io.poupai.app.domain.model.Transaction>,
    ): List<MonthData> {
        val today = LocalDate.now()
        val months = (11 downTo 0).map { monthsAgo ->
            today.minusMonths(monthsAgo.toLong())
        }

        return months.map { month ->
            val monthTransactions = transactions.filter { transaction ->
                val transactionDate = transaction.date.toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate()
                transactionDate.year == month.year &&
                        transactionDate.monthValue == month.monthValue
            }

            val income = monthTransactions
                .filter { it.type == TransactionType.INCOME }
                .sumOf { it.amount }
            val expense = monthTransactions
                .filter { it.type == TransactionType.EXPENSE }
                .sumOf { it.amount }

            MonthData(
                label = month.month.getDisplayName(TextStyle.SHORT, Locale("pt", "BR"))
                    .replaceFirstChar { it.uppercase() },
                income = income,
                expense = expense,
            )
        }
    }

    fun refresh() = loadDashboard()
}