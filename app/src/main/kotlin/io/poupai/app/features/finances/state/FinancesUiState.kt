package io.poupai.app.features.finances.state

data class FinancesUiState(
    val incomeHistory: List<Double> = emptyList(),
    val expenseHistory: List<Double> = emptyList(),
    val profitHistory: List<Double> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)
