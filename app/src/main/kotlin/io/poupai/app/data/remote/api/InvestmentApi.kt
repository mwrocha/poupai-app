package io.poupai.app.data.remote.api

import io.poupai.app.data.remote.dto.InvestmentDto
import retrofit2.Response
import retrofit2.http.GET

interface InvestmentApi {

    @GET("investments")
    suspend fun getInvestments(): Response<List<InvestmentDto>>
}
