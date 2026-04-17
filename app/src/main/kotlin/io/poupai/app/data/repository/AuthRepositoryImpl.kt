package io.poupai.app.data.repository

import io.poupai.app.core.network.Resource
import io.poupai.app.core.util.PreferencesManager
import io.poupai.app.data.local.dao.UserDao
import io.poupai.app.data.mapper.toDomain
import io.poupai.app.data.remote.api.AuthApi
import io.poupai.app.data.remote.dto.LoginRequest
import io.poupai.app.data.remote.dto.RegisterRequest
import io.poupai.app.domain.model.User
import io.poupai.app.domain.repository.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val userDao: UserDao,
    private val preferencesManager: PreferencesManager,
) : AuthRepository {

    override suspend fun login(email: String, password: String): Resource<User> {
        return try {
            val response = authApi.login(LoginRequest(email, password))
            val apiResponse = response.body()

            if (response.isSuccessful && apiResponse?.success == true && apiResponse.data != null) {
                val userDto = apiResponse.data
                userDto.token?.let { preferencesManager.saveAuthToken(it) }
                preferencesManager.saveUserId(userDto.id.orEmpty())
                preferencesManager.saveFirstName(userDto.firstName.orEmpty().trim())
                userDto.profileImageUrl?.let { preferencesManager.saveProfileImageUrl(it) }
                Resource.Success(userDto.toDomain())
            } else {
                Resource.Error(apiResponse?.message ?: "Credenciais inválidas")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Erro ao fazer login", e)
        }
    }

    override suspend fun register(
        email: String,
        password: String,
        username: String,
        firstName: String,
        lastName: String,
        birthDate: String,
        profileImagePath: String?,
    ): Resource<User> {
        return try {
            val response = authApi.register(
                RegisterRequest(
                    email = email,
                    password = password,
                    username = username.ifBlank { null },
                    firstName = firstName.ifBlank { null },
                    lastName = lastName.ifBlank { null },
                    birthDate = birthDate.ifBlank { null },
                    profileImageUrl = profileImagePath,
                )
            )
            val apiResponse = response.body()

            if (response.isSuccessful && apiResponse?.success == true && apiResponse.data != null) {
                val userDto = apiResponse.data
                userDto.token?.let { preferencesManager.saveAuthToken(it) }
                preferencesManager.saveUserId(userDto.id.orEmpty())
                preferencesManager.saveFirstName(userDto.firstName.orEmpty().trim())
                userDto.profileImageUrl?.let { preferencesManager.saveProfileImageUrl(it) }
                Resource.Success(userDto.toDomain())
            } else {
                Resource.Error(apiResponse?.message ?: "Erro ao criar conta")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Erro ao registrar", e)
        }
    }

    override suspend fun logout() {
        preferencesManager.clearAll()
        userDao.deleteAll()
    }

    override suspend fun isLoggedIn(): Boolean =
        preferencesManager.getAuthTokenSync() != null

    override suspend fun getCurrentUser(): User? {
        val userId = preferencesManager.getUserIdSync() ?: return null
        return userDao.getUserById(userId)?.toDomain()
    }
}