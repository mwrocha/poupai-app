package io.poupai.app.features.splash.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.poupai.app.core.util.PreferencesManager
import io.poupai.app.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val preferencesManager: PreferencesManager,
) : ViewModel() {

    enum class Destination { ONBOARDING, DASHBOARD }

    private val _destination = MutableStateFlow<Destination?>(null)
    val destination: StateFlow<Destination?> = _destination.asStateFlow()

    private val _userName = MutableStateFlow("")
    val userName: StateFlow<String> = _userName.asStateFlow()

    init {
        viewModelScope.launch {
            delay(1500) // Tempo mínimo do splash

            val isLoggedIn = authRepository.isLoggedIn()
            if (isLoggedIn) {
                val firstName = preferencesManager.getFirstNameSync().orEmpty()
                _userName.value = firstName
                _destination.value = Destination.DASHBOARD
            } else {
                _destination.value = Destination.ONBOARDING
            }
        }
    }
}