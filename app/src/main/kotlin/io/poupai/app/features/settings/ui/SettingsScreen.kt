package io.poupai.app.features.settings.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * Tela de Configurações — stub para desenvolvimento futuro.
 * Referenciada no menu lateral (Frame 9).
 */
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Configurações — em breve", style = MaterialTheme.typography.headlineMedium)
    }
}
