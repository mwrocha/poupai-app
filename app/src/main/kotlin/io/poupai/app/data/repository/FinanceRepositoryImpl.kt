package io.poupai.app.data.repository

import io.poupai.app.core.network.Resource
import io.poupai.app.data.remote.api.FinanceApi
import io.poupai.app.data.remote.dto.FinanceSummaryDto
import io.poupai.app.domain.repository.FinanceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class FinanceRepositoryImpl @Inject constructor(
    private val financeApi: FinanceApi,
) : FinanceRepository {

    override fun getFinanceSummary(months: Int): Flow<Resource<FinanceRepository.FinanceSummary>> = flow {
        emit(Resource.Loading)
        try {
            val response = financeApi.getFinanceSummary(months)
            val data = response.body()?.data
            if (response.isSuccessful && data != null) {
                emit(Resource.Success(data.toDomain()))
            } else {
                emit(Resource.Error("Erro ao carregar finanças"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Erro de conexão"))
        }
    }

    override fun getFinanceSummaryByPeriod(month: Int, year: Int): Flow<Resource<FinanceRepository.FinanceSummary>> = flow {
        emit(Resource.Loading)
        try {
            val response = financeApi.getFinanceSummaryByPeriod(month, year)
            val data = response.body()?.data
            if (response.isSuccessful && data != null) {
                emit(Resource.Success(data.toDomain()))
            } else {
                emit(Resource.Error("Erro ao carregar período"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Erro de conexão"))
        }
    }

    private fun FinanceSummaryDto.toDomain() = FinanceRepository.FinanceSummary(
        incomeHistory = incomeHistory,
        expenseHistory = expenseHistory,
        profitHistory = profitHistory,
        monthLabels = monthLabels,
        totalIncome = totalIncome,
        totalExpense = totalExpense,
        totalProfit = totalProfit,
        incomeChangePercent = incomeChangePercent,
        expenseChangePercent = expenseChangePercent,
        profitChangePercent = profitChangePercent,
        avgDailyExpense = avgDailyExpense,
        avgMonthlyExpense = avgMonthlyExpense,
        projectedMonthlyExpense = projectedMonthlyExpense,
        biggestExpenseTitle = biggestExpenseTitle,
        biggestExpenseAmount = biggestExpenseAmount,
        categoryBreakdown = categoryBreakdown.map {
            FinanceRepository.CategoryBreakdown(
                category = it.category,
                total = it.total,
                percent = it.percent,
            )
        },
    )
}