package io.poupai.app.features.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.poupai.app.core.network.Resource
import io.poupai.app.domain.usecase.auth.LoginUseCase
import io.poupai.app.features.auth.state.LoginUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChanged(email: String) {
        _uiState.update { it.copy(email = email, errorMessage = null) }
    }

    fun onPasswordChanged(password: String) {
        _uiState.update { it.copy(password = password, errorMessage = null) }
    }

    fun onTogglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun onLoginClicked() {
        val state = _uiState.value
        if (!state.isFormValid) {
            _uiState.update { it.copy(errorMessage = "Preencha todos os campos corretamente") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            when (val result = loginUseCase(state.email, state.password)) {
                is Resource.Success -> {
                    val user = result.data

                    // Monta o nome de exibição
                    // Fallback final: parte do email antes do @ para garantir que nunca fique vazio
                    val displayName = when {
                        user.firstName.isNotBlank() && user.lastName.isNotBlank() ->
                            "${user.firstName} ${user.lastName}"
                        user.firstName.isNotBlank() -> user.firstName
                        user.username.isNotBlank() -> user.username
                        else -> user.email.substringBefore("@").ifBlank { "usuário" }
                    }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isLoginSuccess = true,
                            loggedUserName = displayName,
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
                is Resource.Loading -> Unit
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}