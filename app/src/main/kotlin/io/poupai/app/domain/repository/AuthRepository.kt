package io.poupai.app.domain.repository

import io.poupai.app.core.network.Resource
import io.poupai.app.domain.model.User

interface AuthRepository {
    suspend fun login(email: String, password: String): Resource<User>
    suspend fun register(email: String, password: String): Resource<User>
    suspend fun logout()
    suspend fun isLoggedIn(): Boolean
    suspend fun getCurrentUser(): User?
}
