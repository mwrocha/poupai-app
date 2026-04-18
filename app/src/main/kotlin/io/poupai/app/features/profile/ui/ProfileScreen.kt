package io.poupai.app.features.profile.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import io.poupai.app.core.theme.Purple40
import io.poupai.app.core.theme.PurpleDark
import io.poupai.app.core.util.CpfVisualTransformation
import io.poupai.app.core.util.PhoneVisualTransformation
import io.poupai.app.features.profile.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showEmailDialog by remember { mutableStateOf(false) }
    var currentPassword by remember { mutableStateOf("") }

    val fieldColors = TextFieldDefaults.colors(
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
        unfocusedIndicatorColor = Color(0xFFBDBDBD),
        focusedTextColor = Color(0xFF1C1B1F),
        unfocusedTextColor = Color(0xFF1C1B1F),
        cursorColor = MaterialTheme.colorScheme.primary,
    )

    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.onProfileImageSelected(it, context) }
    }

    if (showEmailDialog) {
        AlertDialog(
            onDismissRequest = { showEmailDialog = false },
            title = { Text("Confirmar alteração de e-mail") },
            text = {
                Column {
                    Text(
                        "Para alterar o e-mail você será desconectado e precisará fazer login novamente.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(16.dp))
                    TextField(
                        value = currentPassword,
                        onValueChange = { currentPassword = it },
                        label = { Text("Senha atual") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = fieldColors,
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showEmailDialog = false
                        viewModel.onSave(currentPassword = currentPassword, onLogout = onLogout)
                        currentPassword = ""
                    },
                    enabled = currentPassword.isNotBlank(),
                ) { Text("Confirmar") }
            },
            dismissButton = {
                TextButton(onClick = { showEmailDialog = false; currentPassword = "" }) {
                    Text("Cancelar")
                }
            },
        )
    }

    LaunchedEffect(uiState.errorMessage, uiState.successMessage) {
        if (uiState.errorMessage != null || uiState.successMessage != null) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearMessages()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        // ─── Header ───
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .background(brush = Brush.verticalGradient(colors = listOf(PurpleDark, Purple40))),
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.align(Alignment.TopStart).padding(8.dp),
            ) {
                Icon(Icons.Default.ArrowBack, "Voltar", tint = Color.White)
            }
            Text(
                "Meu Perfil",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.align(Alignment.Center),
            )
        }

        // ─── Avatar ───
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-40).dp),
            contentAlignment = Alignment.Center,
        ) {
            Box {
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    if (!uiState.editProfileImageUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = uiState.editProfileImageUrl,
                            contentDescription = "Foto de perfil",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                    } else {
                        Text(
                            text = uiState.firstName.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable { imagePicker.launch("image/*") },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.CameraAlt, "Trocar foto",
                        tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
        }

        // ─── Campos ───
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .offset(y = (-24).dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            EditableField(
                label = "Usuário",
                value = uiState.editUsername,
                isEditing = uiState.editingField == "username",
                onFieldClick = { viewModel.onFieldClick("username") },
                onValueChange = viewModel::onUsernameChanged,
                colors = fieldColors,
            )
            EditableField(
                label = "Nome",
                value = uiState.editFirstName,
                isEditing = uiState.editingField == "firstName",
                onFieldClick = { viewModel.onFieldClick("firstName") },
                onValueChange = viewModel::onFirstNameChanged,
                colors = fieldColors,
            )
            EditableField(
                label = "Sobrenome",
                value = uiState.editLastName,
                isEditing = uiState.editingField == "lastName",
                onFieldClick = { viewModel.onFieldClick("lastName") },
                onValueChange = viewModel::onLastNameChanged,
                colors = fieldColors,
            )
            EditableField(
                label = "Data de Nascimento",
                value = uiState.editBirthDate,
                isEditing = uiState.editingField == "birthDate",
                onFieldClick = { viewModel.onFieldClick("birthDate") },
                onValueChange = viewModel::onBirthDateChanged,
                placeholder = "dd/MM/yyyy",
                colors = fieldColors,
            )
            EditableField(
                label = "E-mail",
                value = uiState.editEmail,
                isEditing = uiState.editingField == "email",
                onFieldClick = { viewModel.onFieldClick("email") },
                onValueChange = viewModel::onEmailChanged,
                keyboardType = KeyboardType.Email,
                colors = fieldColors,
                trailingNote = if (uiState.emailChanged) "⚠ Exigirá novo login" else null,
            )
            EditableField(
                label = "CPF",
                value = uiState.editCpf,
                isEditing = uiState.editingField == "cpf",
                onFieldClick = { viewModel.onFieldClick("cpf") },
                onValueChange = viewModel::onCpfChanged,
                keyboardType = KeyboardType.Number,
                placeholder = "000.000.000-00",
                visualTransformation = CpfVisualTransformation(),
                colors = fieldColors,
            )
            EditableField(
                label = "Telefone",
                value = uiState.editPhone,
                isEditing = uiState.editingField == "phone",
                onFieldClick = { viewModel.onFieldClick("phone") },
                onValueChange = viewModel::onPhoneChanged,
                keyboardType = KeyboardType.Number,
                placeholder = "(00) 00000-0000",
                visualTransformation = PhoneVisualTransformation(),
                colors = fieldColors,
                leadingText = "+55 ",
            )
        }

        Spacer(Modifier.height(8.dp))

        // ─── Mensagens próximas ao botão salvar ───
        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            uiState.errorMessage?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }
            uiState.successMessage?.let {
                Text(
                    it,
                    color = Color(0xFF4CAF50),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }
        }

        // ─── Botão salvar ───
        Button(
            onClick = {
                if (uiState.emailChanged) showEmailDialog = true
                else viewModel.onSave(onLogout = onLogout)
            },
            enabled = uiState.hasChanges && !uiState.isSaving,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF503173)),
        ) {
            if (uiState.isSaving) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
            } else {
                Text("Salvar alterações", fontSize = 16.sp, color = Color.White)
            }
        }

        Spacer(Modifier.height(12.dp))

        OutlinedButton(
            onClick = { viewModel.onLogout(onLogout) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Text("Sair")
        }

        Spacer(Modifier.height(32.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditableField(
    label: String,
    value: String,
    isEditing: Boolean,
    onFieldClick: () -> Unit,
    onValueChange: (String) -> Unit,
    colors: TextFieldColors,
    placeholder: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingNote: String? = null,
    leadingText: String? = null,
) {
    val displayValue = if (visualTransformation != VisualTransformation.None && value.isNotBlank()) {
        visualTransformation.filter(
            androidx.compose.ui.text.AnnotatedString(value)
        ).text.text
    } else value

    Column(modifier = Modifier.fillMaxWidth()) {
        if (isEditing) {
            TextField(
                value = value,
                onValueChange = onValueChange,
                label = { Text(label) },
                placeholder = { Text(placeholder) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                visualTransformation = visualTransformation,
                modifier = Modifier.fillMaxWidth(),
                colors = colors,
                prefix = if (leadingText != null) {
                    { Text(leadingText, color = Color(0xFF6B6B6B)) }
                } else null,
                trailingIcon = if (trailingNote != null) {
                    { Text(trailingNote, fontSize = 10.sp, color = Color(0xFFFF9800)) }
                } else null,
            )
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onFieldClick() }
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(label, style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(2.dp))
                    Text(
                        if (leadingText != null && displayValue.isNotBlank())
                            "$leadingText$displayValue"
                        else displayValue.ifBlank { placeholder.ifBlank { "—" } },
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (displayValue.isBlank()) Color(0xFFBDBDBD)
                        else MaterialTheme.colorScheme.onSurface,
                    )
                }
                Icon(Icons.Default.Edit, "Editar",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(18.dp))
            }
            HorizontalDivider()
        }
    }
}