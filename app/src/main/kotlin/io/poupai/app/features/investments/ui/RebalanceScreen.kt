package io.poupai.app.features.investments.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import io.poupai.app.core.theme.GreenPositive
import io.poupai.app.core.theme.Purple40
import io.poupai.app.core.theme.PurpleDark
import io.poupai.app.core.theme.RedNegative
import io.poupai.app.core.util.toBRL
import io.poupai.app.domain.model.RebalanceItem
import io.poupai.app.features.investments.viewmodel.InvestmentsViewModel

@Composable
fun RebalanceScreen(
    onNavigateBack: () -> Unit,
    viewModel: InvestmentsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val rebalance = uiState.rebalance

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F7))) {

        Box(modifier = Modifier.fillMaxWidth()
            .background(brush = Brush.verticalGradient(colors = listOf(PurpleDark, Purple40)))
            .padding(horizontal = 20.dp).padding(top = 16.dp, bottom = 16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, "Voltar", tint = Color.White)
                }
                Spacer(Modifier.weight(1f))
                Text("Rebalanceamento", style = MaterialTheme.typography.titleLarge,
                    color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                Spacer(Modifier.size(48.dp))
            }
        }

        if (rebalance == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Purple40)
            }
        } else if (rebalance.items.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("⚖️", fontSize = 48.sp)
                    Spacer(Modifier.height(12.dp))
                    Text("Nenhum ativo cadastrado", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF9E9E9E))
                    Text("Cadastre ativos e defina os % alvo", fontSize = 12.sp, color = Color(0xFFBDBDBD))
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    // ─── Dica ───
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Purple40.copy(alpha = 0.08f)),
                        elevation = CardDefaults.cardElevation(0.dp)) {
                        Text("Configure o % alvo de cada ativo na edição do investimento. " +
                                "O rebalanceamento mostra o quanto comprar ou vender para atingir sua alocação ideal.",
                            fontSize = 12.sp, color = Purple40,
                            modifier = Modifier.padding(12.dp), lineHeight = 16.sp)
                    }
                }

                item {
                    Text("Total da carteira: ${rebalance.totalCurrentValue.toBRL()}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold, color = Color(0xFF6B6B6B))
                }

                items(rebalance.items) { item -> RebalanceItemCard(item = item) }
                item { Spacer(Modifier.height(32.dp)) }
            }
        }
    }
}

@Composable
private fun RebalanceItemCard(item: RebalanceItem) {
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

    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(typeColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center) {
                    Text(item.name.take(2).uppercase(), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = typeColor)
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(item.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1C1B1F))
                    Text(item.currentValue.toBRL(), fontSize = 12.sp, color = Color(0xFF6B6B6B))
                }
                Surface(shape = RoundedCornerShape(8.dp), color = actionColor.copy(alpha = 0.12f)) {
                    Text(item.action, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = actionColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                }
            }

            Spacer(Modifier.height(12.dp))

            // Barras de alocação
            Text("Alocação atual vs alvo", fontSize = 11.sp, color = Color(0xFF9E9E9E))
            Spacer(Modifier.height(6.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Atual: ${String.format("%.1f", item.currentPercent)}%", fontSize = 12.sp,
                    color = typeColor, fontWeight = FontWeight.SemiBold)
                Text("Alvo: ${String.format("%.1f", item.targetPercent)}%", fontSize = 12.sp,
                    color = Color(0xFF9E9E9E))
            }
            Spacer(Modifier.height(4.dp))

            // Barra atual
            LinearProgressIndicator(progress = { (item.currentPercent / 100).toFloat().coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = typeColor, trackColor = typeColor.copy(alpha = 0.12f))
            Spacer(Modifier.height(2.dp))
            // Barra alvo
            LinearProgressIndicator(progress = { (item.targetPercent / 100).toFloat().coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(3.dp).clip(RoundedCornerShape(3.dp)),
                color = Color(0xFFBDBDBD), trackColor = Color(0xFFF0F0F0))

            if (item.action != "OK") {
                Spacer(Modifier.height(10.dp))
                Text(
                    "${item.action} ${item.amountToAdjust.toBRL()} para rebalancear",
                    fontSize = 12.sp, color = actionColor, fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}