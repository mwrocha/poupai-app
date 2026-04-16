package io.poupai.app.domain.repository

import io.poupai.app.core.network.Resource
import kotlinx.coroutines.flow.Flow

interface FinanceRepository {

    data class FinanceSummary(
        val incomeHistory: List<Double>,
        val expenseHistory: List<Double>,
        val profitHistory: List<Double>,
    )

    fun getFinanceSummary(months: Int = 6): Flow<Resource<FinanceSummary>>
}
