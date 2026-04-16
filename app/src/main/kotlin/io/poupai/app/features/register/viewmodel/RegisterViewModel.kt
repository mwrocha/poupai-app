package io.poupai.app.features.register.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.poupai.app.core.network.Resource
import io.poupai.app.domain.repository.UserRepository
import io.poupai.app.domain.usecase.auth.RegisterUseCase
import io.poupai.app.features.register.state.RegisterCredentialsUiState
import io.poupai.app.features.register.state.RegisterProfileUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase,
    private val userRepository: UserRepository,
) : ViewModel() {

    // ─── Step 1: Credenciais (Frame 13) ───

    private val _credentialsState = MutableStateFlow(RegisterCredentialsUiState())
    val credentialsState: StateFlow<RegisterCredentialsUiState> = _credentialsState.asStateFlow()

    fun onEmailChanged(email: String) {
        _credentialsState.update { it.copy(email = email, errorMessage = null) }
    }

    fun onPasswordChanged(password: String) {
        _credentialsState.update { it.copy(password = password, errorMessage = null) }
    }

    fun onTogglePasswordVisibility() {
        _credentialsState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun onRegisterCredentials() {
        val state = _credentialsState.value
        if (!state.isFormValid) {
            _credentialsState.update { it.copy(errorMessage = "Preencha todos os campos") }
            return
        }

        viewModelScope.launch {
            _credentialsState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = registerUseCase(state.email, state.password)) {
                is Resource.Success -> {
                    _credentialsState.update { it.copy(isLoading = false, isSuccess = true) }
                }
                is Resource.Error -> {
                    _credentialsState.update {
                        it.copy(isLoading = false, errorMessage = result.message)
                    }
                }
                is Resource.Loading -> Unit
            }
        }
    }

    // ─── Step 2: Perfil (Frames 14, 15) ───

    private val _profileState = MutableStateFlow(RegisterProfileUiState())
    val profileState: StateFlow<RegisterProfileUiState> = _profileState.asStateFlow()

    fun onUsernameChanged(username: String) {
        _profileState.update { it.copy(username = username) }
    }

    fun onFirstNameChanged(name: String) {
        _profileState.update { it.copy(firstName = name) }
    }

    fun onLastNameChanged(lastName: String) {
        _profileState.update { it.copy(lastName = lastName) }
    }

    fun onBirthDateChanged(date: String) {
        _profileState.update { it.copy(birthDate = date) }
    }

    fun onProfileImageSelected(path: String) {
        _profileState.update { it.copy(profileImagePath = path) }
    }

    fun onFinishProfile() {
        val state = _profileState.value
        if (!state.isFormValid) {
            _profileState.update { it.copy(errorMessage = "Preencha todos os campos") }
            return
        }

        viewModelScope.launch {
            _profileState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = userRepository.updateProfile(
                username = state.username,
                firstName = state.firstName,
                lastName = state.lastName,
                birthDate = state.birthDate,
                profileImagePath = state.profileImagePath,
            )) {
                is Resource.Success -> {
                    _profileState.update { it.copy(isLoading = false, isSuccess = true) }
                }
                is Resource.Error -> {
                    _profileState.update {
                        it.copy(isLoading = false, errorMessage = result.message)
                    }
                }
                is Resource.Loading -> Unit
            }
        }
    }
}
