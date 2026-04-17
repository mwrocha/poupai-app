package io.poupai.app.domain.usecase.transaction

import io.poupai.app.core.network.Resource
import io.poupai.app.domain.model.Transaction
import io.poupai.app.domain.model.TransactionType
import io.poupai.app.domain.repository.TransactionRepository
import javax.inject.Inject

class AddTransactionUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
) {
    suspend operator fun invoke(
        title: String,
        amount: Double,
        type: TransactionType,
        category: String,
        date: String,
    ): Resource<Transaction> {
        if (title.isBlank()) return Resource.Error("Título é obrigatório")
        if (amount <= 0) return Resource.Error("Valor deve ser maior que zero")
        if (category.isBlank()) return Resource.Error("Categoria é obrigatória")
        if (date.isBlank()) return Resource.Error("Data é obrigatória")

        return transactionRepository.addTransaction(
            title = title,
            amount = amount,
            type = type,
            category = category,
            date = date,
        )
    }
}