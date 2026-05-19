package io.poupai.app.features.allocation.state

import io.poupai.app.domain.model.BenchmarkSummary
import io.poupai.app.domain.model.Investment

data class InvestmentPerformance(
    val investment: Investment,
    val totalReturn: Double,
    val return1M: Double?,
    val return3M: Double?,
    val return6M: Double?,
    val return12M: Double?,
)

/**
 * Retornos do portfólio agregado para cada janela temporal,
 * calculados somando o valor histórico de todos os ativos.
 */
data class PortfolioWindowReturns(
    val return1M: Double,
    val return3M: Double,
    val return6M: Double,
    val return12M: Double,
    val returnAll: Double,
)

data class AllocationUiState(
    val rendaVariavel: List<Investment> = emptyList(),
    val rendaFixa: List<Investment> = emptyList(),
    val criptomoedas: List<Investment> = emptyList(),
    val performances: List<InvestmentPerformance> = emptyList(),
    /** Valor total do portfólio agregado por data: List<(date, totalValue)> */
    val portfolioHistory: List<Pair<String, Double>> = emptyList(),
    val portfolioWindowReturns: PortfolioWindowReturns? = null,
    val benchmark: BenchmarkSummary? = null,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val hideValues: Boolean = false,
    val errorMessage: String? = null,
)
