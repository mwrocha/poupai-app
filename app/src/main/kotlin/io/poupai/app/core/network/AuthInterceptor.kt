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
 *
 * Quando o backend retorna 401 (token expirado ou inválido), limpa as
 * credenciais salvas e emite um evento de sessão expirada para que a UI
 * redirecione o usuário para o login.
 */
@Singleton
class AuthInterceptor @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val sessionManager: SessionManager,
) : Interceptor {

    private val publicRoutes = listOf("/auth/login", "/auth/register")

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url.encodedPath

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

        val response = chain.proceed(authenticatedRequest)

        // Token expirado ou inválido — limpa sessão e notifica a UI
        if (response.code == 401) {
            runBlocking { preferencesManager.clearAll() }
            sessionManager.onSessionExpired()
        }

        return response
    }
}