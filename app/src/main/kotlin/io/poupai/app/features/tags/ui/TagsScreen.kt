package io.poupai.app.features.tags.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import io.poupai.app.core.theme.Purple40
import io.poupai.app.core.theme.PurpleDark
import io.poupai.app.core.util.toBRL
import io.poupai.app.features.tags.components.TagCard
import io.poupai.app.features.tags.viewmodel.TagsViewModel

@Composable
fun TagsScreen(
    onNavigateBack: () -> Unit,
    viewModel: TagsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        // Header roxo
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(PurpleDark, Purple40),
                    ),
                )
                .padding(24.dp),
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Voltar", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Text("Tags", style = MaterialTheme.typography.titleLarge, color = Color.White)
                    Spacer(modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.size(48.dp))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Maiores gastos por categoria",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f),
                )
                Text(
                    text = uiState.totalSpent.toBRL(),
                    style = MaterialTheme.typography.displayLarge,
                    color = Color.White,
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Seletor de mês
        // TODO: dropdown de meses

        // Grid de tags (2 colunas como no protótipo)
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f),
        ) {
            items(uiState.filteredTags) { tag ->
                TagCard(tag = tag)
            }
        }

        // Barra de busca
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = viewModel::onSearchQueryChanged,
            placeholder = { Text("Pesquisar tag...") },
            leadingIcon = { Icon(Icons.Default.Search, "Pesquisar") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            shape = RoundedCornerShape(16.dp),
        )
    }
}
