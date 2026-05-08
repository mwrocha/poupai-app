package io.poupai.app.data.remote.api

import io.poupai.app.data.remote.dto.ApiResponse
import io.poupai.app.data.remote.dto.GamificationDto
import retrofit2.Response
import retrofit2.http.GET

interface GamificationApi {

    @GET("gamification/status")
    suspend fun getStatus(): Response<ApiResponse<GamificationDto>>
}