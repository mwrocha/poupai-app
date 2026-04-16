package io.poupai.app.data.remote.api

import io.poupai.app.data.remote.dto.TransactionDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface TransactionApi {

    @GET("transactions")
    suspend fun getTransactions(): Response<List<TransactionDto>>

    @GET("transactions")
    suspend fun getTransactionsByMonth(
        @Query("year") year: Int,
        @Query("month") month: Int,
    ): Response<List<TransactionDto>>
}
