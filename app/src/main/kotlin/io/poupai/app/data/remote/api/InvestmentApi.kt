package io.poupai.app.data.remote.api

import io.poupai.app.data.remote.dto.CreateInvestmentRequest
import io.poupai.app.data.remote.dto.InvestmentDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface InvestmentApi {

    @GET("investments")
    suspend fun getInvestments(): Response<List<InvestmentDto>>

    @POST("investments")
    suspend fun createInvestment(
        @Body request: CreateInvestmentRequest,
    ): Response<InvestmentDto>
}