package io.poupai.app.data.remote.api

import io.poupai.app.data.remote.dto.FinanceSummaryDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface FinanceApi {

    @GET("finances/summary")
    suspend fun getFinanceSummary(
        @Query("months") months: Int = 6,
    ): Response<FinanceSummaryDto>
}
