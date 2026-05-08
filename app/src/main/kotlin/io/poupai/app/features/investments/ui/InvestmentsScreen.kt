package io.poupai.app.features.investments.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import io.poupai.app.core.theme.GreenPositive
import io.poupai.app.core.theme.Purple40
import io.poupai.app.core.theme.PurpleDark
import io.poupai.app.core.theme.RedNegative
import io.poupai.app.core.util.toBRL
import io.poupai.app.domain.model.Investment
import io.poupai.app.domain.model.InvestmentType
import io.poupai.app.core.designsystem.components.EyeToggleIcon
import io.poupai.app.features.investments.viewmodel.InvestmentsViewModel

private const val HIDDEN = "••••"

private val typeColor = mapOf(
    InvestmentType.RENDA_VARIAVEL to Color(0xFF503173),
    InvestmentType.RENDA_FIXA to Color(0xFF4CAF50),
    InvestmentType.CRIPTOMOEDAS to Color(0xFFFF9800),
)

private val typeLabel = mapOf(
    InvestmentType.RENDA_VARIAVEL to "Renda Variável",
    InvestmentType.RENDA_FIXA to "Renda Fixa",
    InvestmentType.CRIPTOMOEDAS to "Criptomoedas",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvestmentsScreen(
    onNavigateBack: () -> Unit,
    viewModel: InvestmentsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val allInvestments = uiState.rendaVariavel + uiState.rendaFixa + uiState.criptomoedas
    val totalInvested = allInvestments.sumOf { it.investedValue }
    val totalCurrent = allInvestments.sumOf { it.currentValue }
    val totalProfit = totalCurrent - totalInvested

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

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = viewModel::onShowAddSheet,
                containerColor = Purple40,
                shape = CircleShape,
            ) {
                Icon(Icons.Default.Add, "Adicionar investimento", tint = Color.White)
            }
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(brush = Brush.verticalGradient(colors = listOf(PurpleDark, Purple40)))
                    .padding(24.dp)
                    .padding(top = 16.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Voltar", tint = Color.White)
                    }
                    Spacer(Modifier.weight(1f))
                    Text(
                        "Investimentos",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.weight(1f))
                    EyeToggleIcon(hideValues = uiState.hideValues, onToggle = viewModel::toggleHideValues)
                }
            }

            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Purple40)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    item {
                        InvestmentSummaryCard(
                            totalInvested = totalInvested,
                            totalCurrent = totalCurrent,
                            totalProfit = totalProfit,
                            hideValues = uiState.hideValues,
                        )
                    }

                    if (allInvestments.isNotEmpty()) {
                        item {
                            AllocationDonutCard(
                                rendaVariavel = uiState.rendaVariavel.sumOf { it.currentValue },
                                rendaFixa = uiState.rendaFixa.sumOf { it.currentValue },
                                criptomoedas = uiState.criptomoedas.sumOf { it.currentValue },
                                total = totalCurrent,
                            )
                        }
                    }

                    item { InvestmentSection("Renda Variável", InvestmentType.RENDA_VARIAVEL, uiState.rendaVariavel, uiState.hideValues) }
                    item { InvestmentSection("Renda Fixa", InvestmentType.RENDA_FIXA, uiState.rendaFixa, uiState.hideValues) }
                    item { InvestmentSection("Criptomoedas", InvestmentType.CRIPTOMOEDAS, uiState.criptomoedas, uiState.hideValues) }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }

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
                Text("Novo Investimento", fontSize = 20.sp, fontWeight = FontWeight.Bold)

                Text("Tipo", style = MaterialTheme.typography.labelMedium, color = Color(0xFF9E9E9E))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    InvestmentType.entries.forEach { type ->
                        val selected = uiState.formType == type
                        val color = typeColor[type] ?: Purple40
                        FilterChip(
                            selected = selected,
                            onClick = { viewModel.onFormTypeChanged(type) },
                            label = {
                                Text(
                                    typeLabel[type] ?: "",
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = color.copy(alpha = 0.15f),
                                selectedLabelColor = color,
                            ),
                        )
                    }
                }

                TextField(
                    value = uiState.formName,
                    onValueChange = viewModel::onFormNameChanged,
                    label = { Text("Nome do ativo") },
                    placeholder = { Text("Ex: PETR4, Tesouro Selic, Bitcoin...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = fieldColors,
                )

                TextField(
                    value = uiState.formInvestedValue,
                    onValueChange = viewModel::onFormInvestedValueChanged,
                    label = { Text("Valor investido (R$)") },
                    placeholder = { Text("0,00") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    colors = fieldColors,
                )

                TextField(
                    value = uiState.formCurrentValue,
                    onValueChange = viewModel::onFormCurrentValueChanged,
                    label = { Text("Valor atual (R$)") },
                    placeholder = { Text("0,00") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    colors = fieldColors,
                )

                uiState.formError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }

                Button(
                    onClick = viewModel::onAddInvestment,
                    enabled = !uiState.isSaving,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Purple40),
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                    } else {
                        Text("Salvar investimento", fontSize = 16.sp, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun InvestmentSummaryCard(totalInvested: Double, totalCurrent: Double, totalProfit: Double, hideValues: Boolean = false) {
    val profitPercent = if (totalInvested > 0) (totalProfit / totalInvested) * 100 else 0.0
    val isPositive = totalProfit >= 0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(brush = Brush.linearGradient(listOf(PurpleDark, Purple40)), shape = RoundedCornerShape(20.dp))
                .padding(20.dp),
        ) {
            Column {
                Text("Patrimônio Total", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.75f))
                Spacer(Modifier.height(4.dp))
                Text(if (hideValues) HIDDEN else totalCurrent.toBRL(), style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Investido", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f))
                        Text(if (hideValues) HIDDEN else totalInvested.toBRL(), style = MaterialTheme.typography.bodyMedium, color = Color.White)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Rendimento", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (isPositive) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                null,
                                tint = if (isPositive) Color(0xFF81C784) else Color(0xFFEF9A9A),
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "${if (isPositive) "+" else ""}${"%.1f".format(profitPercent)}%",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isPositive) Color(0xFF81C784) else Color(0xFFEF9A9A),
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AllocationDonutCard(rendaVariavel: Double, rendaFixa: Double, criptomoedas: Double, total: Double) {
    if (total <= 0) return
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Alocação", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text("Distribuição por categoria", style = MaterialTheme.typography.bodySmall, color = Color(0xFF9E9E9E))
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                DonutChart(
                    values = listOf(rendaVariavel, rendaFixa, criptomoedas),
                    colors = listOf(typeColor[InvestmentType.RENDA_VARIAVEL]!!, typeColor[InvestmentType.RENDA_FIXA]!!, typeColor[InvestmentType.CRIPTOMOEDAS]!!),
                    modifier = Modifier.size(120.dp),
                )
                Spacer(Modifier.width(20.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    AllocationLegendRow("Renda Variável", rendaVariavel / total * 100, typeColor[InvestmentType.RENDA_VARIAVEL]!!)
                    AllocationLegendRow("Renda Fixa", rendaFixa / total * 100, typeColor[InvestmentType.RENDA_FIXA]!!)
                    AllocationLegendRow("Cripto", criptomoedas / total * 100, typeColor[InvestmentType.CRIPTOMOEDAS]!!)
                }
            }
        }
    }
}

@Composable
private fun DonutChart(values: List<Double>, colors: List<Color>, modifier: Modifier = Modifier) {
    var animPlayed by remember { mutableStateOf(false) }
    val animProgress by animateFloatAsState(
        targetValue = if (animPlayed) 1f else 0f,
        animationSpec = tween(durationMillis = 900),
        label = "donut_anim",
    )
    LaunchedEffect(values) { animPlayed = true }
    val total = values.sum().takeIf { it > 0 } ?: 1.0

    Canvas(modifier = modifier) {
        val strokeWidth = 22.dp.toPx()
        val radius = (size.minDimension - strokeWidth) / 2
        val topLeft = Offset((size.width - radius * 2) / 2, (size.height - radius * 2) / 2)
        val arcSize = Size(radius * 2, radius * 2)
        var startAngle = -90f

        values.forEachIndexed { i, value ->
            val sweep = (value / total * 360f * animProgress).toFloat()
            drawArc(colors.getOrElse(i) { Color.Gray }, startAngle, sweep, false, topLeft, arcSize, style = Stroke(strokeWidth, cap = StrokeCap.Butt))
            startAngle += sweep
        }

        startAngle = -90f
        values.forEach { value ->
            val sweep = (value / total * 360f * animProgress).toFloat()
            drawArc(Color.White, startAngle - 0.5f, 1f, false, topLeft, arcSize, style = Stroke(strokeWidth + 2.dp.toPx(), cap = StrokeCap.Butt))
            startAngle += sweep
        }
    }
}

@Composable
private fun AllocationLegendRow(label: String, percent: Double, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(8.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color(0xFF6B6B6B))
            Text("${"%.1f".format(percent)}%", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = color)
        }
    }
}

@Composable
private fun InvestmentSection(title: String, type: InvestmentType, investments: List<Investment>, hideValues: Boolean = false) {
    var expanded by remember { mutableStateOf(true) }
    val sectionColor = typeColor[type] ?: Purple40

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }.padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(sectionColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(when (type) { InvestmentType.RENDA_VARIAVEL -> "RV"; InvestmentType.RENDA_FIXA -> "RF"; InvestmentType.CRIPTOMOEDAS -> "₿" }, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = sectionColor)
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Text("${investments.size} ativo${if (investments.size != 1) "s" else ""}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF9E9E9E))
                }
                Text(if (hideValues) HIDDEN else investments.sumOf { it.currentValue }.toBRL(), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = sectionColor)
                Spacer(Modifier.width(8.dp))
                Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null, tint = Color(0xFF9E9E9E), modifier = Modifier.size(20.dp))
            }

            AnimatedVisibility(visible = expanded, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
                Column {
                    HorizontalDivider(color = Color(0xFFF0F0F0))
                    if (investments.isEmpty()) {
                        Box(modifier = Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                            Text("Nenhum ativo cadastrado", style = MaterialTheme.typography.bodySmall, color = Color(0xFFBDBDBD))
                        }
                    } else {
                        investments.forEachIndexed { index, investment ->
                            InvestmentItemRow(investment, sectionColor, hideValues)
                            if (index < investments.lastIndex) HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = Color(0xFFF5F5F5))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InvestmentItemRow(investment: Investment, accentColor: Color, hideValues: Boolean = false) {
    val profit = investment.currentValue - investment.investedValue
    val profitPercent = if (investment.investedValue > 0) (profit / investment.investedValue) * 100 else 0.0
    val isPositive = profit >= 0

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(accentColor.copy(alpha = 0.10f)), contentAlignment = Alignment.Center) {
            Text(investment.name.take(2).uppercase(), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = accentColor)
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(investment.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("Investido: ${if (hideValues) HIDDEN else investment.investedValue.toBRL()}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF9E9E9E))
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(if (hideValues) HIDDEN else investment.currentValue.toBRL(), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(if (isPositive) Icons.Default.TrendingUp else Icons.Default.TrendingDown, null, tint = if (isPositive) GreenPositive else RedNegative, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(2.dp))
                Text("${if (isPositive) "+" else ""}${"%.1f".format(profitPercent)}%", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = if (isPositive) GreenPositive else RedNegative)
            }
        }
    }
}