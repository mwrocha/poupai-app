package io.poupai.app.features.register.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import io.poupai.app.features.register.components.ProfileImagePicker
import io.poupai.app.features.register.viewmodel.RegisterViewModel

@Composable
fun RegisterProfileScreen(
    onFinish: (userName: String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel(),
) {
    val uiState by viewModel.profileState.collectAsState()

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            val displayName = when {
                uiState.firstName.isNotBlank() && uiState.lastName.isNotBlank() ->
                    "${uiState.firstName} ${uiState.lastName}"
                uiState.firstName.isNotBlank() -> uiState.firstName
                uiState.username.isNotBlank() -> uiState.username
                else -> ""
            }
            onFinish(displayName)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier.padding(8.dp),
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ProfileImagePicker(
                currentImagePath = uiState.profileImagePath,
                onImageSelected = viewModel::onProfileImageSelected,
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = uiState.username,
                onValueChange = viewModel::onUsernameChanged,
                label = { Text("Usuário") },
                placeholder = { Text("Informe o seu usuário") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = uiState.firstName,
                onValueChange = viewModel::onFirstNameChanged,
                label = { Text("Nome") },
                placeholder = { Text("Informe o seu nome") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = uiState.lastName,
                onValueChange = viewModel::onLastNameChanged,
                label = { Text("Sobrenome") },
                placeholder = { Text("Informe o seu sobrenome") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = uiState.birthDate,
                onValueChange = viewModel::onBirthDateChanged,
                label = { Text("Data de Nascimento") },
                placeholder = { Text("dd-mm-yyyy") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            uiState.errorMessage?.let { error ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = error, color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = viewModel::onFinishProfile,
                enabled = uiState.isFormValid && !uiState.isLoading,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Text("Finalizar")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}