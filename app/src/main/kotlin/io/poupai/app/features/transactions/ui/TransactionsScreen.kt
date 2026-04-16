package io.poupai.app.features.transactions.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Remove
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
import io.poupai.app.core.theme.GreenPositive
import io.poupai.app.core.theme.RedNegative
import io.poupai.app.core.util.toBRL
import io.poupai.app.core.util.toDisplayFormat
import io.poupai.app.features.transactions.components.TransactionItem
import io.poupai.app.features.transactions.viewmodel.TransactionsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    onNavigateBack: () -> Unit,
    viewModel: TransactionsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        // Header roxo com saldo
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(PurpleDark, Purple40),
                    ),
                )
                .padding(24.dp)
                .padding(top = 16.dp),
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Voltar", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "Transações",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.size(48.dp)) // Balanceamento
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = uiState.balance.toBRL(),
                    style = MaterialTheme.typography.displayLarge,
                    color = Color.White,
                )

                if (uiState.comparisonPercentage != 0.0) {
                    Text(
                        text = "Você gastou ${uiState.comparisonPercentage.toInt()}% a menos que o mês anterior",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f),
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Card receitas e despesas
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .offset(y = (-16).dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                // Receitas
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Receitas",
                        tint = GreenPositive,
                        modifier = Modifier.size(24.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = uiState.incomeTotal.toBRL(),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = "Receitas",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                // Despesas
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Remove,
                        contentDescription = "Despesas",
                        tint = RedNegative,
                        modifier = Modifier.size(24.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = uiState.expenseTotal.toBRL(),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = "Despesas",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        // TODO: gráfico semanal (Frame 6)

        // Lista de transações recentes
        Text(
            text = "Transações Recentes",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(uiState.recentTransactions) { transaction ->
                TransactionItem(transaction = transaction)
            }
        }
    }
}
