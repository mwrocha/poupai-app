package io.poupai.app.data.repository

import io.poupai.app.core.network.Resource
import io.poupai.app.data.mapper.toDomain
import io.poupai.app.data.remote.api.InvestmentApi
import io.poupai.app.domain.model.Investment
import io.poupai.app.domain.model.InvestmentType
import io.poupai.app.domain.repository.InvestmentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class InvestmentRepositoryImpl @Inject constructor(
    private val investmentApi: InvestmentApi,
) : InvestmentRepository {

    override fun getInvestments(): Flow<Resource<List<Investment>>> = flow {
        emit(Resource.Loading)
        try {
            val response = investmentApi.getInvestments()
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!.map { it.toDomain() }))
            } else {
                emit(Resource.Error("Erro ao carregar investimentos"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Erro de conexão"))
        }
    }

    override fun getInvestmentsByType(type: InvestmentType): Flow<Resource<List<Investment>>> = flow {
        emit(Resource.Loading)
        try {
            val response = investmentApi.getInvestments()
            if (response.isSuccessful && response.body() != null) {
                val filtered = response.body()!!
                    .map { it.toDomain() }
                    .filter { it.type == type }
                emit(Resource.Success(filtered))
            } else {
                emit(Resource.Error("Erro ao carregar investimentos"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Erro de conexão"))
        }
    }

    override suspend fun getTotalInvested(): Resource<Double> {
        return try {
            val response = investmentApi.getInvestments()
            if (response.isSuccessful && response.body() != null) {
                val total = response.body()!!.sumOf { it.investedValue }
                Resource.Success(total)
            } else {
                Resource.Error("Erro ao calcular total investido")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Erro")
        }
    }

    override suspend fun getTotalProfitability(): Resource<Double> {
        return try {
            val response = investmentApi.getInvestments()
            if (response.isSuccessful && response.body() != null) {
                val total = response.body()!!.sumOf { it.currentValue - it.investedValue }
                Resource.Success(total)
            } else {
                Resource.Error("Erro ao calcular rentabilidade")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Erro")
        }
    }
}
