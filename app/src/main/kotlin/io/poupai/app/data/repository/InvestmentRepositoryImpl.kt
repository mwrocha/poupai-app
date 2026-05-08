package io.poupai.app.data.repository

import io.poupai.app.core.network.Resource
import io.poupai.app.data.mapper.toDomain
import io.poupai.app.data.remote.api.InvestmentApi
import io.poupai.app.data.remote.dto.CreateInvestmentRequest
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
            val data = response.body()?.data
            if (response.isSuccessful && data != null) {
                emit(Resource.Success(data.map { it.toDomain() }))
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
            val data = response.body()?.data
            if (response.isSuccessful && data != null) {
                emit(Resource.Success(data.map { it.toDomain() }.filter { it.type == type }))
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
            val data = response.body()?.data
            if (response.isSuccessful && data != null) Resource.Success(data.sumOf { it.investedValue })
            else Resource.Error("Erro ao calcular total investido")
        } catch (e: Exception) { Resource.Error(e.message ?: "Erro") }
    }

    override suspend fun getTotalProfitability(): Resource<Double> {
        return try {
            val response = investmentApi.getInvestments()
            val data = response.body()?.data
            if (response.isSuccessful && data != null) Resource.Success(data.sumOf { it.currentValue - it.investedValue })
            else Resource.Error("Erro ao calcular rentabilidade")
        } catch (e: Exception) { Resource.Error(e.message ?: "Erro") }
    }

    override suspend fun createInvestment(
        name: String,
        type: InvestmentType,
        currentValue: Double,
        investedValue: Double,
    ): Resource<Investment> {
        return try {
            val request = CreateInvestmentRequest(name = name, type = type.name, currentValue = currentValue, investedValue = investedValue)
            val response = investmentApi.createInvestment(request)
            val data = response.body()?.data
            if (response.isSuccessful && data != null) Resource.Success(data.toDomain())
            else Resource.Error("Erro ao salvar investimento")
        } catch (e: Exception) { Resource.Error(e.message ?: "Erro de conexão") }
    }

    override suspend fun updateInvestment(
        id: String,
        name: String,
        type: InvestmentType,
        currentValue: Double,
        investedValue: Double,
    ): Resource<Investment> {
        return try {
            val request = CreateInvestmentRequest(name = name, type = type.name, currentValue = currentValue, investedValue = investedValue)
            val response = investmentApi.updateInvestment(id, request)
            val data = response.body()?.data
            if (response.isSuccessful && data != null) Resource.Success(data.toDomain())
            else Resource.Error("Erro ao atualizar investimento")
        } catch (e: Exception) { Resource.Error(e.message ?: "Erro de conexão") }
    }

    override suspend fun deleteInvestment(id: String): Resource<Unit> {
        return try {
            val response = investmentApi.deleteInvestment(id)
            if (response.isSuccessful) Resource.Success(Unit)
            else Resource.Error("Erro ao excluir investimento")
        } catch (e: Exception) { Resource.Error(e.message ?: "Erro de conexão") }
    }
}