package io.poupai.app.data.repository

import io.poupai.app.core.network.Resource
import io.poupai.app.data.remote.api.FinanceApi
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
                emit(
                    Resource.Success(
                        FinanceRepository.FinanceSummary(
                            incomeHistory = data.incomeHistory,
                            expenseHistory = data.expenseHistory,
                            profitHistory = data.profitHistory,
                        )
                    )
                )
            } else {
                emit(Resource.Error("Erro ao carregar finanças"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Erro de conexão"))
        }
    }
}