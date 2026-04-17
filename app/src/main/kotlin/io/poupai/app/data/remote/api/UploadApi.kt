package io.poupai.app.data.remote.api

import io.poupai.app.data.remote.dto.ApiResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface UploadApi {

    // Upload durante cadastro — sem autenticação
    @Multipart
    @POST("upload/profile-image/register")
    suspend fun uploadProfileImageRegister(
        @Part file: MultipartBody.Part,
        @Part("tempId") tempId: RequestBody,
    ): Response<ApiResponse<Map<String, String>>>

    // Upload após login — com autenticação (via AuthInterceptor)
    @Multipart
    @POST("upload/profile-image")
    suspend fun uploadProfileImage(
        @Part file: MultipartBody.Part,
    ): Response<ApiResponse<Map<String, String>>>
}