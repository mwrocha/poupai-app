package io.poupai.app.features.dashboard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.poupai.app.core.network.Resource
import io.poupai.app.core.util.PreferencesManager
import io.poupai.app.domain.model.Transaction
import io.poupai.app.domain.model.TransactionType
import io.poupai.app.domain.repository.GamificationRepository
import io.poupai.app.domain.repository.GoalRepository
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
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val transactionRepository: TransactionRepository,
    private val goalRepository: GoalRepository,
    private val gamificationRepository: GamificationRepository,
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
        loadUserInfo()
        loadTransactionsAndSummary()
        loadGoals()
        loadGamification()
    }

    private fun loadUserInfo() {
        viewModelScope.launch {
            val firstName = preferencesManager.getFirstNameSync()?.ifBlank { null } ?: "Usuário"
            val profileImageUrl = preferencesManager.getProfileImageUrlSync()
            _uiState.update { it.copy(userName = firstName, profileImageUrl = profileImageUrl) }
        }
    }

    private fun loadTransactionsAndSummary() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            transactionRepository.getTransactions().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        val transactions = result.data
                        val today = LocalDate.now()

                        // Resumo do mês atual
                        val monthTx = transactions.filter { t ->
                            val d = t.date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                            d.year == today.year && d.monthValue == today.monthValue
                        }
                        val monthIncome = monthTx.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
                        val monthExpense = monthTx.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

                        // Total geral
                        val totalIncome = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
                        val totalExpense = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

                        // 5 últimas transações
                        val recent = transactions.sortedByDescending { it.date }.take(5)

                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                totalSaved = totalIncome - totalExpense,
                                monthIncome = monthIncome,
                                monthExpense = monthExpense,
                                monthlyData = buildMonthlyData(transactions),
                                recentTransactions = recent,
                            )
                        }
                    }
                    is Resource.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                    is Resource.Loading -> Unit
                }
            }
        }
    }

    private fun loadGoals() {
        viewModelScope.launch {
            goalRepository.getGoals().collect { result ->
                if (result is Resource.Success) {
                    // Só metas ainda não concluídas, ordenadas por progresso descrescente
                    val active = result.data
                        .filter { it.currentValue < it.targetValue }
                        .sortedByDescending { it.currentValue / it.targetValue }
                        .take(3)
                    _uiState.update { it.copy(activeGoals = active) }
                }
            }
        }
    }

    private fun loadGamification() {
        viewModelScope.launch {
            gamificationRepository.getStatus().collect { result ->
                if (result is Resource.Success) {
                    _uiState.update {
                        it.copy(
                            currentStreak = result.data.currentStreak,
                            totalPoints = result.data.totalPoints,
                        )
                    }
                }
            }
        }
    }

    private fun buildMonthlyData(transactions: List<Transaction>): List<MonthData> {
        val today = LocalDate.now()
        return (5 downTo 0).map { monthsAgo ->
            val month = today.minusMonths(monthsAgo.toLong())
            val monthTx = transactions.filter { t ->
                val d = t.date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                d.year == month.year && d.monthValue == month.monthValue
            }
            MonthData(
                label = month.month.getDisplayName(TextStyle.SHORT, Locale("pt", "BR")).replaceFirstChar { it.uppercase() },
                income = monthTx.filter { it.type == TransactionType.INCOME }.sumOf { it.amount },
                expense = monthTx.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount },
            )
        }
    }
}