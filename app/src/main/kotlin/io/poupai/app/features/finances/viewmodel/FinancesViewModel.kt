package io.poupai.app.features.finances.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.poupai.app.core.network.Resource
import io.poupai.app.core.util.PreferencesManager
import io.poupai.app.domain.repository.FinanceRepository
import io.poupai.app.features.finances.state.FinancesUiState
import io.poupai.app.features.finances.state.PeriodFilter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class FinancesViewModel @Inject constructor(
    private val financeRepository: FinanceRepository,
    private val preferencesManager: PreferencesManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(FinancesUiState())
    val uiState: StateFlow<FinancesUiState> = _uiState.asStateFlow()

    init {
        observeHideValues()
        loadFinances()
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

    // ─── Filtro de período ───

    fun onPeriodSelected(period: PeriodFilter) {
        _uiState.update { it.copy(selectedPeriod = period) }
        loadFinances()
    }

    fun onMonthYearSelected(month: Int, year: Int) {
        _uiState.update {
            it.copy(
                selectedMonth = month,
                selectedYear = year,
                selectedPeriod = PeriodFilter.CUSTOM_MONTH,
            )
        }
        loadFinances()
    }

    fun loadFinances() {
        val state = _uiState.value
        viewModelScope.launch {
            val flow = when (state.selectedPeriod) {
                PeriodFilter.MONTHS_3 -> financeRepository.getFinanceSummary(3)
                PeriodFilter.MONTHS_6 -> financeRepository.getFinanceSummary(6)
                PeriodFilter.MONTHS_12 -> financeRepository.getFinanceSummary(12)
                PeriodFilter.CUSTOM_MONTH -> financeRepository.getFinanceSummaryByPeriod(
                    state.selectedMonth, state.selectedYear
                )
            }

            flow.collect { result ->
                when (result) {
                    is Resource.Loading -> _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                    is Resource.Success -> _uiState.update {
                        it.copy(
                            isLoading = false,
                            incomeHistory = result.data.incomeHistory,
                            expenseHistory = result.data.expenseHistory,
                            profitHistory = result.data.profitHistory,
                            monthLabels = result.data.monthLabels,
                            totalIncome = result.data.totalIncome,
                            totalExpense = result.data.totalExpense,
                            totalProfit = result.data.totalProfit,
                            incomeChangePercent = result.data.incomeChangePercent,
                            expenseChangePercent = result.data.expenseChangePercent,
                            profitChangePercent = result.data.profitChangePercent,
                            avgDailyExpense = result.data.avgDailyExpense,
                            avgMonthlyExpense = result.data.avgMonthlyExpense,
                            projectedMonthlyExpense = result.data.projectedMonthlyExpense,
                            biggestExpenseTitle = result.data.biggestExpenseTitle,
                            biggestExpenseAmount = result.data.biggestExpenseAmount,
                            categoryBreakdown = result.data.categoryBreakdown,
                        )
                    }
                    is Resource.Error -> _uiState.update {
                        it.copy(isLoading = false, errorMessage = result.message)
                    }
                }
            }
        }
    }
}