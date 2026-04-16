package io.poupai.app.features.goals.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * Tela de Metas — stub para desenvolvimento futuro.
 * Referenciada no menu lateral (Frame 9).
 */
@Composable
fun GoalsScreen(
    onNavigateBack: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Metas — em breve", style = MaterialTheme.typography.headlineMedium)
    }
}
