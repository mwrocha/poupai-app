package io.poupai.app.core.network

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gerencia eventos de sessão do usuário.
 * Emite um evento quando o token expira (401) para que a UI
 * possa redirecionar para o login sem precisar de referências de contexto.
 */
@Singleton
class SessionManager @Inject constructor() {

    private val _sessionExpiredEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val sessionExpiredEvent: SharedFlow<Unit> = _sessionExpiredEvent.asSharedFlow()

    fun onSessionExpired() {
        _sessionExpiredEvent.tryEmit(Unit)
    }
}