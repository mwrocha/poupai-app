package io.poupai.app.features.budget.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import io.poupai.app.core.theme.PoupaiTheme
import io.poupai.app.core.theme.Purple40
import io.poupai.app.core.theme.PurpleDark
import io.poupai.app.core.theme.PurpleLight
import io.poupai.app.core.theme.RedNegative
import io.poupai.app.core.util.toBRL
import io.poupai.app.features.budget.state.BudgetCategory
import io.poupai.app.features.budget.viewmodel.BudgetViewModel
import io.poupai.app.features.transactions.state.EXPENSE_CATEGORIES

private const val HIDDEN = "••••"
private val AmberAlert = Color(0xFFC98A00)

private fun stateColor(item: BudgetCategory): Color = when {
    item.isOver -> RedNegative
    item.isNear -> AmberAlert
    else -> Purple40
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    onNavigateBack: () -> Unit,
    viewModel: BudgetViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showCategoryPicker by remember { mutableStateOf(false) }

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
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar", tint = Color.White)
                }
                Spacer(Modifier.width(4.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Orçamento", style = MaterialTheme.typography.titleLarge,
                        color = Color.White, fontWeight = FontWeight.Bold)
                    if (uiState.monthLabel.isNotBlank()) {
                        Text(uiState.monthLabel, fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
                    }
                }
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
                modifier = Modifier.fillMaxSize(),
            ) {
                item {
                    BudgetHeroCard(
                        totalSpent = uiState.totalSpent,
                        totalBudget = uiState.totalBudget,
                        hideValues = uiState.hideValues,
                    )
                }

                item {
                    Text(
                        "Por categoria",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = PoupaiTheme.tokens.textSecondary,
                    )
                }

                if (uiState.items.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = PoupaiTheme.tokens.surface),
                            elevation = CardDefaults.cardElevation(1.dp),
                        ) {
                            Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    "Sem gastos nem tetos neste mês.",
                                    fontSize = 12.sp,
                                    color = PoupaiTheme.tokens.textMuted,
                                )
                            }
                        }
                    }
                } else {
                    items(uiState.items, key = { it.category }) { item ->
                        BudgetRow(
                            item = item,
                            hideValues = uiState.hideValues,
                            onEdit = { viewModel.onShowSheet(item.category) },
                            onRemove = { viewModel.onRemoveLimit(item.category) },
                        )
                    }
                }

                item {
                    OutlinedButton(
                        onClick = { showCategoryPicker = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp), tint = Purple40)
                        Spacer(Modifier.width(8.dp))
                        Text("Definir orçamento de categoria", color = Purple40)
                    }
                }

                item { Spacer(Modifier.height(40.dp)) }
            }
        }
    }

    if (showCategoryPicker) {
        CategoryPickerDialog(
            alreadyBudgeted = uiState.items.filter { it.hasLimit }.map { it.category }.toSet(),
            onPick = { category ->
                showCategoryPicker = false
                viewModel.onShowSheet(category)
            },
            onDismiss = { showCategoryPicker = false },
        )
    }

    if (uiState.showSheet) {
        ModalBottomSheet(onDismissRequest = viewModel::onDismissSheet, sheetState = sheetState) {
            SetLimitForm(
                category = uiState.editingCategory,
                limitInput = uiState.limitInput,
                onLimitChanged = viewModel::onLimitInputChanged,
                onSave = viewModel::onSaveLimit,
            )
        }
    }
}

// ─── HERO ───

@Composable
private fun BudgetHeroCard(totalSpent: Double, totalBudget: Double, hideValues: Boolean) {
    val hasBudget = totalBudget > 0.0
    val progress = if (hasBudget) (totalSpent / totalBudget).toFloat().coerceIn(0f, 1f) else 0f
    val over = hasBudget && totalSpent > totalBudget
    val barColor = if (over) RedNegative else Color.White

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(4.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(listOf(PurpleDark, Purple40, Color(0xFF6B4396))),
                    RoundedCornerShape(20.dp),
                )
                .padding(20.dp),
        ) {
            Column {
                Text("Gasto do mês", fontSize = 11.sp, color = Color.White.copy(alpha = 0.65f))
                Spacer(Modifier.height(4.dp))
                Text(
                    if (hideValues) HIDDEN else totalSpent.toBRL(),
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
                if (hasBudget) {
                    Text(
                        if (hideValues) "de $HIDDEN orçados" else "de ${totalBudget.toBRL()} orçados",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.65f),
                    )
                    Spacer(Modifier.height(14.dp))
                    Box(
                        Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))
                            .background(Color.White.copy(alpha = 0.2f)),
                    ) {
                        Box(
                            Modifier.fillMaxWidth(progress).fillMaxHeight()
                                .clip(RoundedCornerShape(4.dp)).background(barColor),
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        if (over) "Estourou o orçamento em ${"%.0f".format((totalSpent / totalBudget - 1) * 100)}%"
                        else "${"%.0f".format(progress * 100)}% do orçamento usado",
                        fontSize = 11.sp,
                        color = if (over) Color(0xFFFFC4C4) else Color.White.copy(alpha = 0.8f),
                        fontWeight = FontWeight.SemiBold,
                    )
                } else {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Defina tetos por categoria para acompanhar.",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.65f),
                    )
                }
            }
        }
    }
}

// ─── ROW ───

@Composable
private fun BudgetRow(
    item: BudgetCategory,
    hideValues: Boolean,
    onEdit: () -> Unit,
    onRemove: () -> Unit,
) {
    val color = stateColor(item)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PoupaiTheme.tokens.surface),
        elevation = CardDefaults.cardElevation(1.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        item.category,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PoupaiTheme.tokens.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        if (item.hasLimit) {
                            if (hideValues) "$HIDDEN de $HIDDEN" else "${item.spent.toBRL()} de ${item.limit.toBRL()}"
                        } else {
                            if (hideValues) "Gasto: $HIDDEN · sem teto" else "Gasto: ${item.spent.toBRL()} · sem teto"
                        },
                        fontSize = 11.sp,
                        color = PoupaiTheme.tokens.textMuted,
                    )
                }
                if (item.hasLimit) {
                    Surface(shape = RoundedCornerShape(6.dp), color = color.copy(alpha = 0.12f)) {
                        Text(
                            if (item.isOver) "Estourou"
                            else if (hideValues) "${"%.0f".format(item.progress * 100)}%"
                            else "Resta ${item.remaining.toBRL()}",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = color,
                        )
                    }
                }
                IconButton(onClick = onEdit, modifier = Modifier.size(34.dp)) {
                    Icon(Icons.Default.Edit, "Definir teto", tint = PoupaiTheme.tokens.textMuted,
                        modifier = Modifier.size(17.dp))
                }
                if (item.hasLimit) {
                    IconButton(onClick = onRemove, modifier = Modifier.size(34.dp)) {
                        Icon(Icons.Default.Delete, "Remover teto", tint = PoupaiTheme.tokens.textMuted,
                            modifier = Modifier.size(17.dp))
                    }
                }
            }
            if (item.hasLimit) {
                Spacer(Modifier.height(10.dp))
                Box(
                    Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp))
                        .background(PoupaiTheme.tokens.surfaceSunken),
                ) {
                    Box(
                        Modifier.fillMaxWidth(item.progress).fillMaxHeight()
                            .clip(RoundedCornerShape(3.dp)).background(color),
                    )
                }
            }
        }
    }
}

// ─── CATEGORY PICKER ───

@Composable
private fun CategoryPickerDialog(
    alreadyBudgeted: Set<String>,
    onPick: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val available = EXPENSE_CATEGORIES.filter { it !in alreadyBudgeted }
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(16.dp),
        title = { Text("Escolha a categoria", fontWeight = FontWeight.SemiBold) },
        text = {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(available, key = { it }) { cat ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPick(cat) }
                            .padding(vertical = 12.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            Modifier.size(30.dp).clip(CircleShape).background(PurpleLight.copy(alpha = 0.55f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Default.PieChart, null, tint = Purple40, modifier = Modifier.size(15.dp))
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(cat, fontSize = 14.sp, color = PoupaiTheme.tokens.textPrimary)
                    }
                    HorizontalDivider(color = PoupaiTheme.tokens.surfaceAlt)
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Fechar") } },
    )
}

// ─── SET LIMIT FORM ───

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SetLimitForm(
    category: String,
    limitInput: String,
    onLimitChanged: (String) -> Unit,
    onSave: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text("Teto de $category", fontSize = 20.sp, fontWeight = FontWeight.Bold,
            color = PoupaiTheme.tokens.textPrimary)
        Text(
            "Defina o limite de gasto mensal para esta categoria. Deixe vazio (0) para remover.",
            fontSize = 12.sp,
            color = PoupaiTheme.tokens.textMuted,
        )
        OutlinedTextField(
            value = limitInput,
            onValueChange = onLimitChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Teto mensal (R$)") },
            placeholder = { Text("0,00") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Purple40,
                unfocusedBorderColor = PoupaiTheme.tokens.divider,
                focusedLabelColor = Purple40,
                unfocusedLabelColor = PoupaiTheme.tokens.textMuted,
                focusedTextColor = PoupaiTheme.tokens.textPrimary,
                unfocusedTextColor = PoupaiTheme.tokens.textPrimary,
                cursorColor = Purple40,
            ),
        )
        Button(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Purple40),
        ) {
            Text("Salvar", fontSize = 16.sp, color = Color.White)
        }
    }
}
