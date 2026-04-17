package io.poupai.app.features.transactions.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import io.poupai.app.core.theme.GreenPositive
import io.poupai.app.core.theme.Purple40
import io.poupai.app.core.theme.PurpleDark
import io.poupai.app.core.theme.RedNegative
import io.poupai.app.core.util.toBRL
import io.poupai.app.domain.model.TransactionType
import io.poupai.app.features.transactions.components.TransactionItem
import io.poupai.app.features.transactions.viewmodel.TransactionsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    onNavigateBack: () -> Unit,
    viewModel: TransactionsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val fieldColors = TextFieldDefaults.colors(
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
        unfocusedIndicatorColor = Color(0xFFBDBDBD),
        focusedLabelColor = MaterialTheme.colorScheme.primary,
        unfocusedLabelColor = Color(0xFF9E9E9E),
        focusedTextColor = Color(0xFF1C1B1F),
        unfocusedTextColor = Color(0xFF1C1B1F),
        cursorColor = MaterialTheme.colorScheme.primary,
    )

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = viewModel::onShowAddSheet,
                containerColor = MaterialTheme.colorScheme.primary,
            ) {
                Icon(Icons.Default.Add, "Adicionar transação", tint = Color.White)
            }
        },
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            // ─── Header ───
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(colors = listOf(PurpleDark, Purple40)),
                    )
                    .padding(24.dp)
                    .padding(top = 16.dp),
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Default.ArrowBack, "Voltar", tint = Color.White)
                        }
                        Spacer(Modifier.weight(1f))
                        Text("Transações", style = MaterialTheme.typography.titleLarge,
                            color = Color.White)
                        Spacer(Modifier.weight(1f))
                        Spacer(Modifier.size(48.dp))
                    }

                    Spacer(Modifier.height(16.dp))

                    Text(uiState.balance.toBRL(),
                        style = MaterialTheme.typography.displayLarge, color = Color.White)

                    Spacer(Modifier.height(16.dp))
                }
            }

            // ─── Card receitas/despesas ───
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Add, "Receitas", tint = GreenPositive,
                            modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(uiState.incomeTotal.toBRL(),
                                style = MaterialTheme.typography.titleMedium)
                            Text("Receitas", style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Remove, "Despesas", tint = RedNegative,
                            modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(uiState.expenseTotal.toBRL(),
                                style = MaterialTheme.typography.titleMedium)
                            Text("Despesas", style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // ─── Lista ───
            Text("Transações Recentes",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp))

            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.recentTransactions.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Nenhuma transação ainda",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(8.dp))
                        Text("Toque em + para adicionar",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(uiState.recentTransactions) { transaction ->
                        TransactionItem(transaction = transaction)
                    }
                }
            }
        }
    }

    // ─── BottomSheet adicionar transação ───
    if (uiState.showAddSheet) {
        ModalBottomSheet(
            onDismissRequest = viewModel::onDismissSheet,
            sheetState = sheetState,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("Nova Transação",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold)

                // ─── Tipo: Receita / Despesa ───
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    listOf(TransactionType.INCOME to "Receita",
                        TransactionType.EXPENSE to "Despesa").forEach { (type, label) ->
                        val selected = uiState.formType == type
                        FilterChip(
                            selected = selected,
                            onClick = { viewModel.onFormTypeChanged(type) },
                            label = { Text(label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = if (type == TransactionType.INCOME)
                                    GreenPositive.copy(alpha = 0.15f) else RedNegative.copy(alpha = 0.15f),
                                selectedLabelColor = if (type == TransactionType.INCOME)
                                    GreenPositive else RedNegative,
                            ),
                        )
                    }
                }

                // ─── Título ───
                TextField(
                    value = uiState.formTitle,
                    onValueChange = viewModel::onFormTitleChanged,
                    label = { Text("Título") },
                    placeholder = { Text("Ex: Salário, Aluguel...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = fieldColors,
                )

                // ─── Valor ───
                TextField(
                    value = uiState.formAmount,
                    onValueChange = viewModel::onFormAmountChanged,
                    label = { Text("Valor (R$)") },
                    placeholder = { Text("0,00") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    colors = fieldColors,
                )

                // ─── Categoria ───
                TextField(
                    value = uiState.formCategory,
                    onValueChange = viewModel::onFormCategoryChanged,
                    label = { Text("Categoria") },
                    placeholder = { Text("Ex: Alimentação, Transporte...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = fieldColors,
                )

                // ─── Data ───
                TextField(
                    value = uiState.formDate,
                    onValueChange = viewModel::onFormDateChanged,
                    label = { Text("Data") },
                    placeholder = { Text("dd/MM/yyyy") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = fieldColors,
                )

                // ─── Erro ───
                uiState.formError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }

                // ─── Botão salvar ───
                Button(
                    onClick = viewModel::onAddTransaction,
                    enabled = uiState.isFormValid && !uiState.formIsLoading,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF503173),
                    ),
                ) {
                    if (uiState.formIsLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp),
                            color = Color.White)
                    } else {
                        Text("Salvar transação", fontSize = 16.sp, color = Color.White)
                    }
                }
            }
        }
    }
}