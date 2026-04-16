package io.poupai.app.data.repository

import io.poupai.app.core.network.Resource
import io.poupai.app.domain.model.User
import io.poupai.app.domain.repository.UserRepository
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor() : UserRepository {

    override suspend fun getUser(id: String): Resource<User> {
        return Resource.Error("Não implementado ainda")
    }

    override suspend fun updateProfile(
        username: String,
        firstName: String,
        lastName: String,
        birthDate: String,
        profileImagePath: String?,
    ): Resource<User> {
        return Resource.Error("Não implementado ainda")
    }
}
