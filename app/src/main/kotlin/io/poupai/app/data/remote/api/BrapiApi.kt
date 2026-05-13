package io.poupai.app.data.remote.api

import io.poupai.app.data.remote.dto.BrapiQuoteResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface BrapiApi {

    @GET("api/quote/{tickers}")
    suspend fun getQuote(
        @Path("tickers") tickers: String,
        @Query("token") token: String,
    ): BrapiQuoteResponse
}