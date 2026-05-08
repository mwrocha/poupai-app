package io.poupai.app.data.remote.api

import io.poupai.app.data.remote.dto.ApiResponse
import io.poupai.app.data.remote.dto.TagsSummaryResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface TagApi {

    @GET("tags/summary")
    suspend fun getSummary(
        @Query("month") month: Int? = null,
        @Query("year") year: Int? = null,
    ): Response<ApiResponse<TagsSummaryResponse>>
}