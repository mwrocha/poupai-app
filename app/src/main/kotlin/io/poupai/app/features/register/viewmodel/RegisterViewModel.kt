package io.poupai.app.features.register.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.poupai.app.core.network.Resource
import io.poupai.app.data.remote.api.UploadApi
import io.poupai.app.data.repository.RegisterSessionRepository
import io.poupai.app.domain.usecase.auth.RegisterUseCase
import io.poupai.app.features.register.state.RegisterCredentialsUiState
import io.poupai.app.features.register.state.RegisterProfileUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase,
    private val registerSession: RegisterSessionRepository,
    private val uploadApi: UploadApi,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    // ─── Step 1: Credenciais ──────────────────────────────────────────────

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
        if (state.email.isBlank()) {
            _credentialsState.update { it.copy(errorMessage = "E-mail é obrigatório") }
            return
        }
        if (state.password.length < 6) {
            _credentialsState.update { it.copy(errorMessage = "Senha deve ter no mínimo 6 caracteres") }
            return
        }
        registerSession.saveCredentials(state.email, state.password)
        _credentialsState.update { it.copy(isSuccess = true, errorMessage = null) }
    }

    // ─── Step 2: Perfil ───────────────────────────────────────────────────

    private val _profileState = MutableStateFlow(RegisterProfileUiState())
    val profileState: StateFlow<RegisterProfileUiState> = _profileState.asStateFlow()

    // Uri da imagem selecionada — usada para preview na tela
    private var selectedImageUri: Uri? = null

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
        _profileState.update { it.copy(birthDate = date, errorMessage = null) }
    }

    fun onImageSelected(uri: Uri) {
        selectedImageUri = uri
        _profileState.update { it.copy(profileImageUri = uri) }
    }

    fun onFinishProfile() {
        val profile = _profileState.value

        if (!registerSession.hasCredentials) {
            _profileState.update { it.copy(errorMessage = "Sessão expirada. Volte e preencha o e-mail e senha novamente.") }
            return
        }
        if (!profile.isFormValid) {
            _profileState.update { it.copy(errorMessage = "Preencha todos os campos obrigatórios") }
            return
        }

        val convertedDate = convertDateToIso(profile.birthDate)
        if (convertedDate == null && profile.birthDate.isNotBlank()) {
            _profileState.update { it.copy(errorMessage = "Data inválida. Use o formato dd/MM/yyyy") }
            return
        }

        viewModelScope.launch {
            _profileState.update { it.copy(isLoading = true, errorMessage = null) }

            // 1. Faz upload da imagem se selecionada
            var profileImageUrl: String? = null
            selectedImageUri?.let { uri ->
                profileImageUrl = uploadImage(uri)
            }

            // 2. Registra o usuário com todos os dados
            when (val result = registerUseCase(
                email = registerSession.email,
                password = registerSession.password,
                username = profile.username,
                firstName = profile.firstName,
                lastName = profile.lastName,
                birthDate = convertedDate.orEmpty(),
                profileImagePath = profileImageUrl,
            )) {
                is Resource.Success -> {
                    registerSession.clear()
                    _profileState.update { it.copy(isLoading = false, isSuccess = true) }
                }
                is Resource.Error -> {
                    _profileState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
                is Resource.Loading -> Unit
            }
        }
    }

    private suspend fun uploadImage(uri: Uri): String? {
        return try {
            val tempId = UUID.randomUUID().toString()
            val file = uriToFile(uri)

            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
            val tempIdBody = tempId.toRequestBody("text/plain".toMediaTypeOrNull())

            val response = uploadApi.uploadProfileImageRegister(body, tempIdBody)
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data?.get("url")
            } else null
        } catch (e: Exception) {
            null // Upload falhou — prossegue sem foto
        }
    }

    private fun uriToFile(uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw RuntimeException("Não foi possível abrir a imagem")
        val tempFile = File(context.cacheDir, "profile_${UUID.randomUUID()}.jpg")
        FileOutputStream(tempFile).use { output ->
            inputStream.copyTo(output)
        }
        return tempFile
    }

    private fun convertDateToIso(date: String): String? {
        if (date.isBlank()) return null
        if (date.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) return date
        val formats = listOf("dd/MM/yyyy", "dd-MM-yyyy", "d/M/yyyy", "d-M-yyyy")
        for (format in formats) {
            try {
                val inputFormat = SimpleDateFormat(format, Locale.getDefault())
                inputFormat.isLenient = false
                val parsed = inputFormat.parse(date) ?: continue
                return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(parsed)
            } catch (e: Exception) { continue }
        }
        return null
    }
}