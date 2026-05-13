package io.poupai.app.features.finances.state

import io.poupai.app.domain.repository.FinanceRepository
import java.time.LocalDate

data class FinancesUiState(
    // Histórico
    val incomeHistory: List<Double> = emptyList(),
    val expenseHistory: List<Double> = emptyList(),
    val profitHistory: List<Double> = emptyList(),
    val monthLabels: List<String> = emptyList(),
    // Totais
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val totalProfit: Double = 0.0,
    // Comparativo
    val incomeChangePercent: Double = 0.0,
    val expenseChangePercent: Double = 0.0,
    val profitChangePercent: Double = 0.0,
    // Médias
    val avgDailyExpense: Double = 0.0,
    val avgMonthlyExpense: Double = 0.0,
    // Projeção
    val projectedMonthlyExpense: Double = 0.0,
    // Maior gasto
    val biggestExpenseTitle: String? = null,
    val biggestExpenseAmount: Double? = null,
    // Categorias
    val categoryBreakdown: List<FinanceRepository.CategoryBreakdown> = emptyList(),
    // Filtro de período
    val selectedPeriod: PeriodFilter = PeriodFilter.MONTHS_6,
    val selectedMonth: Int = LocalDate.now().monthValue,
    val selectedYear: Int = LocalDate.now().year,
    // UI
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val hideValues: Boolean = false,
)

enum class PeriodFilter(val label: String) {
    MONTHS_3("3 meses"),
    MONTHS_6("6 meses"),
    MONTHS_12("12 meses"),
    CUSTOM_MONTH("Mês específico"),
}