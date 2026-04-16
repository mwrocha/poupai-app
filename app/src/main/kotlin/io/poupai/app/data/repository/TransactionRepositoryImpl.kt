package io.poupai.app.data.repository

import io.poupai.app.core.network.Resource
import io.poupai.app.data.local.dao.TransactionDao
import io.poupai.app.data.mapper.toDomain
import io.poupai.app.data.mapper.toEntity
import io.poupai.app.data.remote.api.TransactionApi
import io.poupai.app.domain.model.Transaction
import io.poupai.app.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TransactionRepositoryImpl @Inject constructor(
    private val transactionApi: TransactionApi,
    private val transactionDao: TransactionDao,
) : TransactionRepository {

    // Emite direto do Room (Flow reativo) — sem coletar dentro de flow{}
    override fun getTransactions(): Flow<Resource<List<Transaction>>> =
        transactionDao.getAllTransactions().map { entities ->
            Resource.Success(entities.map { it.toDomain() })
        }

    override fun getTransactionsByMonth(year: Int, month: Int): Flow<Resource<List<Transaction>>> =
        transactionDao.getAllTransactions().map { entities ->
            Resource.Success(entities.map { it.toDomain() })
        }

    override suspend fun addTransaction(transaction: Transaction): Resource<Transaction> =
        Resource.Error("Não implementado ainda")

    override suspend fun deleteTransaction(id: String): Resource<Unit> {
        return try {
            transactionDao.deleteTransaction(id)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Erro ao deletar")
        }
    }

    override suspend fun getTotalBalance(): Resource<Double> {
        return try {
            val income = transactionDao.getTotalIncome() ?: 0.0
            val expense = transactionDao.getTotalExpense() ?: 0.0
            Resource.Success(income - expense)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Erro ao calcular saldo")
        }
    }

    override suspend fun getIncomeTotal(): Resource<Double> {
        return try {
            Resource.Success(transactionDao.getTotalIncome() ?: 0.0)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Erro")
        }
    }

    override suspend fun getExpenseTotal(): Resource<Double> {
        return try {
            Resource.Success(transactionDao.getTotalExpense() ?: 0.0)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Erro")
        }
    }
}