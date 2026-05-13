package io.poupai.app.features.dividends.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
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
import io.poupai.app.core.util.toBRL
import io.poupai.app.domain.model.Dividend
import io.poupai.app.domain.model.DividendType
import io.poupai.app.features.dividends.state.DividendsUiState
import io.poupai.app.features.dividends.viewmodel.DividendsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DividendsScreen(
    onNavigateBack: () -> Unit,
    viewModel: DividendsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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

    if (uiState.showDeleteDialog && uiState.deletingDividend != null) {
        AlertDialog(
            onDismissRequest = viewModel::onDeleteCancel,
            title = { Text("Excluir dividendo") },
            text = { Text("Deseja excluir este registro de ${uiState.deletingDividend!!.amount.toBRL()} de ${uiState.deletingDividend!!.investmentName}?") },
            confirmButton = {
                Button(onClick = viewModel::onDeleteConfirm,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Excluir") }
            },
            dismissButton = { TextButton(onClick = viewModel::onDeleteCancel) { Text("Cancelar") } },
        )
    }

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F7)),
    ) {
        // ─── Header ───
        Box(
            modifier = Modifier.fillMaxWidth()
                .background(brush = Brush.verticalGradient(colors = listOf(PurpleDark, Purple40)))
                .padding(horizontal = 20.dp).padding(top = 16.dp, bottom = 16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, "Voltar", tint = Color.White)
                }
                Spacer(Modifier.weight(1f))
                Text("Dividendos", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
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
                // ─── Card de resumo ───
                item { DividendSummaryCard(uiState = uiState) }

                if (uiState.dividends.isEmpty()) {
                    item {
                        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(1.dp)) {
                            Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("💰", fontSize = 40.sp)
                                    Spacer(Modifier.height(12.dp))
                                    Text("Nenhum dividendo registrado", style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFF9E9E9E), textAlign = TextAlign.Center)
                                    Text("Toque em + para registrar", fontSize = 12.sp, color = Color(0xFFBDBDBD))
                                }
                            }
                        }
                    }
                } else {
                    item {
                        Text("Histórico", style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold, color = Color(0xFF6B6B6B))
                    }
                    item {
                        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(1.dp)) {
                            Column {
                                uiState.dividends.forEachIndexed { index, dividend ->
                                    DividendRow(dividend = dividend, onDelete = { viewModel.onDeleteRequest(dividend) })
                                    if (index < uiState.dividends.lastIndex) {
                                        HorizontalDivider(color = Color(0xFFF5F5F5), modifier = Modifier.padding(horizontal = 16.dp))
                                    }
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
        FloatingActionButton(onClick = viewModel::onShowAddSheet, containerColor = GreenPositive,
            shape = CircleShape, modifier = Modifier.padding(24.dp)) {
            Icon(Icons.Default.Add, "Registrar dividendo", tint = Color.White)
        }
    }

    if (uiState.showAddSheet) {
        ModalBottomSheet(onDismissRequest = viewModel::onDismissSheet, sheetState = sheetState) {
            AddDividendForm(uiState = uiState, fieldColors = fieldColors,
                onInvestmentSelected = viewModel::onFormInvestmentSelected,
                onAmountChanged = viewModel::onFormAmountChanged,
                onTypeChanged = viewModel::onFormTypeChanged,
                onDateChanged = viewModel::onFormDateChanged,
                onSave = viewModel::onSaveDividend)
        }
    }
}

@Composable
private fun DividendSummaryCard(uiState: DividendsUiState) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), elevation = CardDefaults.cardElevation(4.dp)) {
        Box(modifier = Modifier.fillMaxWidth()
            .background(brush = Brush.linearGradient(listOf(Color(0xFF2E7D32), GreenPositive)), shape = RoundedCornerShape(20.dp))
            .padding(20.dp)) {
            Column {
                Text("Total recebido", fontSize = 12.sp, color = Color.White.copy(alpha = 0.75f))
                Spacer(Modifier.height(4.dp))
                Text(uiState.totalReceived.toBRL(), style = MaterialTheme.typography.headlineMedium,
                    color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    DividendStatColumn("Este mês", uiState.totalReceivedThisMonth.toBRL())
                    DividendStatColumn("Este ano", uiState.totalReceivedThisYear.toBRL())
                    DividendStatColumn("Projeção anual", uiState.projectedAnnual.toBRL())
                }
            }
        }
    }
}

@Composable
private fun DividendStatColumn(label: String, value: String) {
    Column {
        Text(label, fontSize = 11.sp, color = Color.White.copy(alpha = 0.65f))
        Text(value, fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun DividendRow(dividend: Dividend, onDelete: () -> Unit) {
    val typeColor = when (dividend.type) {
        DividendType.DIVIDENDO -> Color(0xFF2E7D32)
        DividendType.JCP -> Color(0xFF1565C0)
        DividendType.RENDIMENTO -> Color(0xFF6A1B9A)
        DividendType.AMORTIZACAO -> Color(0xFFE65100)
        DividendType.OUTROS -> Color(0xFF546E7A)
    }

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(typeColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center) {
            Text("💰", fontSize = 16.sp)
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(dividend.investmentName, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1C1B1F), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(4.dp), color = typeColor.copy(alpha = 0.10f)) {
                    Text(dividend.type.name, fontSize = 9.sp, color = typeColor, fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                }
                Spacer(Modifier.width(6.dp))
                Text(dividend.date, fontSize = 11.sp, color = Color(0xFF9E9E9E))
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(dividend.amount.toBRL(), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = GreenPositive)
            Text("yield ${String.format("%.2f", dividend.yieldPercent)}%", fontSize = 10.sp, color = Color(0xFF9E9E9E))
        }
        Spacer(Modifier.width(4.dp))
        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.Delete, "Excluir", tint = Color(0xFFBDBDBD), modifier = Modifier.size(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddDividendForm(
    uiState: DividendsUiState,
    fieldColors: TextFieldColors,
    onInvestmentSelected: (String, String) -> Unit,
    onAmountChanged: (String) -> Unit,
    onTypeChanged: (DividendType) -> Unit,
    onDateChanged: (String) -> Unit,
    onSave: () -> Unit,
) {
    var showInvestmentPicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Registrar Dividendo", fontSize = 20.sp, fontWeight = FontWeight.Bold)

        // Seletor de ativo
        OutlinedCard(
            modifier = Modifier.fillMaxWidth().clickable { showInvestmentPicker = !showInvestmentPicker },
            shape = RoundedCornerShape(12.dp),
        ) {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Ativo", fontSize = 11.sp, color = Color(0xFF9E9E9E))
                    Text(if (uiState.formInvestmentName.isNotBlank()) uiState.formInvestmentName else "Selecione o ativo",
                        fontSize = 14.sp, color = if (uiState.formInvestmentName.isNotBlank()) Color(0xFF1C1B1F) else Color(0xFFBDBDBD))
                }
            }
        }

        if (showInvestmentPicker) {
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)) {
                Column {
                    uiState.investments.forEach { inv ->
                        Row(modifier = Modifier.fillMaxWidth()
                            .clickable { onInvestmentSelected(inv.id, inv.name); showInvestmentPicker = false }
                            .padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(inv.name, fontSize = 14.sp, modifier = Modifier.weight(1f))
                            Text(inv.type.name, fontSize = 11.sp, color = Color(0xFF9E9E9E))
                        }
                        HorizontalDivider(color = Color(0xFFF5F5F5))
                    }
                }
            }
        }

        // Tipo de dividendo
        Text("Tipo", style = MaterialTheme.typography.labelMedium, color = Color(0xFF9E9E9E))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            DividendType.entries.take(3).forEach { type ->
                FilterChip(selected = uiState.formType == type, onClick = { onTypeChanged(type) },
                    label = { Text(type.name, fontSize = 10.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GreenPositive.copy(alpha = 0.12f),
                        selectedLabelColor = GreenPositive))
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            DividendType.entries.drop(3).forEach { type ->
                FilterChip(selected = uiState.formType == type, onClick = { onTypeChanged(type) },
                    label = { Text(type.name, fontSize = 10.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GreenPositive.copy(alpha = 0.12f),
                        selectedLabelColor = GreenPositive))
            }
        }

        TextField(value = uiState.formAmount, onValueChange = onAmountChanged,
            label = { Text("Valor recebido (R$)") }, placeholder = { Text("0,00") },
            singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(), colors = fieldColors)

        TextField(value = uiState.formDate, onValueChange = onDateChanged,
            label = { Text("Data (yyyy-MM-dd)") }, placeholder = { Text("2026-05-10") },
            singleLine = true, modifier = Modifier.fillMaxWidth(), colors = fieldColors)

        uiState.formError?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp) }

        Button(onClick = onSave, enabled = !uiState.isSaving,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GreenPositive)) {
            if (uiState.isSaving) CircularProgressIndicator(Modifier.size(24.dp), color = Color.White)
            else Text("Registrar dividendo", fontSize = 16.sp, color = Color.White)
        }
    }
}