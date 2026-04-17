package io.poupai.app.data.remote.api

import io.poupai.app.data.remote.dto.ApiResponse
import io.poupai.app.data.remote.dto.LoginRequest
import io.poupai.app.data.remote.dto.RegisterRequest
import io.poupai.app.data.remote.dto.UserDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest,
    ): Response<ApiResponse<UserDto>>

    @POST("auth/register")
    suspend fun register(
        @Body request: RegisterRequest,
    ): Response<ApiResponse<UserDto>>
}