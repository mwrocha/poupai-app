package io.poupai.app.features.transactions.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // TODO: ícone da categoria

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = transaction.title,
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = transaction.date.toDisplayFormat(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Text(
            text = "${if (transaction.type == TransactionType.EXPENSE) "-" else "+"}${transaction.amount.toBRL()}",
            style = MaterialTheme.typography.titleMedium,
            color = if (transaction.type == TransactionType.EXPENSE) RedNegative else GreenPositive,
        )
    }
}
