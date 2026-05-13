package io.poupai.app.features.settings.state

data class SettingsUiState(
    val theme: String = "system",
    val notificationsEnabled: Boolean = false,
    val showAboutDialog: Boolean = false,
)