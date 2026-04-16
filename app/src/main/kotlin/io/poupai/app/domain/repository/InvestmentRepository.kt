package io.poupai.app.domain.repository

import io.poupai.app.core.network.Resource
import io.poupai.app.domain.model.Investment
import io.poupai.app.domain.model.InvestmentType
import kotlinx.coroutines.flow.Flow

interface InvestmentRepository {
    fun getInvestments(): Flow<Resource<List<Investment>>>
    fun getInvestmentsByType(type: InvestmentType): Flow<Resource<List<Investment>>>
    suspend fun getTotalInvested(): Resource<Double>
    suspend fun getTotalProfitability(): Resource<Double>
}
