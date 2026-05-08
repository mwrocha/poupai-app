package io.poupai.app.features.settings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            preferencesManager.appTheme.collect { theme ->
                _uiState.update { it.copy(theme = theme) }
            }
        }
    }

    fun onThemeChanged(theme: String) {
        viewModelScope.launch { preferencesManager.saveTheme(theme) }
    }

    fun onShowAboutDialog() = _uiState.update { it.copy(showAboutDialog = true) }
    fun onDismissAboutDialog() = _uiState.update { it.copy(showAboutDialog = false) }
}