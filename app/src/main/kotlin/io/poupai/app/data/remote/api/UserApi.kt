package io.poupai.app.data.remote.api

import io.poupai.app.data.remote.dto.ApiResponse
import io.poupai.app.data.remote.dto.UpdateEmailRequest
import io.poupai.app.data.remote.dto.UpdateProfileRequest
import io.poupai.app.data.remote.dto.UserDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT

interface UserApi {

    @GET("users/me")
    suspend fun getMe(): Response<ApiResponse<UserDto>>

    @PUT("users/profile")
    suspend fun updateProfile(
        @Body request: UpdateProfileRequest,
    ): Response<ApiResponse<UserDto>>

    @PUT("users/email")
    suspend fun updateEmail(
        @Body request: UpdateEmailRequest,
    ): Response<ApiResponse<UserDto>>
}