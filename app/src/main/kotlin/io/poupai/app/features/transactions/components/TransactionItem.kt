package io.poupai.app.features.transactions.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import io.poupai.app.core.theme.GreenPositive
import io.poupai.app.core.theme.RedNegative
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
    val amountColor = if (isExpense) RedNegative else GreenPositive
    val iconBgColor = if (isExpense) RedNegative.copy(alpha = 0.1f) else GreenPositive.copy(alpha = 0.1f)
    val icon = categoryIcon(transaction.category)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // ─── Ícone da categoria ───
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(iconBgColor),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = transaction.category,
                    tint = amountColor,
                    modifier = Modifier.size(22.dp),
                )
            }

            Spacer(Modifier.width(12.dp))

            // ─── Título e data ───
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = transaction.date.toDisplayFormat(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (transaction.category.isNotBlank()) {
                        Text(
                            text = "·",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = transaction.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            Spacer(Modifier.width(8.dp))

            // ─── Valor ───
            Text(
                text = "${if (isExpense) "-" else "+"}${transaction.amount.toBRL()}",
                style = MaterialTheme.typography.titleMedium,
                color = amountColor,
            )

            // ─── Botão editar ───
            if (onEditClick != null) {
                Spacer(Modifier.width(2.dp))
                IconButton(
                    onClick = { onEditClick(transaction) },
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        Icons.Default.EditNote,
                        contentDescription = "Editar",
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        modifier = Modifier.size(18.dp),
                    )
                }
            }

            // ─── Botão deletar ───
            if (onDeleteClick != null) {
                Spacer(Modifier.width(4.dp))
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
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
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