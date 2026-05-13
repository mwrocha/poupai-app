package io.poupai.app.features.investmentbook.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import io.poupai.app.core.theme.GreenPositive
import io.poupai.app.core.theme.Purple40
import io.poupai.app.core.theme.PurpleDark
import io.poupai.app.core.theme.RedNegative
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
        focusedIndicatorColor = Purple40, unfocusedIndicatorColor = Color(0xFFBDBDBD),
        focusedLabelColor = Purple40, unfocusedLabelColor = Color(0xFF9E9E9E),
        focusedTextColor = Color(0xFF1C1B1F), unfocusedTextColor = Color(0xFF1C1B1F), cursorColor = Purple40,
    )

    if (uiState.showDeleteDialog && uiState.deletingEntry != null) {
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

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F7))) {

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
                item { BookSummaryCard(uiState = uiState) }
                item { BookFilters(uiState = uiState, viewModel = viewModel) }

                if (uiState.entries.isEmpty()) {
                    item {
                        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(1.dp)) {
                            Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("📒", fontSize = 40.sp)
                                    Spacer(Modifier.height(12.dp))
                                    Text("Nenhum lançamento encontrado",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFF9E9E9E), textAlign = TextAlign.Center)
                                    Text("Toque em + para registrar", fontSize = 12.sp, color = Color(0xFFBDBDBD))
                                }
                            }
                        }
                    }
                } else {
                    item {
                        Text("${uiState.totalEntries} lançamento${if (uiState.totalEntries != 1L) "s" else ""}",
                            fontSize = 12.sp, color = Color(0xFF9E9E9E), modifier = Modifier.padding(horizontal = 4.dp))
                    }
                    item {
                        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(1.dp)) {
                            Column {
                                uiState.entries.forEachIndexed { index, entry ->
                                    EntryRow(entry = entry, onDelete = { viewModel.onDeleteRequest(entry) })
                                    if (index < uiState.entries.lastIndex)
                                        HorizontalDivider(color = Color(0xFFF5F5F5),
                                            modifier = Modifier.padding(horizontal = 16.dp))
                                }
                            }
                        }
                    }
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomEnd) {
        FloatingActionButton(onClick = viewModel::onShowAddSheet, containerColor = Purple40,
            shape = CircleShape, modifier = Modifier.padding(24.dp)) {
            Icon(Icons.Default.Add, "Novo lançamento", tint = Color.White)
        }
    }

    if (uiState.showAddSheet) {
        ModalBottomSheet(onDismissRequest = viewModel::onDismissSheet, sheetState = sheetState) {
            AddEntryForm(
                uiState = uiState, fieldColors = fieldColors,
                onToggleNewAsset = viewModel::onToggleNewAsset,
                onInvestmentSelected = viewModel::onFormInvestmentSelected,
                onNewAssetNameChanged = viewModel::onNewAssetNameChanged,
                onNewAssetTypeChanged = viewModel::onNewAssetTypeChanged,
                onTypeChanged = viewModel::onFormTypeChanged,
                onSharesChanged = viewModel::onFormSharesChanged,
                onSharePriceChanged = viewModel::onFormSharePriceChanged,
                onNewCurrentValueChanged = viewModel::onFormNewCurrentValueChanged,
                onAdjustedSharesChanged = viewModel::onFormAdjustedSharesChanged,
                onAdjustedAvgPriceChanged = viewModel::onFormAdjustedAvgPriceChanged,
                onNotesChanged = viewModel::onFormNotesChanged,
                onDateChanged = viewModel::onFormDateChanged,
                onSave = viewModel::onSaveEntry,
            )
        }
    }
}

// ─── CARD RESUMO ───

@Composable
private fun BookSummaryCard(uiState: InvestmentBookUiState) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(4.dp)) {
        Box(modifier = Modifier.fillMaxWidth()
            .background(brush = Brush.linearGradient(listOf(PurpleDark, Purple40)), shape = RoundedCornerShape(20.dp))
            .padding(20.dp)) {
            Column {
                Text("Movimentações", fontSize = 12.sp, color = Color.White.copy(alpha = 0.75f))
                Text("${uiState.totalEntries} lançamento${if (uiState.totalEntries != 1L) "s" else ""}",
                    style = MaterialTheme.typography.headlineSmall, color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Total aportado", fontSize = 11.sp, color = Color.White.copy(alpha = 0.65f))
                        Text(uiState.totalAported.toBRL(), fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Total resgatado", fontSize = 11.sp, color = Color.White.copy(alpha = 0.65f))
                        Text(uiState.totalRescued.toBRL(), fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

// ─── FILTROS ───

@Composable
private fun BookFilters(uiState: InvestmentBookUiState, viewModel: InvestmentBookViewModel) {
    var showInvestmentPicker by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.FilterList, null, tint = Color(0xFF9E9E9E), modifier = Modifier.size(16.dp))
            Text("Filtrar:", fontSize = 12.sp, color = Color(0xFF9E9E9E))
            if (uiState.selectedInvestmentName != null) {
                FilterChip(selected = true, onClick = { viewModel.onFilterInvestment(null, null) },
                    label = { Text(uiState.selectedInvestmentName, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Purple40.copy(alpha = 0.12f), selectedLabelColor = Purple40))
            } else {
                FilterChip(selected = false, onClick = { showInvestmentPicker = !showInvestmentPicker },
                    label = { Text("Ativo", fontSize = 11.sp) })
            }
            if (uiState.selectedInvestmentId != null || uiState.selectedMonth != null) {
                TextButton(onClick = viewModel::onClearFilters, contentPadding = PaddingValues(horizontal = 8.dp)) {
                    Text("Limpar", fontSize = 11.sp, color = RedNegative)
                }
            }
        }
        if (showInvestmentPicker && uiState.investments.isNotEmpty()) {
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)) {
                Column {
                    uiState.investments.forEach { inv ->
                        Row(modifier = Modifier.fillMaxWidth()
                            .clickable { viewModel.onFilterInvestment(inv.id, inv.name); showInvestmentPicker = false }
                            .padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(inv.name, fontSize = 13.sp, modifier = Modifier.weight(1f))
                            Text(inv.type.name, fontSize = 10.sp, color = Color(0xFF9E9E9E))
                        }
                        HorizontalDivider(color = Color(0xFFF5F5F5))
                    }
                }
            }
        }
    }
}

// ─── LINHA DE LANÇAMENTO ───

@Composable
private fun EntryRow(entry: InvestmentEntry, onDelete: () -> Unit) {
    val (color, emoji) = when (entry.type) {
        EntryType.APORTE -> GreenPositive to "📥"
        EntryType.RESGATE -> RedNegative to "📤"
        EntryType.ATUALIZACAO_VALOR -> Purple40 to "📊"
        EntryType.AJUSTE_POSICAO -> Color(0xFFFF9800) to "⚖️"
    }
    val typeLabel = when (entry.type) {
        EntryType.APORTE -> "Aporte"
        EntryType.RESGATE -> "Resgate"
        EntryType.ATUALIZACAO_VALOR -> "Atualização"
        EntryType.AJUSTE_POSICAO -> "Ajuste"
    }

    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(color.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center) {
            Text(emoji, fontSize = 16.sp)
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(entry.investmentName, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1C1B1F), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(4.dp), color = color.copy(alpha = 0.10f)) {
                    Text(typeLabel, fontSize = 9.sp, color = color, fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                }
                Text(entry.date, fontSize = 11.sp, color = Color(0xFF9E9E9E))
            }
            when (entry.type) {
                EntryType.APORTE, EntryType.RESGATE -> {
                    if ((entry.shares ?: 0.0) > 0)
                        Text("${String.format("%.2f", entry.shares)} cotas × ${entry.sharePrice?.toBRL() ?: "—"}",
                            fontSize = 10.sp, color = Color(0xFF9E9E9E))
                    if (entry.type == EntryType.APORTE && (entry.newAveragePrice ?: 0.0) > 0)
                        Text("Novo PM: ${entry.newAveragePrice?.toBRL()}", fontSize = 10.sp,
                            color = color, fontWeight = FontWeight.SemiBold)
                }
                EntryType.AJUSTE_POSICAO -> {
                    Text("${String.format("%.2f", entry.adjustedShares ?: 0.0)} cotas — PM ${entry.adjustedAveragePrice?.toBRL() ?: "—"}",
                        fontSize = 10.sp, color = color, fontWeight = FontWeight.SemiBold)
                }
                else -> Unit
            }
            entry.notes?.let {
                Text(it, fontSize = 10.sp, color = Color(0xFF9E9E9E), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            entry.totalValue?.let {
                Text(it.toBRL(), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = color)
            }
        }
        Spacer(Modifier.width(4.dp))
        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.Delete, "Excluir", tint = Color(0xFFBDBDBD), modifier = Modifier.size(16.dp))
        }
    }
}

// ─── FORMULÁRIO ───

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEntryForm(
    uiState: InvestmentBookUiState,
    fieldColors: TextFieldColors,
    onToggleNewAsset: (Boolean) -> Unit,
    onInvestmentSelected: (String, String) -> Unit,
    onNewAssetNameChanged: (String) -> Unit,
    onNewAssetTypeChanged: (InvestmentType) -> Unit,
    onTypeChanged: (EntryType) -> Unit,
    onSharesChanged: (String) -> Unit,
    onSharePriceChanged: (String) -> Unit,
    onNewCurrentValueChanged: (String) -> Unit,
    onAdjustedSharesChanged: (String) -> Unit,
    onAdjustedAvgPriceChanged: (String) -> Unit,
    onNotesChanged: (String) -> Unit,
    onDateChanged: (String) -> Unit,
    onSave: () -> Unit,
) {
    var showInvestmentPicker by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {

        Text("Novo Lançamento", fontSize = 20.sp, fontWeight = FontWeight.Bold)

        // ─── Toggle ativo existente / novo ativo ───
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf(false to "Ativo existente", true to "Novo ativo").forEach { (isNew, label) ->
                FilterChip(selected = uiState.isNewAsset == isNew, onClick = { onToggleNewAsset(isNew) },
                    label = { Text(label, fontSize = 12.sp) },
                    modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Purple40.copy(alpha = 0.12f), selectedLabelColor = Purple40))
            }
        }

        if (uiState.isNewAsset) {
            TextField(value = uiState.newAssetName, onValueChange = onNewAssetNameChanged,
                label = { Text("Nome do ativo") }, placeholder = { Text("Ex: PETR4, Tesouro IPCA+...") },
                singleLine = true, modifier = Modifier.fillMaxWidth(), colors = fieldColors)

            Text("Categoria", style = MaterialTheme.typography.labelMedium, color = Color(0xFF9E9E9E))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(InvestmentType.RENDA_VARIAVEL to "Renda Variável",
                    InvestmentType.RENDA_FIXA to "Renda Fixa",
                    InvestmentType.CRIPTOMOEDAS to "Cripto").forEach { (type, label) ->
                    val typeColor = when (type) {
                        InvestmentType.RENDA_VARIAVEL -> Color(0xFF503173)
                        InvestmentType.RENDA_FIXA -> Color(0xFF4CAF50)
                        InvestmentType.CRIPTOMOEDAS -> Color(0xFFFF9800)
                    }
                    FilterChip(selected = uiState.newAssetType == type, onClick = { onNewAssetTypeChanged(type) },
                        label = { Text(label, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = typeColor.copy(alpha = 0.15f), selectedLabelColor = typeColor))
                }
            }
        } else {
            OutlinedCard(modifier = Modifier.fillMaxWidth().clickable { showInvestmentPicker = !showInvestmentPicker },
                shape = RoundedCornerShape(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Ativo", fontSize = 11.sp, color = Color(0xFF9E9E9E))
                        Text(if (uiState.formInvestmentName.isNotBlank()) uiState.formInvestmentName else "Selecione o ativo",
                            fontSize = 14.sp,
                            color = if (uiState.formInvestmentName.isNotBlank()) Color(0xFF1C1B1F) else Color(0xFFBDBDBD))
                    }
                }
            }
            if (showInvestmentPicker) {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)) {
                    Column {
                        if (uiState.investments.isEmpty()) {
                            Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                Text("Nenhum ativo. Use 'Novo ativo'.", fontSize = 12.sp,
                                    color = Color(0xFF9E9E9E), textAlign = TextAlign.Center)
                            }
                        } else {
                            uiState.investments.forEach { inv ->
                                Row(modifier = Modifier.fillMaxWidth()
                                    .clickable { onInvestmentSelected(inv.id, inv.name); showInvestmentPicker = false }
                                    .padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(inv.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                        Text(when (inv.type) {
                                            InvestmentType.RENDA_VARIAVEL -> "Renda Variável"
                                            InvestmentType.RENDA_FIXA -> "Renda Fixa"
                                            InvestmentType.CRIPTOMOEDAS -> "Criptomoedas"
                                        }, fontSize = 11.sp, color = Color(0xFF9E9E9E))
                                    }
                                    if (inv.averagePrice > 0)
                                        Text("PM: ${inv.averagePrice.toBRL()}", fontSize = 11.sp, color = Purple40)
                                }
                                HorizontalDivider(color = Color(0xFFF5F5F5))
                            }
                        }
                    }
                }
            }
        }

        HorizontalDivider(color = Color(0xFFF0F0F0))

        // ─── Tipo de lançamento ───
        Text("Tipo", style = MaterialTheme.typography.labelMedium, color = Color(0xFF9E9E9E))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(EntryType.APORTE to "Aporte", EntryType.RESGATE to "Resgate",
                EntryType.ATUALIZACAO_VALOR to "Atualização", EntryType.AJUSTE_POSICAO to "Ajuste").forEach { (type, label) ->
                val color = when (type) {
                    EntryType.APORTE -> GreenPositive
                    EntryType.RESGATE -> RedNegative
                    EntryType.ATUALIZACAO_VALOR -> Purple40
                    EntryType.AJUSTE_POSICAO -> Color(0xFFFF9800)
                }
                FilterChip(selected = uiState.formType == type, onClick = { onTypeChanged(type) },
                    label = { Text(label, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = color.copy(alpha = 0.12f), selectedLabelColor = color))
            }
        }

        // ─── Campos condicionais por tipo ───
        when (uiState.formType) {
            EntryType.APORTE, EntryType.RESGATE -> {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextField(value = uiState.formShares, onValueChange = onSharesChanged,
                        label = { Text("Qtd cotas") }, placeholder = { Text("0") },
                        singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f), colors = fieldColors)
                    TextField(value = uiState.formSharePrice, onValueChange = onSharePriceChanged,
                        label = { Text("Preço/cota (R$)") }, placeholder = { Text("0,00") },
                        singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f), colors = fieldColors)
                }
                if (uiState.formTotalValue > 0) {
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Purple40.copy(alpha = 0.08f)),
                        elevation = CardDefaults.cardElevation(0.dp)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total da operação", fontSize = 12.sp, color = Purple40)
                            Text(uiState.formTotalValue.toBRL(), fontSize = 12.sp, color = Purple40, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            EntryType.ATUALIZACAO_VALOR -> {
                TextField(value = uiState.formNewCurrentValue, onValueChange = onNewCurrentValueChanged,
                    label = { Text("Novo valor atual (R$)") }, placeholder = { Text("0,00") },
                    singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(), colors = fieldColors)
            }
            EntryType.AJUSTE_POSICAO -> {
                // ─── Card explicativo ───
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFF9800).copy(alpha = 0.08f)),
                    elevation = CardDefaults.cardElevation(0.dp)) {
                    Text(
                        "Use para migrar ativos existentes. Informe a posição atual (cotas e PM) " +
                                "e os próximos aportes recalcularão o PM automaticamente.",
                        fontSize = 11.sp, color = Color(0xFFE65100),
                        modifier = Modifier.padding(12.dp), lineHeight = 16.sp,
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextField(value = uiState.formAdjustedShares, onValueChange = onAdjustedSharesChanged,
                        label = { Text("Qtd cotas atual") }, placeholder = { Text("0") },
                        singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f), colors = fieldColors)
                    TextField(value = uiState.formAdjustedAveragePrice, onValueChange = onAdjustedAvgPriceChanged,
                        label = { Text("PM atual (R$)") }, placeholder = { Text("0,00") },
                        singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f), colors = fieldColors)
                }
                if (uiState.formAdjustedTotalValue > 0) {
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFF9800).copy(alpha = 0.08f)),
                        elevation = CardDefaults.cardElevation(0.dp)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Posição total", fontSize = 12.sp, color = Color(0xFFE65100))
                            Text(uiState.formAdjustedTotalValue.toBRL(), fontSize = 12.sp,
                                color = Color(0xFFE65100), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        TextField(value = uiState.formDate, onValueChange = onDateChanged,
            label = { Text("Data (yyyy-MM-dd)") }, placeholder = { Text("2026-05-13") },
            singleLine = true, modifier = Modifier.fillMaxWidth(), colors = fieldColors)

        TextField(value = uiState.formNotes, onValueChange = onNotesChanged,
            label = { Text("Observação (opcional)") }, placeholder = { Text("Ex: Migração de posição...") },
            singleLine = true, modifier = Modifier.fillMaxWidth(), colors = fieldColors)

        uiState.formError?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp) }

        Button(onClick = onSave, enabled = !uiState.isSaving,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Purple40)) {
            if (uiState.isSaving) CircularProgressIndicator(Modifier.size(24.dp), color = Color.White)
            else Text("Registrar lançamento", fontSize = 16.sp, color = Color.White)
        }
    }
}