package io.poupai.app.data.remote.api

import io.poupai.app.data.remote.dto.ApiResponse
import io.poupai.app.data.remote.dto.UserDto
import retrofit2.Response
import retrofit2.http.GET

interface UserApi {

    @GET("users/me")
    suspend fun getMe(): Response<ApiResponse<UserDto>>
}