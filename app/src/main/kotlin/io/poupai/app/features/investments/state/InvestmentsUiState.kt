package io.poupai.app.features.investments.state

import io.poupai.app.domain.model.BenchmarkSummary
import io.poupai.app.domain.model.Investment
import io.poupai.app.domain.model.InvestmentType
import io.poupai.app.domain.model.RebalanceSummary

data class InvestmentsUiState(
    val rendaVariavel: List<Investment> = emptyList(),
    val rendaFixa: List<Investment> = emptyList(),
    val criptomoedas: List<Investment> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val hideValues: Boolean = false,
    val benchmark: BenchmarkSummary? = null,
    val rebalance: RebalanceSummary? = null,
)