package io.poupai.app.data.remote.api

import io.poupai.app.data.remote.dto.ApiResponse
import io.poupai.app.data.remote.dto.CreateGoalRequest
import io.poupai.app.data.remote.dto.GoalDto
import io.poupai.app.data.remote.dto.UpdateGoalProgressRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface GoalApi {

    @GET("goals")
    suspend fun getGoals(): Response<ApiResponse<List<GoalDto>>>

    @POST("goals")
    suspend fun createGoal(
        @Body request: CreateGoalRequest,
    ): Response<ApiResponse<GoalDto>>

    @PATCH("goals/{id}/progress")
    suspend fun updateProgress(
        @Path("id") id: String,
        @Body request: UpdateGoalProgressRequest,
    ): Response<ApiResponse<GoalDto>>

    @DELETE("goals/{id}")
    suspend fun deleteGoal(
        @Path("id") id: String,
    ): Response<ApiResponse<Void>>
}