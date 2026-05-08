package io.poupai.app.features.finances.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.poupai.app.core.network.Resource
import io.poupai.app.core.util.PreferencesManager
import io.poupai.app.domain.repository.FinanceRepository
import io.poupai.app.domain.repository.TagRepository
import io.poupai.app.features.finances.state.FinancesUiState
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
class FinancesViewModel @Inject constructor(
    private val financeRepository: FinanceRepository,
    private val tagRepository: TagRepository,
    private val preferencesManager: PreferencesManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(FinancesUiState())
    val uiState: StateFlow<FinancesUiState> = _uiState.asStateFlow()

    companion object { private const val MONTHS = 6 }

    init {
        observeHideValues()
        loadAll()
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

    fun loadAll() {
        loadFinances()
        loadCategoryDistribution()
    }

    private fun loadFinances() {
        viewModelScope.launch {
            financeRepository.getFinanceSummary(MONTHS).collect { result ->
                when (result) {
                    is Resource.Loading -> _uiState.update { it.copy(isLoading = true) }
                    is Resource.Success -> _uiState.update {
                        it.copy(
                            isLoading = false,
                            incomeHistory = result.data.incomeHistory,
                            expenseHistory = result.data.expenseHistory,
                            profitHistory = result.data.profitHistory,
                            monthLabels = buildMonthLabels(MONTHS),
                        )
                    }
                    is Resource.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
            }
        }
    }

    private fun loadCategoryDistribution() {
        viewModelScope.launch {
            val now = LocalDate.now()
            tagRepository.getTagsSummary(month = now.monthValue, year = now.year).collect { result ->
                if (result is Resource.Success) {
                    _uiState.update {
                        it.copy(
                            categoryDistribution = result.data.take(5),
                            totalExpense = result.data.sumOf { tag -> tag.totalSpent },
                        )
                    }
                }
            }
        }
    }

    private fun buildMonthLabels(months: Int): List<String> {
        val now = LocalDate.now()
        return (months - 1 downTo 0).map { offset ->
            now.minusMonths(offset.toLong()).month
                .getDisplayName(TextStyle.SHORT, Locale("pt", "BR"))
                .replaceFirstChar { it.uppercase() }
        }
    }
}