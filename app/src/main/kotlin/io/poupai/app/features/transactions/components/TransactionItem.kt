package io.poupai.app.features.transactions.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.poupai.app.core.theme.PoupaiTheme
import io.poupai.app.core.theme.Purple40
import io.poupai.app.core.theme.Purple60
import io.poupai.app.core.theme.PurpleLight
import io.poupai.app.core.util.toBRL
import io.poupai.app.core.util.toDisplayFormat
import io.poupai.app.domain.model.Transaction
import io.poupai.app.domain.model.TransactionType

@Composable
fun TransactionItem(
    transaction: Transaction,
    onDeleteClick: ((Transaction) -> Unit)? = null,
    onEditClick: ((Transaction) -> Unit)? = null,
    isDeleting: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val isExpense = transaction.type == TransactionType.EXPENSE
    // Paleta on-brand: receita roxo escuro, despesa lavanda
    val accentColor = if (isExpense) Purple60 else Purple40
    val iconBgColor = PurpleLight.copy(alpha = 0.55f)
    val icon = categoryIcon(transaction.category)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PoupaiTheme.tokens.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // ─── Avatar da categoria ───
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBgColor),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = transaction.category,
                    tint = Purple40,
                    modifier = Modifier.size(20.dp),
                )
            }

            Spacer(Modifier.width(12.dp))

            // ─── Título e meta ───
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PoupaiTheme.tokens.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = transaction.date.toDisplayFormat(),
                        fontSize = 11.sp,
                        color = PoupaiTheme.tokens.textMuted,
                    )
                    if (transaction.category.isNotBlank()) {
                        Box(
                            Modifier
                                .size(3.dp)
                                .clip(CircleShape)
                                .background(PoupaiTheme.tokens.textMuted),
                        )
                        Text(
                            text = transaction.category,
                            fontSize = 11.sp,
                            color = PoupaiTheme.tokens.textSecondary,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }

            Spacer(Modifier.width(8.dp))

            // ─── Valor ───
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (isExpense) "−" else "+"} ${transaction.amount.toBRL()}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                )
                // Faixinha sutil que diferencia tipo sem usar verde/vermelho
                Spacer(Modifier.height(4.dp))
                Box(
                    Modifier
                        .width(28.dp)
                        .height(2.dp)
                        .clip(RoundedCornerShape(1.dp))
                        .background(accentColor.copy(alpha = 0.45f)),
                )
            }

            if (onEditClick != null) {
                Spacer(Modifier.width(2.dp))
                IconButton(
                    onClick = { onEditClick(transaction) },
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        Icons.Default.EditNote,
                        contentDescription = "Editar",
                        tint = Purple40.copy(alpha = 0.55f),
                        modifier = Modifier.size(18.dp),
                    )
                }
            }

            if (onDeleteClick != null) {
                Spacer(Modifier.width(2.dp))
                if (isDeleting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.error,
                    )
                } else {
                    IconButton(
                        onClick = { onDeleteClick(transaction) },
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(
                            Icons.Default.DeleteOutline,
                            contentDescription = "Deletar",
                            tint = PoupaiTheme.tokens.textMuted,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }
        }
    }
}

private fun categoryIcon(category: String): ImageVector {
    val lower = category.lowercase()
    return when {
        lower.contains("salário") || lower.contains("salario") || lower.contains("renda") -> Icons.Default.AccountBalance
        lower.contains("aliment") || lower.contains("comida") || lower.contains("restaur") || lower.contains("mercado") -> Icons.Default.ShoppingCart
        lower.contains("transport") || lower.contains("uber") || lower.contains("combustív") || lower.contains("gasolina") -> Icons.Default.DirectionsCar
        lower.contains("saúde") || lower.contains("saude") || lower.contains("médico") || lower.contains("farmácia") -> Icons.Default.LocalHospital
        lower.contains("educação") || lower.contains("educacao") || lower.contains("curso") || lower.contains("escola") -> Icons.Default.School
        lower.contains("lazer") || lower.contains("entretenim") || lower.contains("cinema") || lower.contains("viagem") -> Icons.Default.SportsEsports
        lower.contains("moradia") || lower.contains("aluguel") || lower.contains("condom") -> Icons.Default.Home
        lower.contains("invest") || lower.contains("dividend") || lower.contains("rendim") -> Icons.Default.TrendingUp
        lower.contains("roupa") || lower.contains("vestuário") || lower.contains("compras") -> Icons.Default.ShoppingBag
        lower.contains("conta") || lower.contains("energia") || lower.contains("água") || lower.contains("internet") -> Icons.Default.Receipt
        else -> if (lower.contains("receita") || lower.contains("income")) Icons.Default.Add else Icons.Default.AttachMoney
    }
}
