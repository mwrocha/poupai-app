package io.poupai.app.features.allocation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.poupai.app.core.network.Resource
import io.poupai.app.core.util.PreferencesManager
import io.poupai.app.domain.model.Investment
import io.poupai.app.domain.model.InvestmentType
import io.poupai.app.domain.model.ProfitabilitySnapshot
import io.poupai.app.domain.repository.InvestmentRepository
import io.poupai.app.features.allocation.state.AllocationUiState
import io.poupai.app.features.allocation.state.InvestmentPerformance
import io.poupai.app.features.allocation.state.PortfolioWindowReturns
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class AllocationViewModel @Inject constructor(
    private val investmentRepository: InvestmentRepository,
    private val preferencesManager: PreferencesManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AllocationUiState())
    val uiState: StateFlow<AllocationUiState> = _uiState.asStateFlow()

    init {
        observeHideValues()
        loadData()
    }

    private fun observeHideValues() {
        viewModelScope.launch {
            preferencesManager.hideValues.collect { hide ->
                _uiState.update { it.copy(hideValues = hide) }
            }
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            investmentRepository.getInvestments().collect { result ->
                when (result) {
                    is Resource.Loading -> _uiState.update { it.copy(isLoading = true) }
                    is Resource.Success -> {
                        val investments = result.data
                        val grouped = investments.groupBy { it.type }
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                rendaVariavel = grouped[InvestmentType.RENDA_VARIAVEL].orEmpty(),
                                rendaFixa = grouped[InvestmentType.RENDA_FIXA].orEmpty(),
                                criptomoedas = grouped[InvestmentType.CRIPTOMOEDAS].orEmpty(),
                                performances = computePerformances(investments),
                                portfolioHistory = computePortfolioHistory(investments),
                                portfolioWindowReturns = computePortfolioWindowReturns(investments),
                            )
                        }
                    }
                    is Resource.Error -> _uiState.update {
                        it.copy(isLoading = false, errorMessage = result.message)
                    }
                }
            }
        }
        viewModelScope.launch {
            val result = investmentRepository.getBenchmark()
            if (result is Resource.Success) _uiState.update { it.copy(benchmark = result.data) }
        }
    }

    // ─── Performance por ativo ───

    private fun computePerformances(investments: List<Investment>): List<InvestmentPerformance> =
        investments.map { inv ->
            val sorted = inv.history.sortedBy { it.date }
            InvestmentPerformance(
                investment = inv,
                totalReturn = if (inv.investedValue > 0)
                    (inv.currentValue - inv.investedValue) / inv.investedValue * 100.0 else 0.0,
                return1M = historyReturn(inv, sorted, 1),
                return3M = historyReturn(inv, sorted, 3),
                return6M = historyReturn(inv, sorted, 6),
                return12M = historyReturn(inv, sorted, 12),
            )
        }

    private fun historyReturn(
        inv: Investment,
        sortedHistory: List<ProfitabilitySnapshot>,
        monthsBack: Int,
    ): Double? {
        if (sortedHistory.isEmpty()) return null
        val targetDate = LocalDate.now().minusMonths(monthsBack.toLong())
        val snapshot = sortedHistory.lastOrNull {
            runCatching { LocalDate.parse(it.date) <= targetDate }.getOrElse { false }
        } ?: return null
        return if (snapshot.value > 0)
            (inv.currentValue - snapshot.value) / snapshot.value * 100.0 else null
    }

    // ─── Histórico do portfólio agregado ───

    /**
     * Para cada data presente em qualquer ativo, soma o valor do snapshot mais recente
     * de TODOS os ativos até aquela data. Produz uma série temporal do portfólio total.
     */
    private fun computePortfolioHistory(investments: List<Investment>): List<Pair<String, Double>> {
        val allDates = investments
            .flatMap { it.history.map { s -> s.date } }
            .distinct()
            .sorted()
        if (allDates.isEmpty()) return emptyList()
        return allDates.map { date ->
            val total = investments.sumOf { inv ->
                inv.history
                    .filter { it.date <= date }
                    .maxByOrNull { it.date }
                    ?.value ?: 0.0
            }
            date to total
        }.filter { it.second > 0 }
    }

    // ─── Retornos do portfólio por janela temporal ───

    private fun computePortfolioWindowReturns(investments: List<Investment>): PortfolioWindowReturns {
        val totalCurrent = investments.sumOf { it.currentValue }
        val totalInvested = investments.sumOf { it.investedValue }
        return PortfolioWindowReturns(
            return1M = portfolioWindowReturn(investments, totalCurrent, 1),
            return3M = portfolioWindowReturn(investments, totalCurrent, 3),
            return6M = portfolioWindowReturn(investments, totalCurrent, 6),
            return12M = portfolioWindowReturn(investments, totalCurrent, 12),
            returnAll = if (totalInvested > 0)
                (totalCurrent - totalInvested) / totalInvested * 100.0 else 0.0,
        )
    }

    private fun portfolioWindowReturn(
        investments: List<Investment>,
        totalCurrent: Double,
        monthsBack: Int,
    ): Double {
        val targetDate = LocalDate.now().minusMonths(monthsBack.toLong())
        val totalHistorical = investments.sumOf { inv ->
            val sorted = inv.history.sortedBy { it.date }
            sorted.lastOrNull {
                runCatching { LocalDate.parse(it.date) <= targetDate }.getOrElse { false }
            }?.value ?: inv.currentValue // fallback: assume sem variação
        }
        return if (totalHistorical > 0)
            (totalCurrent - totalHistorical) / totalHistorical * 100.0 else 0.0
    }

    fun toggleHideValues() {
        viewModelScope.launch { preferencesManager.saveHideValues(!_uiState.value.hideValues) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
