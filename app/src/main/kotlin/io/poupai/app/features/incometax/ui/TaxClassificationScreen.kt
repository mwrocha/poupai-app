package io.poupai.app.features.incometax.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import io.poupai.app.core.theme.Purple40
import io.poupai.app.core.theme.PurpleDark
import io.poupai.app.domain.model.InvestmentType
import io.poupai.app.features.incometax.viewmodel.TaxClassificationViewModel

@Composable
fun TaxClassificationScreen(
    onNavigateBack: () -> Unit,
    viewModel: TaxClassificationViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val rvAssets = uiState.investments.filter { it.type == InvestmentType.RENDA_VARIAVEL }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F7))) {

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
                    Icon(Icons.Default.ArrowBack, "Voltar", tint = Color.White)
                }
                Spacer(Modifier.weight(1f))
                Text(
                    "Classificação tributária",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.weight(1f))
                Spacer(Modifier.size(48.dp))
            }
        }

        when {
            uiState.isLoading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator(color = Purple40)
            }
            rvAssets.isEmpty() -> Box(
                Modifier.fillMaxSize().padding(32.dp),
                Alignment.Center,
            ) {
                Text(
                    "Nenhum ativo de Renda Variável cadastrado.",
                    fontSize = 13.sp,
                    color = Color(0xFF9E9E9E),
                )
            }
            else -> LazyColumn(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item { ExplainerCard() }
                item {
                    Text(
                        "Renda Variável (${rvAssets.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF6B6B6B),
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                    )
                }
                items(rvAssets, key = { it.id }) { inv ->
                    val isFii = inv.id in uiState.fiiIds
                    AssetRow(
                        name = inv.name,
                        isFii = isFii,
                        onToggle = { viewModel.toggleFii(inv.id, it) },
                    )
                }
                item { Spacer(Modifier.height(40.dp)) }
            }
        }
    }
}

@Composable
private fun ExplainerCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(
                Icons.Default.Info,
                contentDescription = null,
                tint = Color(0xFF1565C0),
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(8.dp))
            Column {
                Text(
                    "Por que classificar?",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF0D47A1),
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Ações e FIIs têm regras diferentes:\n" +
                        "• Ações: isenção até R\$ 20.000/mês em vendas, 15% sobre lucro acima\n" +
                        "• FIIs: 20% sobre lucro, sem isenção",
                    fontSize = 11.sp,
                    color = Color(0xFF0D47A1).copy(alpha = 0.85f),
                    lineHeight = 14.sp,
                )
            }
        }
    }
}

@Composable
private fun AssetRow(
    name: String,
    isFii: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        if (isFii) Color(0xFF4CAF50).copy(alpha = 0.12f)
                        else Color(0xFF503173).copy(alpha = 0.10f),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    name.take(2).uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isFii) Color(0xFF2E7D32) else Color(0xFF503173),
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1C1B1F),
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    if (isFii) "Tratado como FII (20%, sem isenção)"
                    else "Tratado como ação (15%, isenção R\$ 20k/mês)",
                    fontSize = 10.sp,
                    color = Color(0xFF9E9E9E),
                )
            }
            Switch(
                checked = isFii,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF4CAF50),
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color(0xFFBDBDBD),
                ),
            )
        }
    }
}
