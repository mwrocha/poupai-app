package io.poupai.app.data.remote.api

import io.poupai.app.data.remote.dto.ApiResponse
import io.poupai.app.data.remote.dto.BenchmarkDto
import io.poupai.app.data.remote.dto.CreateDividendRequest
import io.poupai.app.data.remote.dto.CreateEntryRequest
import io.poupai.app.data.remote.dto.CreateInvestmentRequest
import io.poupai.app.data.remote.dto.DividendDto
import io.poupai.app.data.remote.dto.DividendSummaryDto
import io.poupai.app.data.remote.dto.EntryDto
import io.poupai.app.data.remote.dto.EntrySummaryDto
import io.poupai.app.data.remote.dto.InvestmentDto
import io.poupai.app.data.remote.dto.RebalanceDto
import io.poupai.app.data.remote.dto.UpdateInvestmentRequest
import retrofit2.Response
import retrofit2.http.*

interface InvestmentApi {

    // ─── Ativos ───
    @GET("investments")
    suspend fun getInvestments(): Response<ApiResponse<List<InvestmentDto>>>

    @POST("investments")
    suspend fun createInvestment(@Body request: CreateInvestmentRequest): Response<ApiResponse<InvestmentDto>>

    @PUT("investments/{id}")
    suspend fun updateInvestment(@Path("id") id: String, @Body request: UpdateInvestmentRequest): Response<ApiResponse<InvestmentDto>>

    @DELETE("investments/{id}")
    suspend fun deleteInvestment(@Path("id") id: String): Response<ApiResponse<Void>>

    // ─── Livro contábil ───
    @GET("investments/entries")
    suspend fun getEntries(
        @Query("investmentId") investmentId: String? = null,
        @Query("year") year: Int? = null,
        @Query("month") month: Int? = null,
    ): Response<ApiResponse<EntrySummaryDto>>

    @POST("investments/entries")
    suspend fun addEntry(@Body request: CreateEntryRequest): Response<ApiResponse<EntryDto>>

    @DELETE("investments/entries/{entryId}")
    suspend fun deleteEntry(@Path("entryId") entryId: String): Response<ApiResponse<Void>>

    // ─── Dividendos ───
    @GET("investments/dividends")
    suspend fun getDividends(@Query("year") year: Int? = null, @Query("month") month: Int? = null): Response<ApiResponse<DividendSummaryDto>>

    @POST("investments/dividends")
    suspend fun addDividend(@Body request: CreateDividendRequest): Response<ApiResponse<DividendDto>>

    @DELETE("investments/dividends/{id}")
    suspend fun deleteDividend(@Path("id") id: String): Response<ApiResponse<Void>>

    // ─── Rebalanceamento ───
    @GET("investments/rebalance")
    suspend fun getRebalance(): Response<ApiResponse<RebalanceDto>>

    // ─── Benchmark ───
    @GET("investments/benchmark")
    suspend fun getBenchmark(): Response<ApiResponse<BenchmarkDto>>
}