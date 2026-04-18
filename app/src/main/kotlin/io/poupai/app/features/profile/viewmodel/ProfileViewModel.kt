package io.poupai.app.features.profile.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.poupai.app.core.util.PreferencesManager
import io.poupai.app.data.remote.api.UploadApi
import io.poupai.app.data.remote.api.UserApi
import io.poupai.app.data.remote.dto.UpdateEmailRequest
import io.poupai.app.data.remote.dto.UpdateProfileRequest
import io.poupai.app.domain.repository.AuthRepository
import io.poupai.app.features.profile.state.ProfileUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userApi: UserApi,
    private val uploadApi: UploadApi,
    private val preferencesManager: PreferencesManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val response = userApi.getMe()
                val user = response.body()?.data
                if (response.isSuccessful && user != null) {
                    val birthDate = user.birthDate?.let { formatDateForDisplay(it) } ?: ""
                    // CPF e phone guardados como só dígitos internamente
                    val cpfDigits = user.cpf.orEmpty().filter { it.isDigit() }
                    val phoneDigits = user.phone.orEmpty().filter { it.isDigit() }
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            username = user.username.orEmpty(),
                            firstName = user.firstName.orEmpty(),
                            lastName = user.lastName.orEmpty(),
                            birthDate = birthDate,
                            email = user.email.orEmpty(),
                            cpf = cpfDigits,
                            phone = phoneDigits,
                            profileImageUrl = user.profileImageUrl,
                            editUsername = user.username.orEmpty(),
                            editFirstName = user.firstName.orEmpty(),
                            editLastName = user.lastName.orEmpty(),
                            editBirthDate = birthDate,
                            editEmail = user.email.orEmpty(),
                            editCpf = cpfDigits,
                            editPhone = phoneDigits,
                            editProfileImageUrl = user.profileImageUrl,
                        )
                    }
                } else {
                    loadFromLocal()
                }
            } catch (e: Exception) {
                loadFromLocal()
            }
        }
    }

    private suspend fun loadFromLocal() {
        val user = authRepository.getCurrentUser()
        if (user != null) {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val birthDate = user.birthDate?.let { dateFormat.format(it) } ?: ""
            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    username = user.username,
                    firstName = user.firstName,
                    lastName = user.lastName,
                    birthDate = birthDate,
                    email = user.email,
                    profileImageUrl = user.profileImageUrl,
                    editUsername = user.username,
                    editFirstName = user.firstName,
                    editLastName = user.lastName,
                    editBirthDate = birthDate,
                    editEmail = user.email,
                    editProfileImageUrl = user.profileImageUrl,
                )
            }
        }
    }

    // ─── Edição de campos ───
    fun onFieldClick(field: String) {
        _uiState.update { it.copy(editingField = if (it.editingField == field) null else field) }
    }

    fun onUsernameChanged(value: String) = _uiState.update { it.copy(editUsername = value) }
    fun onFirstNameChanged(value: String) = _uiState.update { it.copy(editFirstName = value) }
    fun onLastNameChanged(value: String) = _uiState.update { it.copy(editLastName = value) }
    fun onBirthDateChanged(value: String) = _uiState.update { it.copy(editBirthDate = value) }
    fun onEmailChanged(value: String) = _uiState.update { it.copy(editEmail = value) }

    // CPF e phone: guarda apenas dígitos, a máscara cuida da exibição
    fun onCpfChanged(value: String) {
        val digits = value.filter { it.isDigit() }.take(11)
        _uiState.update { it.copy(editCpf = digits) }
    }

    fun onPhoneChanged(value: String) {
        val digits = value.filter { it.isDigit() }.take(11)
        _uiState.update { it.copy(editPhone = digits) }
    }

    // ─── Troca de foto ───
    fun onProfileImageSelected(uri: Uri, context: Context) {
        viewModelScope.launch {
            try {
                val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
                val inputStream = context.contentResolver.openInputStream(uri) ?: return@launch
                val bytes = inputStream.readBytes()
                inputStream.close()

                val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
                val filePart = MultipartBody.Part.createFormData(
                    "file",
                    "profile_${System.currentTimeMillis()}.jpg",
                    requestBody,
                )

                val response = uploadApi.uploadProfileImage(filePart)
                val uploadedUrl = response.body()?.data?.get("url")
                if (response.isSuccessful && uploadedUrl != null) {
                    _uiState.update { it.copy(editProfileImageUrl = uploadedUrl) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Erro ao enviar foto") }
            }
        }
    }

    // ─── Salvar alterações ───
    fun onSave(currentPassword: String = "", onLogout: () -> Unit = {}) {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            try {
                if (state.emailChanged) {
                    if (currentPassword.isBlank()) {
                        _uiState.update {
                            it.copy(isSaving = false, errorMessage = "Informe sua senha atual para alterar o e-mail")
                        }
                        return@launch
                    }
                    val emailResponse = userApi.updateEmail(
                        UpdateEmailRequest(newEmail = state.editEmail, currentPassword = currentPassword)
                    )
                    if (!emailResponse.isSuccessful) {
                        val msg = emailResponse.body()?.message ?: "Erro ao atualizar e-mail"
                        _uiState.update { it.copy(isSaving = false, errorMessage = msg) }
                        return@launch
                    }
                }

                val profileResponse = userApi.updateProfile(
                    UpdateProfileRequest(
                        username = state.editUsername.ifBlank { null },
                        firstName = state.editFirstName.ifBlank { null },
                        lastName = state.editLastName.ifBlank { null },
                        birthDate = convertDateToApi(state.editBirthDate),
                        profileImageUrl = state.editProfileImageUrl,
                        // Salva no banco só os dígitos
                        cpf = state.editCpf.ifBlank { null },
                        phone = state.editPhone.ifBlank { null },
                    )
                )

                if (profileResponse.isSuccessful) {
                    preferencesManager.saveFirstName(state.editFirstName.trim())
                    state.editProfileImageUrl?.let { preferencesManager.saveProfileImageUrl(it) }
                    _uiState.update { it.copy(isSaving = false, successMessage = "Perfil atualizado!") }
                    if (state.emailChanged) {
                        authRepository.logout()
                        onLogout()
                    } else {
                        loadProfile()
                    }
                } else {
                    val msg = profileResponse.body()?.message ?: "Erro ao salvar"
                    _uiState.update { it.copy(isSaving = false, errorMessage = msg) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, errorMessage = "Erro de conexão") }
            }
        }
    }

    fun onLogout(onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            onLoggedOut()
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    private fun formatDateForDisplay(date: String): String {
        return try {
            val input = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val output = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            output.format(input.parse(date)!!)
        } catch (e: Exception) { date }
    }

    private fun convertDateToApi(date: String): String? {
        if (date.isBlank()) return null
        if (date.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) return date
        return try {
            val input = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val output = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            output.format(input.parse(date)!!)
        } catch (e: Exception) { null }
    }
}