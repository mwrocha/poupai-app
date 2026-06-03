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
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import io.poupai.app.core.designsystem.components.EyeToggleIcon
import io.poupai.app.core.designsystem.components.PoupaiDrawerScaffold
import io.poupai.app.core.designsystem.components.PullToRefresh
import io.poupai.app.core.designsystem.components.TopLevelNavCallbacks
import io.poupai.app.core.theme.PoupaiTheme
import io.poupai.app.core.theme.Purple40
import io.poupai.app.core.theme.Purple60
import io.poupai.app.core.theme.PurpleDark
import io.poupai.app.core.theme.PurpleLight
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
    topLevelNav: TopLevelNavCallbacks,
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
        unfocusedLabelColor = PoupaiTheme.tokens.textMuted,
        focusedTextColor = PoupaiTheme.tokens.textPrimary,
        unfocusedTextColor = PoupaiTheme.tokens.textPrimary,
        cursorColor = Purple40,
    )

    if (uiState.showDeleteDialog && uiState.transactionToDelete != null) {
        AlertDialog(
            onDismissRequest = viewModel::onDeleteCancel,
            title = { Text("Excluir transação") },
            text = { Text("Deseja excluir \"${uiState.transactionToDelete!!.title}\"? Esta ação não pode ser desfeita.") },
            confirmButton = {
                Button(
                    onClick = viewModel::onDeleteConfirm,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                ) { Text("Excluir") }
            },
            dismissButton = { TextButton(onClick = viewModel::onDeleteCancel) { Text("Cancelar") } },
            shape = RoundedCornerShape(16.dp),
        )
    }

    PoupaiDrawerScaffold(
        selectedRoute = "transactions",
        nav = topLevelNav,
    ) { onMenuClick ->
    Column(modifier = Modifier.fillMaxSize().background(PoupaiTheme.tokens.bg)) {

        // ─── Header ───
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(PurpleDark, Purple40)))
                .padding(horizontal = 20.dp)
                .padding(top = 16.dp, bottom = 16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onMenuClick) {
                    Icon(Icons.Default.Menu, "Menu", tint = Color.White)
                }
                Spacer(Modifier.weight(1f))
                Text(
                    "Transações",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.weight(1f))
                EyeToggleIcon(hideValues = uiState.hideValues, onToggle = viewModel::toggleHideValues)
            }
        }

        uiState.errorMessage?.let { err ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
            ) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(err, Modifier.weight(1f), fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onErrorContainer)
                    TextButton(onClick = viewModel::clearError) { Text("Ok") }
                }
            }
        }

        PullToRefresh(
            isRefreshing = uiState.isRefreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier.weight(1f),
        ) {
            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Purple40)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    item {
                        BalanceHeroCard(
                            uiState = uiState,
                            onPreviousMonth = viewModel::onPreviousMonth,
                            onNextMonth = viewModel::onNextMonth,
                        )
                    }

                    item { SectionTitle("Filtros", Icons.Default.FilterList) }
                    item {
                        FilterChipsRow(
                            uiState = uiState,
                            onFilterChanged = viewModel::onFilterChanged,
                        )
                    }

                    item {
                        SectionTitle(
                            "${uiState.filteredTransactions.size} " +
                                if (uiState.filteredTransactions.size == 1) "transação" else "transações",
                            Icons.Default.Receipt,
                        )
                    }

                    if (uiState.filteredTransactions.isEmpty()) {
                        item { EmptyState(allEmpty = uiState.allTransactions.isEmpty()) }
                    } else {
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
    }
    } // close PoupaiDrawerScaffold

    // ─── FAB ───
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomEnd) {
        FloatingActionButton(
            onClick = viewModel::onShowAddSheet,
            containerColor = Purple40,
            shape = CircleShape,
            modifier = Modifier.padding(24.dp),
        ) {
            Icon(Icons.Default.Add, "Adicionar transação", tint = Color.White)
        }
    }

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

    if (uiState.showEditSheet) {
        ModalBottomSheet(
            onDismissRequest = viewModel::onDismissEditSheet, sheetState = editSheetState,
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

@Composable
private fun SectionTitle(text: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 4.dp, start = 2.dp),
    ) {
        Icon(icon, null, tint = Purple40, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text(
            text,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = PoupaiTheme.tokens.textSecondary,
        )
    }
}

// ─── BALANCE HERO ───

@Composable
private fun BalanceHeroCard(
    uiState: TransactionsUiState,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
) {
    val monthLabel = "${
        Month.of(uiState.selectedMonth)
            .getDisplayName(TextStyle.FULL, Locale("pt", "BR"))
            .replaceFirstChar { it.uppercase() }
    } ${uiState.selectedYear}"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(6.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(listOf(PurpleDark, Purple40, Color(0xFF6B4396))),
                    RoundedCornerShape(22.dp),
                )
                .padding(20.dp),
        ) {
            Column {
                // ─── Barra de mês integrada ───
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    IconButton(
                        onClick = onPreviousMonth,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f)),
                    ) {
                        Icon(
                            Icons.Default.ArrowBackIosNew,
                            "Mês anterior",
                            tint = Color.White,
                            modifier = Modifier.size(12.dp),
                        )
                    }
                    Text(
                        monthLabel,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                    )
                    IconButton(
                        onClick = onNextMonth,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f)),
                    ) {
                        Icon(
                            Icons.Default.ArrowForwardIos,
                            "Próximo mês",
                            tint = Color.White,
                            modifier = Modifier.size(12.dp),
                        )
                    }
                }

                Spacer(Modifier.height(18.dp))
                Text(
                    "Saldo do mês",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.65f),
                )
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    if (uiState.balance < 0) {
                        Text(
                            "−",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.headlineMedium,
                        )
                    }
                    Text(
                        if (uiState.hideValues) HIDDEN else kotlin.math.abs(uiState.balance).toBRL(),
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                    )
                }

                Spacer(Modifier.height(18.dp))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color.White.copy(alpha = 0.15f)),
                )
                Spacer(Modifier.height(14.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    InlineFlowStat(
                        modifier = Modifier.weight(1f),
                        label = "Receitas",
                        value = if (uiState.hideValues) HIDDEN else uiState.incomeTotal.toBRL(),
                        icon = Icons.Default.ArrowUpward,
                    )
                    Box(
                        Modifier.width(1.dp).height(40.dp)
                            .background(Color.White.copy(alpha = 0.15f)),
                    )
                    InlineFlowStat(
                        modifier = Modifier.weight(1f),
                        label = "Despesas",
                        value = if (uiState.hideValues) HIDDEN else uiState.expenseTotal.toBRL(),
                        icon = Icons.Default.ArrowDownward,
                    )
                }
            }
        }
    }
}

@Composable
private fun InlineFlowStat(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    icon: ImageVector,
) {
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, null, tint = Color.White, modifier = Modifier.size(12.dp))
            }
            Spacer(Modifier.width(6.dp))
            Text(
                label,
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.7f),
                fontWeight = FontWeight.SemiBold,
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            value,
            fontSize = 16.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

// ─── FILTROS ───

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterChipsRow(
    uiState: TransactionsUiState, onFilterChanged: (TransactionFilter) -> Unit,
) {
    val options = listOf(
        TransactionFilter.ALL to "Todos",
        TransactionFilter.INCOME to "Receitas",
        TransactionFilter.EXPENSE to "Despesas",
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(PoupaiTheme.tokens.surface)
            .padding(5.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        options.forEach { (filter, label) ->
            val isSelected = uiState.activeFilter == filter
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                color = if (isSelected) PoupaiTheme.tokens.accentBright else Color.Transparent,
                onClick = { onFilterChanged(filter) },
            ) {
                Text(
                    label,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) Color.White else PoupaiTheme.tokens.textMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 9.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

// ─── EMPTY STATE ───

@Composable
private fun EmptyState(allEmpty: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = PoupaiTheme.tokens.surface),
        elevation = CardDefaults.cardElevation(1.dp),
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .padding(40.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(PurpleLight.copy(alpha = 0.55f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Inbox, null, tint = Purple40, modifier = Modifier.size(28.dp))
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    if (allEmpty) "Nenhuma transação ainda" else "Nenhuma transação neste período",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = PoupaiTheme.tokens.textSecondary,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Toque em + para adicionar",
                    fontSize = 12.sp,
                    color = PoupaiTheme.tokens.textMuted,
                )
            }
        }
    }
}

// ─── FORMULÁRIO COMPARTILHADO ───

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
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = PoupaiTheme.tokens.textPrimary)

        // Tipo — segmented na paleta roxa
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(PoupaiTheme.tokens.surfaceAlt)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            listOf(
                TransactionType.INCOME to ("Receita" to Icons.Default.ArrowUpward),
                TransactionType.EXPENSE to ("Despesa" to Icons.Default.ArrowDownward),
            ).forEach { (type, payload) ->
                val (label, icon) = payload
                val isSelected = formType == type
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(9.dp),
                    color = if (isSelected) PoupaiTheme.tokens.accentBright else Color.Transparent,
                    onClick = { onTypeChanged(type) },
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 9.dp),
                    ) {
                        Icon(
                            icon, null,
                            tint = if (isSelected) Color.White else PoupaiTheme.tokens.textMuted,
                            modifier = Modifier.size(14.dp),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            label,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else PoupaiTheme.tokens.textMuted,
                        )
                    }
                }
            }
        }

        TextField(
            value = formTitle,
            onValueChange = onTitleChanged,
            label = { Text("Título") },
            placeholder = { Text("Ex: Salário, Aluguel...") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = fieldColors,
        )

        TextField(
            value = formAmount,
            onValueChange = onAmountChanged,
            label = { Text("Valor (R$)") },
            placeholder = { Text("0,00") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            colors = fieldColors,
        )

        Text("Categoria", style = MaterialTheme.typography.labelMedium, color = PoupaiTheme.tokens.textMuted)
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
        TextField(
            value = formCategory,
            onValueChange = onCategoryChanged,
            label = { Text("Ou digite uma categoria") },
            placeholder = { Text("Ex: Airbnb, Pet...") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = fieldColors,
        )

        TextField(
            value = formDate,
            onValueChange = onDateChanged,
            label = { Text("Data") },
            placeholder = { Text("dd-MM-yyyy") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            colors = fieldColors,
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
                modifier = Modifier.size(24.dp), color = Color.White,
            )
            else Text(saveLabel, fontSize = 16.sp, color = Color.White)
        }
    }
}
