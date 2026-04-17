package io.poupai.app.features.register.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    val fieldColors = TextFieldDefaults.colors(
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
        disabledContainerColor = Color.Transparent,
        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
        unfocusedIndicatorColor = Color(0xFFBDBDBD),
        focusedLabelColor = MaterialTheme.colorScheme.primary,
        unfocusedLabelColor = Color(0xFF9E9E9E),
        cursorColor = MaterialTheme.colorScheme.primary,
        focusedTextColor = Color(0xFF754AA8),      // ← texto dos forms
        unfocusedTextColor = Color(0xFF311E46),
    )

    Column(modifier = Modifier.fillMaxSize()) {
        IconButton(onClick = onNavigateBack, modifier = Modifier.padding(8.dp)) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // ─── Picker de foto de perfil ───
            ProfileImagePicker(
                currentImageUri = uiState.profileImageUri,
                onImageSelected = viewModel::onImageSelected,
            )

            Spacer(Modifier.height(32.dp))

            TextField(
                value = uiState.username,
                onValueChange = viewModel::onUsernameChanged,
                label = { Text("Usuário") },
                placeholder = { Text("Informe o seu usuário") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors,
            )

            Spacer(Modifier.height(8.dp))

            TextField(
                value = uiState.firstName,
                onValueChange = viewModel::onFirstNameChanged,
                label = { Text("Nome") },
                placeholder = { Text("Informe o seu nome") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors,
            )

            Spacer(Modifier.height(8.dp))

            TextField(
                value = uiState.lastName,
                onValueChange = viewModel::onLastNameChanged,
                label = { Text("Sobrenome") },
                placeholder = { Text("Informe o seu sobrenome") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors,
            )

            Spacer(Modifier.height(8.dp))

            TextField(
                value = uiState.birthDate,
                onValueChange = viewModel::onBirthDateChanged,
                label = { Text("Data de Nascimento") },
                placeholder = { Text("dd/MM/yyyy") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors,
            )

            uiState.errorMessage?.let { error ->
                Spacer(Modifier.height(8.dp))
                Text(text = error, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
            }

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = viewModel::onFinishProfile,
                enabled = uiState.isFormValid && !uiState.isLoading,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF503173),
                    disabledContainerColor = Color(0xFF503173).copy(alpha = 0.5f),
                ),
            ) {
                if (uiState.isLoading) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp,
                        )
                        Text("Criando conta...", fontSize = 16.sp, color = Color.White)
                    }
                } else {
                    Text("Finalizar", fontSize = 16.sp, color = Color.White)
                }
            }
        }
    }
}