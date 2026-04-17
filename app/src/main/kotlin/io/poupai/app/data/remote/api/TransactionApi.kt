package io.poupai.app.data.remote.api

import io.poupai.app.data.remote.dto.ApiResponse
import io.poupai.app.data.remote.dto.CreateTransactionRequest
import io.poupai.app.data.remote.dto.TransactionDto
import retrofit2.Response
import retrofit2.http.*

interface TransactionApi {

    @GET("transactions")
    suspend fun getTransactions(): Response<ApiResponse<List<TransactionDto>>>

    @GET("transactions")
    suspend fun getTransactionsByMonth(
        @Query("year") year: Int,
        @Query("month") month: Int,
    ): Response<ApiResponse<List<TransactionDto>>>

    @POST("transactions")
    suspend fun createTransaction(
        @Body request: CreateTransactionRequest,
    ): Response<ApiResponse<TransactionDto>>

    @DELETE("transactions/{id}")
    suspend fun deleteTransaction(
        @Path("id") id: String,
    ): Response<ApiResponse<Void>>
}