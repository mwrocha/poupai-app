package io.poupai.app.features.settings.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.qualifiers.ApplicationContext
import io.poupai.app.core.notification.NotificationScheduler
import io.poupai.app.core.security.AppLock
import io.poupai.app.core.util.PreferencesManager
import io.poupai.app.features.settings.state.SettingsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val notificationScheduler: NotificationScheduler,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            preferencesManager.appTheme.collect { theme ->
                _uiState.update { it.copy(theme = theme) }
            }
        }
        viewModelScope.launch {
            preferencesManager.isBiometricEnabled.collect { enabled ->
                _uiState.update { it.copy(biometricEnabled = enabled) }
            }
        }
        // Verifica se o lembrete já está agendado
        _uiState.update { it.copy(notificationsEnabled = notificationScheduler.isReminderScheduled()) }
    }

    fun onThemeChanged(theme: String) {
        viewModelScope.launch { preferencesManager.saveTheme(theme) }
    }

    fun onNotificationsChanged(enabled: Boolean) {
        _uiState.update { it.copy(notificationsEnabled = enabled) }
        if (enabled) notificationScheduler.scheduleDailyReminder()
        else notificationScheduler.cancelReminder()
    }

    fun onBiometricChanged(enabled: Boolean) {
        // Não deixa ligar o bloqueio se o aparelho não tem como autenticar — evitaria
        // trancar o usuário pra fora.
        if (enabled && !AppLock.canAuthenticate(context)) {
            _uiState.update {
                it.copy(
                    biometricUnavailableMessage =
                        "Configure uma biometria ou um PIN/padrão de tela no seu aparelho para usar o bloqueio.",
                )
            }
            return
        }
        viewModelScope.launch { preferencesManager.setBiometricEnabled(enabled) }
    }

    fun dismissBiometricMessage() = _uiState.update { it.copy(biometricUnavailableMessage = null) }

    fun onShowAboutDialog() = _uiState.update { it.copy(showAboutDialog = true) }
    fun onDismissAboutDialog() = _uiState.update { it.copy(showAboutDialog = false) }
}