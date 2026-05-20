package io.poupai.app.features.investmentbook.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Sync
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
import io.poupai.app.core.designsystem.components.PullToRefresh
import io.poupai.app.core.theme.PoupaiTheme
import io.poupai.app.core.theme.Purple40
import io.poupai.app.core.theme.Purple60
import io.poupai.app.core.theme.PurpleDark
import io.poupai.app.core.theme.PurpleLight
import io.poupai.app.core.util.toBRL
import io.poupai.app.domain.model.EntryType
import io.poupai.app.domain.model.InvestmentEntry
import io.poupai.app.domain.model.InvestmentType
import io.poupai.app.features.investmentbook.state.InvestmentBookUiState
import io.poupai.app.features.investmentbook.viewmodel.InvestmentBookViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvestmentBookScreen(
    onNavigateBack: () -> Unit,
    viewModel: InvestmentBookViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val fieldColors = TextFieldDefaults.colors(
        focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent,
        focusedIndicatorColor = PoupaiTheme.tokens.accentBright, unfocusedIndicatorColor = PoupaiTheme.tokens.textMuted,
        focusedLabelColor = PoupaiTheme.tokens.accentBright, unfocusedLabelColor = PoupaiTheme.tokens.textMuted,
        focusedTextColor = PoupaiTheme.tokens.textPrimary, unfocusedTextColor = PoupaiTheme.tokens.textPrimary, cursorColor = PoupaiTheme.tokens.accentBright,
    )

    val listState = uiState.listState
    val formState = uiState.formState

    if (listState.showDeleteDialog && listState.deletingEntry != null) {
        AlertDialog(
            onDismissRequest = viewModel::onDeleteCancel,
            title = { Text("Excluir lançamento") },
            text = { Text("Deseja excluir este lançamento? A operação será revertida no ativo.") },
            confirmButton = {
                Button(onClick = viewModel::onDeleteConfirm,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                    Text("Excluir")
                }
            },
            dismissButton = { TextButton(onClick = viewModel::onDeleteCancel) { Text("Cancelar") } },
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(PoupaiTheme.tokens.bg)) {

        Box(modifier = Modifier.fillMaxWidth()
            .background(brush = Brush.verticalGradient(colors = listOf(PurpleDark, Purple40)))
            .padding(horizontal = 20.dp).padding(top = 16.dp, bottom = 16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, "Voltar", tint = Color.White) }
                Spacer(Modifier.weight(1f))
                Text("Livro Contábil", style = MaterialTheme.typography.titleLarge,
                    color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                Spacer(Modifier.size(48.dp))
            }
        }

        PullToRefresh(
            isRefreshing = listState.isRefreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier.weight(1f),
        ) {
        if (listState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Purple40)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                item { BookSummaryCard(listState) }
                item { BookFilters(listState = listState, viewModel = viewModel) }

                if (listState.entries.isEmpty()) {
                    item {
                        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = PoupaiTheme.tokens.surface),
                            elevation = CardDefaults.cardElevation(1.dp)) {
                            Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        Modifier.size(56.dp).clip(CircleShape)
                                            .background(PurpleLight.copy(alpha = 0.55f)),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(
                                            Icons.Default.Book, null,
                                            tint = Purple40, modifier = Modifier.size(28.dp),
                                        )
                                    }
                                    Spacer(Modifier.height(12.dp))
                                    Text("Nenhum lançamento encontrado",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = PoupaiTheme.tokens.textSecondary, textAlign = TextAlign.Center)
                                    Spacer(Modifier.height(4.dp))
                                    Text("Toque em + para registrar", fontSize = 12.sp,
                                        color = PoupaiTheme.tokens.textMuted)
                                }
                            }
                        }
                    }
                } else {
                    item {
                        Text("${listState.totalEntries} lançamento${if (listState.totalEntries != 1L) "s" else ""}",
                            fontSize = 12.sp, color = PoupaiTheme.tokens.textMuted, modifier = Modifier.padding(horizontal = 4.dp))
                    }
                    item {
                        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = PoupaiTheme.tokens.surface),
                            elevation = CardDefaults.cardElevation(1.dp)) {
                            Column {
                                listState.entries.forEachIndexed { index, entry ->
                                    EntryRow(entry = entry, onDelete = { viewModel.onDeleteRequest(entry) })
                                    if (index < listState.entries.lastIndex)
                                        HorizontalDivider(color = PoupaiTheme.tokens.divider,
                                            modifier = Modifier.padding(horizontal = 16.dp))
                                }
                            }
                        }
                    }
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
        } // close PullToRefresh
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomEnd) {
        FloatingActionButton(onClick = viewModel::onShowAddSheet, containerColor = Purple40,
            shape = CircleShape, modifier = Modifier.padding(24.dp)) {
            Icon(Icons.Default.Add, "Novo lançamento", tint = Color.White)
        }
    }

    if (formState.showSheet) {
        ModalBottomSheet(onDismissRequest = viewModel::onDismissSheet, sheetState = sheetState) {
            AddEntryForm(
                formState = formState, investments = listState.investments, fieldColors = fieldColors,
                onToggleNewAsset = viewModel::onToggleNewAsset,
                onInvestmentSelected = viewModel::onFormInvestmentSelected,
                onNewAssetNameChanged = viewModel::onNewAssetNameChanged,
                onNewAssetTypeChanged = viewModel::onNewAssetTypeChanged,
                onTypeChanged = viewModel::onFormTypeChanged,
                onSharesChanged = viewModel::onFormSharesChanged,
                onSharePriceChanged = viewModel::onFormSharePriceChanged,
                onNewCurrentValueChanged = viewModel::onFormNewCurrentValueChanged,
                onNotesChanged = viewModel::onFormNotesChanged,
                onDateChanged = viewModel::onFormDateChanged,
                onSave = viewModel::onSaveEntry,
            )
        }
    }
}

// ─── CARD RESUMO ───

@Composable
private fun BookSummaryCard(listState: io.poupai.app.features.investmentbook.state.InvestmentBookListState) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(4.dp)) {
        Box(modifier = Modifier.fillMaxWidth()
            .background(
                brush = Brush.linearGradient(listOf(PurpleDark, Purple40, Color(0xFF6B4396))),
                shape = RoundedCornerShape(20.dp),
            )
            .padding(20.dp)) {
            Column {
                Text("Movimentações", fontSize = 12.sp, color = Color.White.copy(alpha = 0.75f))
                Text("${listState.totalEntries} lançamento${if (listState.totalEntries != 1L) "s" else ""}",
                    style = MaterialTheme.typography.headlineSmall, color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Total aportado", fontSize = 11.sp, color = Color.White.copy(alpha = 0.65f))
                        Text(listState.totalAported.toBRL(), fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Total resgatado", fontSize = 11.sp, color = Color.White.copy(alpha = 0.65f))
                        Text(listState.totalRescued.toBRL(), fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

// ─── FILTROS ───

@Composable
private fun BookFilters(
    listState: io.poupai.app.features.investmentbook.state.InvestmentBookListState,
    viewModel: InvestmentBookViewModel,
) {
    var showInvestmentPicker by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.FilterList, null, tint = PoupaiTheme.tokens.textMuted, modifier = Modifier.size(16.dp))
            Text("Filtrar:", fontSize = 12.sp, color = PoupaiTheme.tokens.textMuted)
            if (listState.selectedInvestmentName != null) {
                FilterChip(selected = true, onClick = { viewModel.onFilterInvestment(null, null) },
                    label = { Text(listState.selectedInvestmentName, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Purple40.copy(alpha = 0.12f), selectedLabelColor = Purple40))
            } else {
                FilterChip(selected = false, onClick = { showInvestmentPicker = !showInvestmentPicker },
                    label = { Text("Ativo", fontSize = 11.sp) })
            }
            if (listState.selectedInvestmentId != null || listState.selectedMonth != null) {
                TextButton(onClick = viewModel::onClearFilters, contentPadding = PaddingValues(horizontal = 8.dp)) {
                    Text("Limpar", fontSize = 11.sp, color = PoupaiTheme.tokens.textSecondary)
                }
            }
        }
        if (showInvestmentPicker && listState.investments.isNotEmpty()) {
            AlertDialog(
                onDismissRequest = { showInvestmentPicker = false },
                title = { Text("Filtrar por ativo", fontWeight = FontWeight.SemiBold) },
                text = {
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(listState.investments) { inv ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.onFilterInvestment(inv.id, inv.name)
                                        showInvestmentPicker = false
                                    }
                                    .padding(vertical = 12.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(inv.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.weight(1f), color = PoupaiTheme.tokens.textPrimary)
                                Text(when (inv.type) {
                                    InvestmentType.RENDA_VARIAVEL -> "Renda Variável"
                                    InvestmentType.RENDA_FIXA -> "Renda Fixa"
                                    InvestmentType.CRIPTOMOEDAS -> "Criptomoedas"
                                }, fontSize = 10.sp, color = PoupaiTheme.tokens.textMuted)
                            }
                            HorizontalDivider(color = PoupaiTheme.tokens.divider)
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showInvestmentPicker = false }) {
                        Text("Cancelar")
                    }
                },
                shape = RoundedCornerShape(16.dp),
            )
        }
    }
}

// ─── LINHA DE LANÇAMENTO ───

@Composable
private fun EntryRow(entry: InvestmentEntry, onDelete: () -> Unit) {
    val (color, icon: ImageVector) = when (entry.type) {
        EntryType.APORTE -> Purple40 to Icons.Default.ArrowUpward
        EntryType.RESGATE -> Color(0xFF7C5295) to Icons.Default.ArrowDownward
        EntryType.ATUALIZACAO_VALOR -> Purple60 to Icons.Default.Sync
    }
    val typeLabel = when (entry.type) {
        EntryType.APORTE -> "Aporte"
        EntryType.RESGATE -> "Resgate"
        EntryType.ATUALIZACAO_VALOR -> "Atualização"
    }

    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(color.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(entry.investmentName, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                color = PoupaiTheme.tokens.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(4.dp), color = color.copy(alpha = 0.10f)) {
                    Text(typeLabel, fontSize = 9.sp, color = color, fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                }
                Text(io.poupai.app.core.util.DateFormatter.isoToDisplay(entry.date),
                    fontSize = 11.sp, color = PoupaiTheme.tokens.textMuted)
            }
            when (entry.type) {
                EntryType.APORTE, EntryType.RESGATE -> {
                    if ((entry.shares ?: 0.0) > 0)
                        Text("${String.format("%.2f", entry.shares)} cotas × ${entry.sharePrice?.toBRL() ?: "—"}",
                            fontSize = 10.sp, color = PoupaiTheme.tokens.textMuted)
                    if (entry.type == EntryType.APORTE && (entry.newAveragePrice ?: 0.0) > 0)
                        Text("Novo PM: ${entry.newAveragePrice?.toBRL()}", fontSize = 10.sp,
                            color = color, fontWeight = FontWeight.SemiBold)
                }
                else -> Unit
            }
            entry.notes?.let {
                Text(it, fontSize = 10.sp, color = PoupaiTheme.tokens.textMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            entry.totalValue?.let {
                Text(it.toBRL(), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = color)
            }
        }
        Spacer(Modifier.width(4.dp))
        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.Delete, "Excluir", tint = PoupaiTheme.tokens.textMuted, modifier = Modifier.size(16.dp))
        }
    }
}

// ─── CARD DE POSIÇÃO ATUAL (mostrado em RESGATE) ───

@Composable
private fun CurrentPositionCard(investment: io.poupai.app.domain.model.Investment) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = PurpleLight.copy(alpha = 0.55f)),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.AccountBalanceWallet, null,
                    tint = Purple40, modifier = Modifier.size(14.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    "Sua posição atual em ${investment.name}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PoupaiTheme.tokens.textSecondary,
                )
            }
            Spacer(Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                PositionStat(
                    label = "Cotas",
                    value = "%.4f".format(investment.shares).trimEnd('0').trimEnd('.'),
                )
                if (investment.averagePrice > 0) {
                    PositionStat(label = "PM", value = investment.averagePrice.toBRL())
                }
                PositionStat(
                    label = "Valor atual",
                    value = investment.currentValue.toBRL(),
                    align = Alignment.End,
                )
            }
        }
    }
}

@Composable
private fun PositionStat(
    label: String,
    value: String,
    align: Alignment.Horizontal = Alignment.Start,
) {
    Column(horizontalAlignment = align) {
        Text(label, fontSize = 9.sp, color = PoupaiTheme.tokens.textMuted)
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PoupaiTheme.tokens.textPrimary)
    }
}

// ─── FORMULÁRIO ───

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEntryForm(
    formState: io.poupai.app.features.investmentbook.state.InvestmentEntryFormState,
    investments: List<io.poupai.app.domain.model.Investment>,
    fieldColors: TextFieldColors,
    onToggleNewAsset: (Boolean) -> Unit,
    onInvestmentSelected: (String, String) -> Unit,
    onNewAssetNameChanged: (String) -> Unit,
    onNewAssetTypeChanged: (InvestmentType) -> Unit,
    onTypeChanged: (EntryType) -> Unit,
    onSharesChanged: (String) -> Unit,
    onSharePriceChanged: (String) -> Unit,
    onNewCurrentValueChanged: (String) -> Unit,
    onNotesChanged: (String) -> Unit,
    onDateChanged: (String) -> Unit,
    onSave: () -> Unit,
) {
    var showInvestmentPicker by remember { mutableStateOf(false) }
    val fe = formState.fieldErrors

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {

        Text("Novo Lançamento", fontSize = 20.sp, fontWeight = FontWeight.Bold)

        // ─── Toggle ativo existente / novo ativo ───
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf(false to "Ativo existente", true to "Novo ativo").forEach { (isNew, label) ->
                FilterChip(selected = formState.isNewAsset == isNew, onClick = { onToggleNewAsset(isNew) },
                    label = { Text(label, fontSize = 12.sp) },
                    modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Purple40.copy(alpha = 0.12f), selectedLabelColor = Purple40))
            }
        }

        if (formState.isNewAsset) {
            TextField(value = formState.newAssetName, onValueChange = onNewAssetNameChanged,
                label = { Text("Nome do ativo") }, placeholder = { Text("Ex: PETR4, Tesouro IPCA+...") },
                singleLine = true, modifier = Modifier.fillMaxWidth(), colors = fieldColors,
                isError = fe.containsKey("newAssetName"),
                supportingText = fe["newAssetName"]?.let { { Text(it, color = MaterialTheme.colorScheme.error) } })

            Text("Categoria", style = MaterialTheme.typography.labelMedium, color = PoupaiTheme.tokens.textMuted)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(InvestmentType.RENDA_VARIAVEL to "Renda Variável",
                    InvestmentType.RENDA_FIXA to "Renda Fixa",
                    InvestmentType.CRIPTOMOEDAS to "Cripto").forEach { (type, label) ->
                    val typeColor = when (type) {
                        InvestmentType.RENDA_VARIAVEL -> Purple40
                        InvestmentType.RENDA_FIXA -> Purple60
                        InvestmentType.CRIPTOMOEDAS -> Color(0xFF7C5295)
                    }
                    FilterChip(selected = formState.newAssetType == type, onClick = { onNewAssetTypeChanged(type) },
                        label = { Text(label, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = typeColor.copy(alpha = 0.15f), selectedLabelColor = typeColor))
                }
            }
        } else {
            OutlinedCard(modifier = Modifier.fillMaxWidth().clickable { showInvestmentPicker = !showInvestmentPicker },
                shape = RoundedCornerShape(12.dp),
                colors = if (fe.containsKey("formInvestmentId"))
                    CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.08f))
                else CardDefaults.cardColors(containerColor = PoupaiTheme.tokens.surface)) {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Ativo", fontSize = 11.sp, color = PoupaiTheme.tokens.textMuted)
                        Text(if (formState.formInvestmentName.isNotBlank()) formState.formInvestmentName else "Selecione o ativo",
                            fontSize = 14.sp,
                            color = if (formState.formInvestmentName.isNotBlank()) PoupaiTheme.tokens.textPrimary else PoupaiTheme.tokens.textMuted)
                    }
                }
            }
            fe["formInvestmentId"]?.let {
                Text(it, color = MaterialTheme.colorScheme.error, fontSize = 11.sp,
                    modifier = Modifier.padding(start = 4.dp))
            }
            if (showInvestmentPicker) {
                AlertDialog(
                    onDismissRequest = { showInvestmentPicker = false },
                    title = { Text("Selecionar ativo", fontWeight = FontWeight.SemiBold) },
                    text = {
                        if (investments.isEmpty()) {
                            Text("Nenhum ativo cadastrado. Use 'Novo ativo'.",
                                fontSize = 13.sp, color = PoupaiTheme.tokens.textMuted)
                        } else {
                            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                                items(investments) { inv ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                onInvestmentSelected(inv.id, inv.name)
                                                showInvestmentPicker = false
                                            }
                                            .padding(vertical = 12.dp, horizontal = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(inv.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                                                color = PoupaiTheme.tokens.textPrimary)
                                            Text(when (inv.type) {
                                                InvestmentType.RENDA_VARIAVEL -> "Renda Variável"
                                                InvestmentType.RENDA_FIXA -> "Renda Fixa"
                                                InvestmentType.CRIPTOMOEDAS -> "Criptomoedas"
                                            }, fontSize = 11.sp, color = PoupaiTheme.tokens.textMuted)
                                        }
                                        if (inv.averagePrice > 0)
                                            Text("PM: ${inv.averagePrice.toBRL()}", fontSize = 11.sp,
                                                color = Purple40, fontWeight = FontWeight.SemiBold)
                                    }
                                    HorizontalDivider(color = PoupaiTheme.tokens.divider)
                                }
                            }
                        }
                    },
                    confirmButton = {},
                    dismissButton = {
                        TextButton(onClick = { showInvestmentPicker = false }) {
                            Text("Cancelar")
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                )
            }
        }

        HorizontalDivider(color = PoupaiTheme.tokens.divider)

        // ─── Tipo de lançamento ───
        Text("Tipo", style = MaterialTheme.typography.labelMedium, color = PoupaiTheme.tokens.textMuted)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(
                EntryType.APORTE to "Aporte",
                EntryType.RESGATE to "Resgate",
                EntryType.ATUALIZACAO_VALOR to "Atualização",
            ).forEach { (type, label) ->
                val color = when (type) {
                    EntryType.APORTE -> Purple40
                    EntryType.RESGATE -> Color(0xFF7C5295)
                    EntryType.ATUALIZACAO_VALOR -> Purple60
                }
                FilterChip(selected = formState.formType == type, onClick = { onTypeChanged(type) },
                    label = { Text(label, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = color.copy(alpha = 0.12f), selectedLabelColor = color))
            }
        }

        // ─── Campos condicionais por tipo ───
        when (formState.formType) {
            EntryType.APORTE, EntryType.RESGATE -> {
                // Para RESGATE com ativo existente, mostra posição atual antes dos campos.
                if (formState.formType == EntryType.RESGATE && !formState.isNewAsset) {
                    val selectedInvestment = investments.find { it.id == formState.formInvestmentId }
                    if (selectedInvestment != null && selectedInvestment.shares > 0) {
                        CurrentPositionCard(selectedInvestment)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextField(value = formState.formShares, onValueChange = onSharesChanged,
                        label = { Text("Qtd cotas") }, placeholder = { Text("0") },
                        singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f), colors = fieldColors,
                        isError = fe.containsKey("formShares"),
                        supportingText = fe["formShares"]?.let { { Text(it, color = MaterialTheme.colorScheme.error) } })
                    TextField(value = formState.formSharePrice, onValueChange = onSharePriceChanged,
                        label = { Text("Preço/cota (R$)") }, placeholder = { Text("0,00") },
                        singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f), colors = fieldColors,
                        isError = fe.containsKey("formSharePrice"),
                        supportingText = fe["formSharePrice"]?.let { { Text(it, color = MaterialTheme.colorScheme.error) } })
                }
                if (formState.formTotalValue > 0) {
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Purple40.copy(alpha = 0.08f)),
                        elevation = CardDefaults.cardElevation(0.dp)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total da operação", fontSize = 12.sp, color = Purple40)
                            Text(formState.formTotalValue.toBRL(), fontSize = 12.sp, color = Purple40, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            EntryType.ATUALIZACAO_VALOR -> {
                val selectedInvestment = investments.find { it.id == formState.formInvestmentId }
                val hasShares = (selectedInvestment?.shares ?: 0.0) > 0
                TextField(value = formState.formNewCurrentValue, onValueChange = onNewCurrentValueChanged,
                    label = { Text(if (hasShares) "Preço atual por cota (R$)" else "Novo valor total da posição (R$)") },
                    placeholder = { Text(if (hasShares) "Ex: 28,50" else "Ex: 10.800,00") },
                    singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(), colors = fieldColors,
                    isError = fe.containsKey("formNewCurrentValue"),
                    supportingText = fe["formNewCurrentValue"]?.let { { Text(it, color = MaterialTheme.colorScheme.error) } })
            }
        }

        TextField(value = formState.formDate, onValueChange = onDateChanged,
            label = { Text("Data (dd-mm-aaaa)") }, placeholder = { Text("18-05-2026") },
            singleLine = true, modifier = Modifier.fillMaxWidth(), colors = fieldColors,
            isError = fe.containsKey("formDate"),
            supportingText = fe["formDate"]?.let { { Text(it, color = MaterialTheme.colorScheme.error) } })

        TextField(value = formState.formNotes, onValueChange = onNotesChanged,
            label = { Text("Observação (opcional)") }, placeholder = { Text("Ex: Migração de posição...") },
            singleLine = true, modifier = Modifier.fillMaxWidth(), colors = fieldColors)

        formState.generalError?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp) }

        Button(onClick = onSave, enabled = !formState.isSaving,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Purple40)) {
            if (formState.isSaving) CircularProgressIndicator(Modifier.size(24.dp), color = Color.White)
            else Text("Registrar lançamento", fontSize = 16.sp, color = Color.White)
        }
    }
}
