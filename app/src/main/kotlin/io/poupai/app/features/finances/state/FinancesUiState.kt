package io.poupai.app.features.finances.state

import io.poupai.app.domain.model.Tag

data class FinancesUiState(
    val incomeHistory: List<Double> = emptyList(),
    val expenseHistory: List<Double> = emptyList(),
    val profitHistory: List<Double> = emptyList(),
    val monthLabels: List<String> = emptyList(),
    val categoryDistribution: List<Tag> = emptyList(),
    val totalExpense: Double = 0.0,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val hideValues: Boolean = false,
)