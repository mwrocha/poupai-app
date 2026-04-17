package io.poupai.app.domain.repository

import io.poupai.app.core.network.Resource
import io.poupai.app.domain.model.Transaction
import io.poupai.app.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun getTransactions(): Flow<Resource<List<Transaction>>>
    fun getTransactionsByMonth(year: Int, month: Int): Flow<Resource<List<Transaction>>>
    suspend fun addTransaction(
        title: String,
        amount: Double,
        type: TransactionType,
        category: String,
        date: String,
    ): Resource<Transaction>
    suspend fun deleteTransaction(id: String): Resource<Unit>
    suspend fun getTotalBalance(): Resource<Double>
    suspend fun getIncomeTotal(): Resource<Double>
    suspend fun getExpenseTotal(): Resource<Double>
}