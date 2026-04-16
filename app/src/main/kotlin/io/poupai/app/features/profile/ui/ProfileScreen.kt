package io.poupai.app.features.profile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import io.poupai.app.core.theme.Purple40
import io.poupai.app.core.theme.PurpleDark
import io.poupai.app.features.profile.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier.fillMaxWidth().height(180.dp)
                .background(brush = Brush.verticalGradient(colors = listOf(PurpleDark, Purple40, Color.Transparent))),
        ) {
            Text("Perfil", style = MaterialTheme.typography.headlineLarge, color = Color.White,
                modifier = Modifier.padding(24.dp).padding(top = 32.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(64.dp).clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.Person, "Foto de perfil",
                    modifier = Modifier.size(36.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text("${uiState.firstName} ${uiState.lastName}", style = MaterialTheme.typography.titleLarge)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF4CAF50)))
                    Spacer(Modifier.width(4.dp))
                    Text("online", style = MaterialTheme.typography.labelSmall, color = Color(0xFF4CAF50))
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ProfileField("Usuário", uiState.username)
            ProfileField("Nome", uiState.firstName)
            ProfileField("Sobrenome", uiState.lastName)
            ProfileField("Data de Nascimento", uiState.birthDate)
        }

        Spacer(Modifier.weight(1f))

        OutlinedButton(
            onClick = { viewModel.onLogout { onNavigateBack() } },
            modifier = Modifier.fillMaxWidth().padding(24.dp).height(56.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Text("Sair")
        }
    }
}

@Composable
private fun ProfileField(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value.ifBlank { "—" }, style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary)
        HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
    }
}
