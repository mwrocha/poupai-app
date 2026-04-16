package io.poupai.app.features.finances.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import io.poupai.app.features.finances.viewmodel.FinancesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinancesScreen(
    onNavigateBack: () -> Unit,
    viewModel: FinancesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Finanças Pessoais") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.Menu, "Menu")
                }
            },
        )

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                Spacer(Modifier.height(8.dp))

                Text("Gastos e Receitas", style = MaterialTheme.typography.titleMedium)
                Card(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Gráfico em breve", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Text("Rendimentos", style = MaterialTheme.typography.titleMedium)
                Card(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Gráfico em breve", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
