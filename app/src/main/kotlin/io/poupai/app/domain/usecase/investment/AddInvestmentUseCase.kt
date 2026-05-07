package io.poupai.app.domain.usecase.investment

import io.poupai.app.core.network.Resource
import io.poupai.app.domain.model.Investment
import io.poupai.app.domain.model.InvestmentType
import io.poupai.app.domain.repository.InvestmentRepository  // ← adicionar esta linha
import javax.inject.Inject

class AddInvestmentUseCase @Inject constructor(
    private val investmentRepository: InvestmentRepository,
) {
    suspend operator fun invoke(
        name: String,
        type: InvestmentType,
        currentValue: Double,
        investedValue: Double,
    ): Resource<Investment> {
        return investmentRepository.createInvestment(
            name = name,
            type = type,
            currentValue = currentValue,
            investedValue = investedValue,
        )
    }
}