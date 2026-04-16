package io.poupai.app.features.transactions.state

import io.poupai.app.domain.model.Transaction

data class TransactionsUiState(
    val balance: Double = 0.0,
    val incomeTotal: Double = 0.0,
    val expenseTotal: Double = 0.0,
    val comparisonPercentage: Double = 0.0,
    val weeklyChartData: List<Double> = emptyList(),
    val recentTransactions: List<Transaction> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)
