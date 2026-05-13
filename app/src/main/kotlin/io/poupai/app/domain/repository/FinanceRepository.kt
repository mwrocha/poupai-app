package io.poupai.app.domain.repository

import io.poupai.app.core.network.Resource
import kotlinx.coroutines.flow.Flow

interface FinanceRepository {

    data class CategoryBreakdown(
        val category: String,
        val total: Double,
        val percent: Double,
    )

    data class FinanceSummary(
        val incomeHistory: List<Double>,
        val expenseHistory: List<Double>,
        val profitHistory: List<Double>,
        val monthLabels: List<String>,
        val totalIncome: Double,
        val totalExpense: Double,
        val totalProfit: Double,
        val incomeChangePercent: Double,
        val expenseChangePercent: Double,
        val profitChangePercent: Double,
        val avgDailyExpense: Double,
        val avgMonthlyExpense: Double,
        val projectedMonthlyExpense: Double,
        val biggestExpenseTitle: String?,
        val biggestExpenseAmount: Double?,
        val categoryBreakdown: List<CategoryBreakdown>,
    )

    fun getFinanceSummary(months: Int = 6): Flow<Resource<FinanceSummary>>
    fun getFinanceSummaryByPeriod(month: Int, year: Int): Flow<Resource<FinanceSummary>>
}