package io.poupai.app.core.network

import io.poupai.app.core.util.PreferencesManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Interceptor que injeta automaticamente o Bearer token em todas as requisições,
 * exceto nas rotas públicas de autenticação (/auth/login, /auth/register).
 */
@Singleton
class AuthInterceptor @Inject constructor(
    private val preferencesManager: PreferencesManager,
) : Interceptor {

    // Rotas que não precisam de token
    private val publicRoutes = listOf("/auth/login", "/auth/register")

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url.encodedPath

        // Rotas públicas — nunca adiciona Authorization
        if (publicRoutes.any { path.endsWith(it) }) {
            return chain.proceed(request)
        }

        val token = runBlocking { preferencesManager.getAuthTokenSync() }

        val authenticatedRequest = if (!token.isNullOrBlank()) {
            request.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            request
        }

        return chain.proceed(authenticatedRequest)
    }
}