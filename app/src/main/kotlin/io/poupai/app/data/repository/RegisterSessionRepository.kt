package io.poupai.app.data.repository

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Guarda as credenciais do cadastro em memória durante o fluxo de registro.
 * Escopado como Singleton — sobrevive a qualquer navegação entre telas.
 * Deve ser limpo após o cadastro ser concluído ou cancelado.
 */
@Singleton
class RegisterSessionRepository @Inject constructor() {

    var email: String = ""
        private set

    var password: String = ""
        private set

    fun saveCredentials(email: String, password: String) {
        this.email = email
        this.password = password
    }

    fun clear() {
        email = ""
        password = ""
    }

    val hasCredentials: Boolean
        get() = email.isNotBlank() && password.isNotBlank()
}