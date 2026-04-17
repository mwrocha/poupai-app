package io.poupai.app.data.repository

import io.poupai.app.core.network.Resource
import io.poupai.app.data.local.dao.TransactionDao
import io.poupai.app.data.mapper.toDomain
import io.poupai.app.data.mapper.toEntity
import io.poupai.app.data.remote.api.TransactionApi
import io.poupai.app.data.remote.dto.CreateTransactionRequest
import io.poupai.app.domain.model.Transaction
import io.poupai.app.domain.model.TransactionType
import io.poupai.app.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TransactionRepositoryImpl @Inject constructor(
    private val transactionApi: TransactionApi,
    private val transactionDao: TransactionDao,
) : TransactionRepository {

    /**
     * Estratégia offline-first:
     * 1. Emite imediatamente os dados do Room (cache local)
     * 2. Busca da API em paralelo
     * 3. Salva no Room e emite os dados atualizados
     */
    override fun getTransactions(): Flow<Resource<List<Transaction>>> = flow {
        // 1. Emite cache local primeiro (resposta imediata)
        val cached = transactionDao.getAllTransactionsOnce()
        if (cached.isNotEmpty()) {
            emit(Resource.Success(cached.map { it.toDomain() }))
        } else {
            emit(Resource.Loading())
        }

        // 2. Sincroniza com a API
        try {
            val response = transactionApi.getTransactions()
            val apiResponse = response.body()
            if (response.isSuccessful && apiResponse?.success == true && apiResponse.data != null) {
                val remote = apiResponse.data
                // Substitui o cache local pelo dado mais recente da API
                transactionDao.clearAll()
                transactionDao.insertTransactions(remote.map { it.toEntity() })
                emit(Resource.Success(remote.map { it.toDomain() }))
            } else {
                // API falhou mas já emitimos o cache — não precisa emitir erro se tinha cache
                if (cached.isEmpty()) {
                    emit(Resource.Error("Erro ao carregar transações"))
                }
            }
        } catch (e: Exception) {
            if (cached.isEmpty()) {
                emit(Resource.Error(e.message ?: "Erro de conexão"))
            }
            // Se tinha cache, silencia o erro — usuário já viu os dados locais
        }
    }

    override fun getTransactionsByMonth(year: Int, month: Int): Flow<Resource<List<Transaction>>> =
        transactionDao.getAllTransactions().map { entities ->
            Resource.Success(entities.map { it.toDomain() })
        }

    override suspend fun addTransaction(
        title: String,
        amount: Double,
        type: TransactionType,
        category: String,
        date: String,
    ): Resource<Transaction> {
        return try {
            val response = transactionApi.createTransaction(
                CreateTransactionRequest(
                    title = title,
                    amount = amount,
                    type = type.name,
                    category = category,
                    date = date,
                )
            )
            val apiResponse = response.body()
            if (response.isSuccessful && apiResponse?.success == true && apiResponse.data != null) {
                val transaction = apiResponse.data.toDomain()
                transactionDao.insertTransaction(transaction.toEntity())
                Resource.Success(transaction)
            } else {
                Resource.Error(apiResponse?.message ?: "Erro ao criar transação")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Erro de conexão")
        }
    }

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