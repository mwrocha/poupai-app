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
    val categoryTargetRV: Double = 0.0,
    val categoryTargetRF: Double = 0.0,
    val categoryTargetCripto: Double = 0.0,
    val savingAllocationTargetId: String? = null,
    // ─── Edit sheet ───
    val showEditSheet: Boolean = false,
    val editingInvestment: Investment? = null,
    val editFormName: String = "",
    val editFormShares: String = "",
    val editFormAveragePrice: String = "",
    val editFormInvestedValue: String = "",
    val editFormError: String? = null,
    val isSavingEdit: Boolean = false,
)