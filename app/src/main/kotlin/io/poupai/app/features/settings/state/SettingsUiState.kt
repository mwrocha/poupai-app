package io.poupai.app.features.settings.state

data class SettingsUiState(
    val theme: String = "system",
    val notificationsEnabled: Boolean = false,
    val biometricEnabled: Boolean = false,
    val biometricUnavailableMessage: String? = null,
    val showAboutDialog: Boolean = false,
)