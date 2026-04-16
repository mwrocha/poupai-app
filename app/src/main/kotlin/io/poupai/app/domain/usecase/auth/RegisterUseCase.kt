package io.poupai.app.domain.usecase.auth

import io.poupai.app.core.network.Resource
import io.poupai.app.domain.model.User
import io.poupai.app.domain.repository.AuthRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(email: String, password: String): Resource<User> {
        if (email.isBlank()) return Resource.Error("E-mail não pode ser vazio")
        if (password.length < 6) return Resource.Error("Senha deve ter no mínimo 6 caracteres")
        return authRepository.register(email, password)
    }
}
