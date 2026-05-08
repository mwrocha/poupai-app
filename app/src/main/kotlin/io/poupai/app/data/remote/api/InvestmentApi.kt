package io.poupai.app.data.remote.api

import io.poupai.app.data.remote.dto.ApiResponse
import io.poupai.app.data.remote.dto.CreateInvestmentRequest
import io.poupai.app.data.remote.dto.InvestmentDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface InvestmentApi {

    @GET("investments")
    suspend fun getInvestments(): Response<ApiResponse<List<InvestmentDto>>>

    @POST("investments")
    suspend fun createInvestment(
        @Body request: CreateInvestmentRequest,
    ): Response<ApiResponse<InvestmentDto>>

    @PUT("investments/{id}")
    suspend fun updateInvestment(
        @Path("id") id: String,
        @Body request: CreateInvestmentRequest,
    ): Response<ApiResponse<InvestmentDto>>

    @DELETE("investments/{id}")
    suspend fun deleteInvestment(
        @Path("id") id: String,
    ): Response<ApiResponse<Void>>
}