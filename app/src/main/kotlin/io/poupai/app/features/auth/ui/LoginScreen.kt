package io.poupai.app.features.auth.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import io.poupai.app.R
import io.poupai.app.features.auth.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    onLoginSuccess: (userName: String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isLoginSuccess) {
        if (uiState.isLoginSuccess) {
            onLoginSuccess(uiState.loggedUserName)
        }
    }

    // Cores dos campos
    val fieldColors = TextFieldDefaults.colors(
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
        disabledContainerColor = Color.Transparent,
        focusedIndicatorColor = MaterialTheme.colorScheme.primary,   // linha roxa ao focar
        unfocusedIndicatorColor = Color(0xFFBDBDBD),                 // linha cinza quando inativo
        focusedLabelColor = MaterialTheme.colorScheme.primary,
        unfocusedLabelColor = Color(0xFF9E9E9E),
        cursorColor = MaterialTheme.colorScheme.primary,
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind { drawDecorativeEllipses() },
    ) {
        // ─── Logo + título no topo ───
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 72.dp, start = 32.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_texto),
                contentDescription = "Poupaí",
                modifier = Modifier.size(180.dp),
                contentScale = ContentScale.Fit,
            )

            Spacer(Modifier.height(20.dp))

            Text(
                text = "Bem-vindo!",
                fontSize = 50.sp,
                fontWeight = FontWeight.Light,
                color = Color.White,
            )
        }

        // ─── Formulário ───
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp),
        ) {
            Text(
                text = "Login",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1B1F),
            )

            Spacer(Modifier.height(24.dp))

            // ─── Campo Email ───
            TextField(
                value = uiState.email,
                onValueChange = viewModel::onEmailChanged,
                label = { Text("Email") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                trailingIcon = {
                    if (uiState.email.isNotBlank()) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors,
            )

            Spacer(Modifier.height(8.dp))

            // ─── Campo Senha ───
            TextField(
                value = uiState.password,
                onValueChange = viewModel::onPasswordChanged,
                label = { Text("Senha") },
                singleLine = true,
                visualTransformation = if (uiState.isPasswordVisible)
                    VisualTransformation.None
                else
                    PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = viewModel::onTogglePasswordVisibility) {
                        Icon(
                            imageVector = if (uiState.isPasswordVisible)
                                Icons.Default.Visibility
                            else
                                Icons.Default.VisibilityOff,
                            contentDescription = "Alternar visibilidade",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors,
            )

            // ─── Recuperar senha ───
            TextButton(
                onClick = { },
                modifier = Modifier.align(Alignment.Start),
                contentPadding = PaddingValues(0.dp),
            ) {
                Text(
                    text = "Recuperar senha?",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 14.sp,
                )
            }

            // ─── Erro ───
            uiState.errorMessage?.let { error ->
                Spacer(Modifier.height(4.dp))
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                )
            }

            Spacer(Modifier.height(16.dp))

            // ─── Botão Login ───
            Button(
                onClick = viewModel::onLoginClicked,
                enabled = uiState.isFormValid && !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF503173),
                    disabledContainerColor = Color(0xFF503173).copy(alpha = 0.5f),
                ),
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Login", fontSize = 16.sp, color = Color.White)
                        Text("→", fontSize = 18.sp, color = Color.White)
                    }
                }
            }
        }
    }
}

// ─── Círculos decorativos — idênticos ao WelcomeScreen ───
private fun DrawScope.drawDecorativeEllipses() {
    val w = size.width
    val h = size.height
    val ellipseW = w * 1.3f
    val ellipseH = h * 0.62f
    val pivotX = 0f
    val pivotY = 0f

    rotate(degrees = 28f, pivot = Offset(pivotX, pivotY)) {
        drawOval(
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFFF4F5FF), Color(0xFF513174)),
                start = Offset(0f, 0f),
                end = Offset(ellipseW, ellipseH),
            ),
            topLeft = Offset(-w * 0.25f, -h * 0.15f),
            size = Size(ellipseW, ellipseH),
        )
    }

    rotate(degrees = 28f, pivot = Offset(pivotX, pivotY)) {
        drawOval(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFFC630F8).copy(alpha = 0.75f),
                    Color(0xFF8F90FF).copy(alpha = 0.85f),
                ),
                start = Offset(0f, 0f),
                end = Offset(ellipseW * 0.9f, ellipseH * 0.9f),
            ),
            topLeft = Offset(-w * 0.35f, h * 0.04f),
            size = Size(ellipseW * 0.95f, ellipseH * 0.95f),
        )
    }

    rotate(degrees = 28f, pivot = Offset(pivotX, pivotY)) {
        drawOval(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFFD9A6F1).copy(alpha = 0.80f),
                    Color(0xFFC630F8).copy(alpha = 0.45f),
                ),
                start = Offset(0f, 0f),
                end = Offset(ellipseW * 0.85f, ellipseH * 0.85f),
            ),
            topLeft = Offset(-w * 0.05f, -h * 0.08f),
            size = Size(ellipseW * 0.88f, ellipseH * 0.88f),
        )
    }
}