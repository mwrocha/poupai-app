package io.poupai.app.data.remote.dto

data class FinanceSummaryDto(
    // Histórico mensal
    val incomeHistory: List<Double>,
    val expenseHistory: List<Double>,
    val profitHistory: List<Double>,
    val monthLabels: List<String>,
    // Totais
    val totalIncome: Double,
    val totalExpense: Double,
    val totalProfit: Double,
    // Comparativo com período anterior
    val incomeChangePercent: Double,
    val expenseChangePercent: Double,
    val profitChangePercent: Double,
    // Médias
    val avgDailyExpense: Double,
    val avgMonthlyExpense: Double,
    // Projeção
    val projectedMonthlyExpense: Double,
    // Maior gasto
    val biggestExpenseTitle: String?,
    val biggestExpenseAmount: Double?,
    // Categorias
    val categoryBreakdown: List<CategoryBreakdownDto>,
)

data class CategoryBreakdownDto(
    val category: String,
    val total: Double,
    val percent: Double,
)