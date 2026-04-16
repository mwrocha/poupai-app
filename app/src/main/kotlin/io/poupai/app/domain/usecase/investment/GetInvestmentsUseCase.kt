package io.poupai.app.domain.usecase.investment

import io.poupai.app.core.network.Resource
import io.poupai.app.domain.model.Investment
import io.poupai.app.domain.repository.InvestmentRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetInvestmentsUseCase @Inject constructor(
    private val investmentRepository: InvestmentRepository,
) {
    operator fun invoke(): Flow<Resource<List<Investment>>> {
        return investmentRepository.getInvestments()
    }
}
