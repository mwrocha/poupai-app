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
            if (response.isSuccessful && response.body() != null) {
                val dto = response.body()!!
                emit(
                    Resource.Success(
                        FinanceRepository.FinanceSummary(
                            incomeHistory = dto.incomeHistory,
                            expenseHistory = dto.expenseHistory,
                            profitHistory = dto.profitHistory,
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
