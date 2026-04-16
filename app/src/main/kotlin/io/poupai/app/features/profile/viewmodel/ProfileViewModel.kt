package io.poupai.app.features.profile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.poupai.app.domain.repository.AuthRepository
import io.poupai.app.features.profile.state.ProfileUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser()
            if (user != null) {
                val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        username = user.username,
                        firstName = user.firstName,
                        lastName = user.lastName,
                        birthDate = user.birthDate?.let { d -> dateFormat.format(d) } ?: "",
                        profileImageUrl = user.profileImageUrl,
                    )
                }
            }
        }
    }

    fun onLogout(onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            onLoggedOut()
        }
    }
}
