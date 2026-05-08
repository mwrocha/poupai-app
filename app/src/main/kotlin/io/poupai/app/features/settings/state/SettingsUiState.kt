package io.poupai.app.features.settings.state

data class SettingsUiState(
    val theme: String = "system",
    val showAboutDialog: Boolean = false,
)