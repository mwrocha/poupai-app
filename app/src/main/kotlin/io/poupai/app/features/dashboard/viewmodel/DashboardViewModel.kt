package io.poupai.app.features.dashboard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.poupai.app.core.network.Resource
import io.poupai.app.core.util.PreferencesManager
import io.poupai.app.domain.repository.TransactionRepository
import io.poupai.app.features.dashboard.state.DashboardUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val transactionRepository: TransactionRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboard()
    }

    private fun loadDashboard() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Lê direto do DataStore — já salvo no login/cadastro
            val firstName = preferencesManager.getFirstNameSync()
                ?.ifBlank { null } ?: "Usuário"
            val profileImageUrl = preferencesManager.getProfileImageUrlSync()

            _uiState.update {
                it.copy(
                    userName = firstName,
                    profileImageUrl = profileImageUrl,
                )
            }

            // Saldo total
            when (val balance = transactionRepository.getTotalBalance()) {
                is Resource.Success -> _uiState.update { it.copy(totalSaved = balance.data) }
                is Resource.Error -> _uiState.update { it.copy(errorMessage = balance.message) }
                is Resource.Loading -> Unit
            }

            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun refresh() = loadDashboard()
}