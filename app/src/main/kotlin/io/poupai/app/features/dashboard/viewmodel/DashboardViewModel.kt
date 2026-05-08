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
        observeHideValues()
        loadDashboard()
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

    fun loadDashboard() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val firstName = preferencesManager.getFirstNameSync()?.ifBlank { null } ?: "Usuário"
            val profileImageUrl = preferencesManager.getProfileImageUrlSync()
            _uiState.update { it.copy(userName = firstName, profileImageUrl = profileImageUrl) }

            transactionRepository.getTransactions().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        val transactions = result.data
                        val totalIncome = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
                        val totalExpense = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                totalSaved = totalIncome - totalExpense,
                                monthlyData = buildMonthlyData(transactions),
                            )
                        }
                    }
                    is Resource.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                    is Resource.Loading -> Unit
                }
            }
        }
    }

    private fun buildMonthlyData(transactions: List<io.poupai.app.domain.model.Transaction>): List<MonthData> {
        val today = LocalDate.now()
        return (11 downTo 0).map { monthsAgo ->
            val month = today.minusMonths(monthsAgo.toLong())
            val monthTx = transactions.filter { t ->
                val d = t.date.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                d.year == month.year && d.monthValue == month.monthValue
            }
            MonthData(
                label = month.month.getDisplayName(TextStyle.SHORT, Locale("pt", "BR")).replaceFirstChar { it.uppercase() },
                income = monthTx.filter { it.type == TransactionType.INCOME }.sumOf { it.amount },
                expense = monthTx.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount },
            )
        }
    }

    fun refresh() = loadDashboard()
}