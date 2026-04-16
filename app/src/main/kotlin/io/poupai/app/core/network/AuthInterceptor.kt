package io.poupai.app.core.network

import io.poupai.app.core.util.PreferencesManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Interceptor que injeta automaticamente o Bearer token em todas as requisições.
 * Lê o token salvo no DataStore.
 */
@Singleton
class AuthInterceptor @Inject constructor(
    private val preferencesManager: PreferencesManager,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking { preferencesManager.getAuthTokenSync() }

        val request = if (!token.isNullOrBlank()) {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }

        return chain.proceed(request)
    }
}
