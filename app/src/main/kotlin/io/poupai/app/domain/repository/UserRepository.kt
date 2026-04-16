package io.poupai.app.domain.repository

import io.poupai.app.core.network.Resource
import io.poupai.app.domain.model.User

interface UserRepository {
    suspend fun getUser(id: String): Resource<User>
    suspend fun updateProfile(
        username: String,
        firstName: String,
        lastName: String,
        birthDate: String,
        profileImagePath: String?,
    ): Resource<User>
}
