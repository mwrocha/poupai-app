package io.poupai.app.features.investments.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import io.poupai.app.core.theme.GreenPositive
import io.poupai.app.core.theme.Purple40
import io.poupai.app.core.theme.PurpleDark
import io.poupai.app.core.theme.RedNegative
import io.poupai.app.core.util.toBRL
import io.poupai.app.domain.model.InvestmentType
import io.poupai.app.domain.model.RebalanceItem
import io.poupai.app.domain.model.RebalanceSummary
import io.poupai.app.features.investments.viewmodel.InvestmentsViewModel

@Composable
fun RebalanceScreen(
    onNavigateBack: () -> Unit,
    viewModel: InvestmentsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val rebalance = uiState.rebalance

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F7))
    ) {

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
                    "Rebalanceamento",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.weight(1f))
                Spacer(Modifier.size(48.dp))
            }
        }

        when {
            rebalance == null -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Purple40)
                }
            }

            rebalance.items.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("⚖️", fontSize = 48.sp)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Nenhum ativo cadastrado",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF9E9E9E),
                        )
                        Text(
                            "Cadastre ativos e defina os % alvo",
                            fontSize = 12.sp,
                            color = Color(0xFFBDBDBD),
                        )
                    }
                }
            }

            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item {
                        CategoryAllocationCard(
                            rebalance = rebalance,
                            targetRV = uiState.categoryTargetRV,
                            targetRF = uiState.categoryTargetRF,
                            targetCripto = uiState.categoryTargetCripto,
                            onSaveTarget = { type, value ->
                                viewModel.saveCategoryTarget(
                                    type, value
                                )
                            },
                        )
                    }

                    item {
                        Text(
                            "Total da carteira: ${rebalance.totalCurrentValue.toBRL()}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF6B6B6B),
                        )
                    }

                    items(rebalance.items) { item ->
                        RebalanceItemCard(
                            item = item,
                            isSaving = uiState.savingAllocationTargetId == item.investmentId,
                            onSaveTarget = { target ->
                                viewModel.updateAllocationTarget(item.investmentId, target)
                            },
                        )
                    }

                    item { Spacer(Modifier.height(32.dp)) }
                }
            }
        }
    }
}

@Composable
private fun CategoryAllocationCard(
    rebalance: RebalanceSummary,
    targetRV: Double,
    targetRF: Double,
    targetCripto: Double,
    onSaveTarget: (InvestmentType, Double) -> Unit,
) {
    val total = rebalance.totalCurrentValue.takeIf { it > 0.0 } ?: 1.0
    val currentRV = rebalance.items.filter { it.type == "RENDA_VARIAVEL" }
        .sumOf { it.currentValue } / total * 100.0
    val currentRF =
        rebalance.items.filter { it.type == "RENDA_FIXA" }.sumOf { it.currentValue } / total * 100.0
    val currentCripto = rebalance.items.filter { it.type == "CRIPTOMOEDAS" }
        .sumOf { it.currentValue } / total * 100.0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Alocação por Categoria",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1C1B1F),
            )
            Spacer(Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Categoria",
                    fontSize = 11.sp,
                    color = Color(0xFF9E9E9E),
                    modifier = Modifier.weight(1.8f)
                )
                Text(
                    "Atual",
                    fontSize = 11.sp,
                    color = Color(0xFF9E9E9E),
                    modifier = Modifier.weight(0.9f),
                    textAlign = TextAlign.End
                )
                Text(
                    "Alvo",
                    fontSize = 11.sp,
                    color = Color(0xFF9E9E9E),
                    modifier = Modifier.width(88.dp),
                    textAlign = TextAlign.Center
                )
                Text(
                    "Dif.",
                    fontSize = 11.sp,
                    color = Color(0xFF9E9E9E),
                    modifier = Modifier.weight(0.9f),
                    textAlign = TextAlign.End
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFF0F0F0)
            )

            CategoryRow(
                label = "Renda Variável",
                color = Color(0xFF503173),
                currentPercent = currentRV,
                savedTarget = targetRV,
                onSave = { onSaveTarget(InvestmentType.RENDA_VARIAVEL, it) },
            )
            Spacer(Modifier.height(10.dp))
            CategoryRow(
                label = "Renda Fixa",
                color = Color(0xFF4CAF50),
                currentPercent = currentRF,
                savedTarget = targetRF,
                onSave = { onSaveTarget(InvestmentType.RENDA_FIXA, it) },
            )
            Spacer(Modifier.height(10.dp))
            CategoryRow(
                label = "Criptomoedas",
                color = Color(0xFFFF9800),
                currentPercent = currentCripto,
                savedTarget = targetCripto,
                onSave = { onSaveTarget(InvestmentType.CRIPTOMOEDAS, it) },
            )
        }
    }
}

@Composable
private fun CategoryRow(
    label: String,
    color: Color,
    currentPercent: Double,
    savedTarget: Double,
    onSave: (Double) -> Unit,
) {
    var text by remember(savedTarget) {
        mutableStateOf(if (savedTarget == 0.0) "" else String.format("%.1f", savedTarget))
    }

    fun commit() {
        val value = text.replace(",", ".").toDoubleOrNull()?.coerceIn(0.0, 100.0) ?: return
        onSave(value)
    }

    val parsedTarget = text.replace(",", ".").toDoubleOrNull() ?: savedTarget
    val diff = parsedTarget - currentPercent
    val diffColor = when {
        diff > 0.05 -> GreenPositive
        diff < -0.05 -> RedNegative
        else -> Color(0xFF9E9E9E)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(modifier = Modifier.weight(1.8f), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color),
            )
            Spacer(Modifier.width(6.dp))
            Text(label, fontSize = 12.sp, color = Color(0xFF1C1B1F))
        }
        Text(
            "${String.format("%.1f", currentPercent)}%",
            fontSize = 12.sp,
            color = Color(0xFF6B6B6B),
            modifier = Modifier.weight(0.9f),
            textAlign = TextAlign.End,
        )
        Row(
            modifier = Modifier
                .width(88.dp)
                .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = { new ->
                    if (new.isEmpty() || (new.length <= 5 && new.replace(",", ".").toDoubleOrNull()
                            ?.let { it <= 100.0 } != false)
                    ) {
                        text = new
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .onFocusChanged { if (!it.isFocused) commit() },
                textStyle = LocalTextStyle.current.copy(
                    fontSize = 12.sp,
                    textAlign = TextAlign.End,
                    color = Color(0xFF1C1B1F),
                ),
                placeholder = {
                    Text(
                        "0",
                        fontSize = 11.sp,
                        color = Color(0xFFBDBDBD),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End,
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { commit() }),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = color,
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                    focusedContainerColor = color.copy(alpha = 0.04f),
                    unfocusedContainerColor = Color.Transparent,
                    focusedTextColor = Color(0xFF1C1B1F),
                    unfocusedTextColor = Color(0xFF1C1B1F),
                    cursorColor = color,
                ),
            )
            Text(
                "%",
                fontSize = 11.sp,
                color = Color(0xFF9E9E9E),
                modifier = Modifier.padding(start = 2.dp)
            )
        }
        Text(
            "${if (diff >= 0) "+" else ""}${String.format("%.1f", diff)}%",
            fontSize = 12.sp,
            color = diffColor,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(0.9f),
            textAlign = TextAlign.End,
        )
    }
}

@Composable
private fun RebalanceItemCard(
    item: RebalanceItem,
    isSaving: Boolean,
    onSaveTarget: (Double) -> Unit,
) {
    val actionColor = when (item.action) {
        "COMPRAR" -> GreenPositive
        "VENDER" -> RedNegative
        else -> Color(0xFF9E9E9E)
    }
    val typeColor = when (item.type) {
        "RENDA_VARIAVEL" -> Color(0xFF503173)
        "RENDA_FIXA" -> Color(0xFF4CAF50)
        else -> Color(0xFFFF9800)
    }

    var targetText by remember(item.investmentId) {
        mutableStateOf(
            if (item.targetPercent == 0.0) "" else String.format("%.1f", item.targetPercent),
        )
    }

    fun commitTarget() {
        val value = targetText.replace(",", ".").toDoubleOrNull()?.coerceIn(0.0, 100.0) ?: return
        onSaveTarget(value)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(typeColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        item.name.take(2).uppercase(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = typeColor,
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        item.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1C1B1F)
                    )
                    Text(item.currentValue.toBRL(), fontSize = 12.sp, color = Color(0xFF6B6B6B))
                }
                Surface(shape = RoundedCornerShape(8.dp), color = actionColor.copy(alpha = 0.12f)) {
                    Text(
                        item.action,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = actionColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Alocação atual vs alvo", fontSize = 11.sp, color = Color(0xFF9E9E9E))
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Atual: ${String.format("%.1f", item.currentPercent)}%",
                        fontSize = 12.sp,
                        color = typeColor,
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Alvo: ", fontSize = 12.sp, color = Color(0xFF6B6B6B))
                    OutlinedTextField(
                        value = targetText,
                        onValueChange = { new ->
                            if (new.isEmpty() || (new.length <= 5 && new.replace(",", ".")
                                    .toDoubleOrNull()?.let { it <= 100.0 } != false)
                            ) {
                                targetText = new
                            }
                        },
                        modifier = Modifier
                            .width(80.dp)
                            .onFocusChanged { if (!it.isFocused) commitTarget() },
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = 12.sp,
                            textAlign = TextAlign.End,
                            color = Color(0xFF1C1B1F),
                        ),
                        placeholder = {
                            Text(
                                "0",
                                fontSize = 11.sp,
                                color = Color(0xFFBDBDBD),
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.End,
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Done,
                        ),
                        keyboardActions = KeyboardActions(onDone = { commitTarget() }),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = typeColor,
                            unfocusedBorderColor = Color(0xFFE0E0E0),
                            focusedContainerColor = typeColor.copy(alpha = 0.04f),
                            unfocusedContainerColor = Color.Transparent,
                            focusedTextColor = Color(0xFF1C1B1F),
                            unfocusedTextColor = Color(0xFF1C1B1F),
                            cursorColor = typeColor,
                        ),
                    )
                    Spacer(Modifier.width(4.dp))
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 1.5.dp,
                            color = typeColor,
                        )
                    } else {
                        Text("%", fontSize = 12.sp, color = Color(0xFF6B6B6B))
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { (item.currentPercent / 100).toFloat().coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = typeColor,
                trackColor = typeColor.copy(alpha = 0.12f),
            )
            Spacer(Modifier.height(2.dp))
            LinearProgressIndicator(
                progress = { (item.targetPercent / 100).toFloat().coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = Color(0xFFBDBDBD),
                trackColor = Color(0xFFF0F0F0),
            )

            if (item.action != "OK") {
                Spacer(Modifier.height(10.dp))
                Text(
                    "${item.action} ${item.amountToAdjust.toBRL()} para rebalancear",
                    fontSize = 12.sp,
                    color = actionColor,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}