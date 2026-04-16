package io.poupai.app.domain.usecase.transaction

import io.poupai.app.core.network.Resource
import io.poupai.app.domain.model.Transaction
import io.poupai.app.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTransactionsUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
) {
    operator fun invoke(): Flow<Resource<List<Transaction>>> =
        transactionRepository.getTransactions()
}