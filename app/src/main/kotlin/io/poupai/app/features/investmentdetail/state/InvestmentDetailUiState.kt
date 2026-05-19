package io.poupai.app.features.investmentdetail.state

import io.poupai.app.domain.model.Dividend
import io.poupai.app.domain.model.Investment
import io.poupai.app.domain.model.InvestmentEntry

data class InvestmentDetailUiState(
    val investment: Investment? = null,
    val entries: List<InvestmentEntry> = emptyList(),
    val dividends: List<Dividend> = emptyList(),
    val totalAported: Double = 0.0,
    val totalRescued: Double = 0.0,
    val totalDividends: Double = 0.0,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val hideValues: Boolean = false,
    val errorMessage: String? = null,
)
