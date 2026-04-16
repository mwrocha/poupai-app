package io.poupai.app.core.network

/**
 * Wrapper genérico para resultados de operações assíncronas.
 * Usado em repositórios e ViewModels para tratar sucesso, erro e loading.
 */
sealed class Resource<out T> {
    data class Success<T>(val data: T) : Resource<T>()
    data class Error(val message: String, val throwable: Throwable? = null) : Resource<Nothing>()
    data object Loading : Resource<Nothing>()
}
