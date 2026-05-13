package io.poupai.app.features.transactions.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import io.poupai.app.core.designsystem.components.EyeToggleIcon
import io.poupai.app.core.theme.GreenPositive
import io.poupai.app.core.theme.Purple40
import io.poupai.app.core.theme.PurpleDark
import io.poupai.app.core.theme.RedNegative
import io.poupai.app.core.util.toBRL
import io.poupai.app.domain.model.TransactionType
import io.poupai.app.features.transactions.components.TransactionItem
import io.poupai.app.features.transactions.state.TransactionFilter
import io.poupai.app.features.transactions.state.TransactionsUiState
import io.poupai.app.features.transactions.viewmodel.TransactionsViewModel
import java.time.Month
import java.time.format.TextStyle
import java.util.Locale

private const val HIDDEN = "••••"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    onNavigateBack: () -> Unit,
    viewModel: TransactionsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val addSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val editSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val fieldColors = TextFieldDefaults.colors(
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
        focusedIndicatorColor = Purple40,
        unfocusedIndicatorColor = Color(0xFFBDBDBD),
        focusedLabelColor = Purple40,
        unfocusedLabelColor = Color(0xFF9E9E9E),
        focusedTextColor = Color(0xFF1C1B1F),
        unfocusedTextColor = Color(0xFF1C1B1F),
        cursorColor = Purple40,
    )

    // ─── Dialogs ───
    if (uiState.showDeleteDialog && uiState.transactionToDelete != null) {
        AlertDialog(
            onDismissRequest = viewModel::onDeleteCancel,
            title = { Text("Excluir transação") },
            text = { Text("Deseja excluir \"${uiState.transactionToDelete!!.title}\"? Esta ação não pode ser desfeita.") },
            confirmButton = {
                Button(
                    onClick = viewModel::onDeleteConfirm,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Excluir") }
            },
            dismissButton = { TextButton(onClick = viewModel::onDeleteCancel) { Text("Cancelar") } },
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F7)),
    ) {
        // ─── Header ───
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(brush = Brush.verticalGradient(colors = listOf(PurpleDark, Purple40)))
                .padding(horizontal = 20.dp)
                .padding(top = 16.dp, bottom = 16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, "Voltar", tint = Color.White)
                }
                Spacer(Modifier.weight(1f))
                Text(
                    "Transações",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.weight(1f))
                EyeToggleIcon(
                    hideValues = uiState.hideValues, onToggle = viewModel::toggleHideValues
                )
            }
        }

        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Purple40)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f),
            ) {
                item { TransactionSummaryCard(uiState = uiState) }
                item {
                    MonthSelector(
                        uiState = uiState,
                        onPrevious = viewModel::onPreviousMonth,
                        onNext = viewModel::onNextMonth
                    )
                }
                item {
                    FilterChipsRow(
                        uiState = uiState, onFilterChanged = viewModel::onFilterChanged
                    )
                }

                if (uiState.filteredTransactions.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(1.dp)
                        ) {
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("💸", fontSize = 40.sp)
                                    Spacer(Modifier.height(12.dp))
                                    Text(
                                        if (uiState.allTransactions.isEmpty()) "Nenhuma transação ainda"
                                        else "Nenhuma transação neste período",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFF9E9E9E),
                                        textAlign = TextAlign.Center,
                                    )
                                    Text(
                                        "Toque em + para adicionar",
                                        fontSize = 12.sp,
                                        color = Color(0xFFBDBDBD)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    item {
                        Text(
                            "${uiState.filteredTransactions.size} transaç${if (uiState.filteredTransactions.size == 1) "ão" else "ões"}",
                            fontSize = 12.sp,
                            color = Color(0xFF9E9E9E),
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                    items(uiState.filteredTransactions, key = { it.id }) { transaction ->
                        TransactionItem(
                            transaction = transaction,
                            onDeleteClick = { viewModel.onDeleteRequest(it) },
                            onEditClick = { viewModel.onEditRequest(it) },
                            isDeleting = uiState.deletingId == transaction.id,
                        )
                    }
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }

    // ─── FAB ───
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomEnd) {
        FloatingActionButton(
            onClick = viewModel::onShowAddSheet,
            containerColor = Purple40,
            shape = CircleShape,
            modifier = Modifier.padding(24.dp)
        ) {
            Icon(Icons.Default.Add, "Adicionar transação", tint = Color.White)
        }
    }

    // ─── Sheet: adicionar ───
    if (uiState.showAddSheet) {
        ModalBottomSheet(onDismissRequest = viewModel::onDismissSheet, sheetState = addSheetState) {
            TransactionForm(
                title = "Nova Transação",
                formTitle = uiState.formTitle,
                formAmount = uiState.formAmount,
                formType = uiState.formType,
                formCategory = uiState.formCategory,
                formDate = uiState.formDate,
                formError = uiState.formError,
                isLoading = uiState.formIsLoading,
                isValid = uiState.isFormValid,
                categories = uiState.currentCategories,
                fieldColors = fieldColors,
                onTitleChanged = viewModel::onFormTitleChanged,
                onAmountChanged = viewModel::onFormAmountChanged,
                onTypeChanged = viewModel::onFormTypeChanged,
                onCategoryChanged = viewModel::onFormCategoryChanged,
                onDateChanged = viewModel::onFormDateChanged,
                onSave = viewModel::onAddTransaction,
                saveLabel = "Salvar transação",
            )
        }
    }

    // ─── Sheet: editar ───
    if (uiState.showEditSheet) {
        ModalBottomSheet(
            onDismissRequest = viewModel::onDismissEditSheet, sheetState = editSheetState
        ) {
            TransactionForm(
                title = "Editar Transação",
                formTitle = uiState.editTitle,
                formAmount = uiState.editAmount,
                formType = uiState.editType,
                formCategory = uiState.editCategory,
                formDate = uiState.editDate,
                formError = uiState.editError,
                isLoading = uiState.editIsLoading,
                isValid = uiState.isEditValid,
                categories = uiState.editCategories,
                fieldColors = fieldColors,
                onTitleChanged = viewModel::onEditTitleChanged,
                onAmountChanged = viewModel::onEditAmountChanged,
                onTypeChanged = viewModel::onEditTypeChanged,
                onCategoryChanged = viewModel::onEditCategoryChanged,
                onDateChanged = viewModel::onEditDateChanged,
                onSave = viewModel::onUpdateTransaction,
                saveLabel = "Salvar alterações",
            )
        }
    }
}

// ─── CARD RESUMO ───

@Composable
private fun TransactionSummaryCard(uiState: TransactionsUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(listOf(PurpleDark, Purple40)),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(20.dp)
        ) {
            Column {
                Text("Saldo atual", fontSize = 12.sp, color = Color.White.copy(alpha = 0.75f))
                Spacer(Modifier.height(4.dp))
                Text(
                    if (uiState.hideValues) HIDDEN else uiState.balance.toBRL(),
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(GreenPositive.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.TrendingUp,
                                null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(
                                "Receitas", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f)
                            )
                            Text(
                                if (uiState.hideValues) HIDDEN else uiState.incomeTotal.toBRL(),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(RedNegative.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.TrendingDown,
                                null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(
                                "Despesas", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f)
                            )
                            Text(
                                if (uiState.hideValues) HIDDEN else uiState.expenseTotal.toBRL(),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── SELETOR DE MÊS ───

@Composable
private fun MonthSelector(
    uiState: TransactionsUiState, onPrevious: () -> Unit, onNext: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onPrevious) {
                Icon(
                    Icons.Default.ArrowBackIosNew,
                    "Mês anterior",
                    tint = Purple40,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                "${
                    Month.of(uiState.selectedMonth)
                        .getDisplayName(TextStyle.FULL, Locale("pt", "BR"))
                        .replaceFirstChar { it.uppercase() }
                } ${uiState.selectedYear}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1C1B1F),
            )
            IconButton(onClick = onNext) {
                Icon(
                    Icons.Default.ArrowForwardIos,
                    "Próximo mês",
                    tint = Purple40,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// ─── FILTROS ───

@Composable
private fun FilterChipsRow(
    uiState: TransactionsUiState, onFilterChanged: (TransactionFilter) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(
            Triple(TransactionFilter.ALL, "Todos", Purple40),
            Triple(TransactionFilter.INCOME, "Receitas", GreenPositive),
            Triple(TransactionFilter.EXPENSE, "Despesas", RedNegative)
        ).forEach { (filter, label, color) ->
            FilterChip(
                selected = uiState.activeFilter == filter,
                onClick = { onFilterChanged(filter) },
                label = { Text(label, fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = color.copy(alpha = 0.12f),
                    selectedLabelColor = color,
                ),
            )
        }
    }
}

// ─── FORMULÁRIO COMPARTILHADO (Adicionar e Editar) ───

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TransactionForm(
    title: String,
    formTitle: String,
    formAmount: String,
    formType: TransactionType,
    formCategory: String,
    formDate: String,
    formError: String?,
    isLoading: Boolean,
    isValid: Boolean,
    categories: List<String>,
    fieldColors: TextFieldColors,
    onTitleChanged: (String) -> Unit,
    onAmountChanged: (String) -> Unit,
    onTypeChanged: (TransactionType) -> Unit,
    onCategoryChanged: (String) -> Unit,
    onDateChanged: (String) -> Unit,
    onSave: () -> Unit,
    saveLabel: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold)

        // Tipo
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(
                TransactionType.INCOME to "Receita", TransactionType.EXPENSE to "Despesa"
            ).forEach { (type, label) ->
                val color = if (type == TransactionType.INCOME) GreenPositive else RedNegative
                FilterChip(
                    selected = formType == type,
                    onClick = { onTypeChanged(type) },
                    label = { Text(label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = color.copy(alpha = 0.12f),
                        selectedLabelColor = color,
                    ),
                )
            }
        }

        TextField(
            value = formTitle,
            onValueChange = onTitleChanged,
            label = { Text("Título") },
            placeholder = { Text("Ex: Salário, Aluguel...") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = fieldColors
        )

        TextField(
            value = formAmount,
            onValueChange = onAmountChanged,
            label = { Text("Valor (R$)") },
            placeholder = { Text("0,00") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            colors = fieldColors
        )

        // ─── Categorias pré-definidas ───
        Text("Categoria", style = MaterialTheme.typography.labelMedium, color = Color(0xFF9E9E9E))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(categories) { category ->
                val selected = formCategory == category
                FilterChip(
                    selected = selected,
                    onClick = { onCategoryChanged(category) },
                    label = { Text(category, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Purple40.copy(alpha = 0.12f),
                        selectedLabelColor = Purple40,
                    ),
                )
            }
        }
        // Campo livre para categoria customizada
        TextField(
            value = formCategory,
            onValueChange = onCategoryChanged,
            label = { Text("Ou digite uma categoria") },
            placeholder = { Text("Ex: Airbnb, Pet...") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = fieldColors
        )

        TextField(
            value = formDate,
            onValueChange = onDateChanged,
            label = { Text("Data") },
            placeholder = { Text("dd/MM/yyyy") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = fieldColors
        )

        formError?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp) }

        Button(
            onClick = onSave,
            enabled = isValid && !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Purple40),
        ) {
            if (isLoading) CircularProgressIndicator(
                modifier = Modifier.size(24.dp), color = Color.White
            )
            else Text(saveLabel, fontSize = 16.sp, color = Color.White)
        }
    }
}