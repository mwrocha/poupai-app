package io.poupai.app.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

data class DrawerMenuItem(
    val icon: ImageVector,
    val label: String,
    val route: String,
)

val drawerMenuItems = listOf(
    DrawerMenuItem(Icons.Default.AttachMoney,    "Investimentos",     "investments"),
    DrawerMenuItem(Icons.Default.AccountBalance, "Finanças Pessoais", "finances"),
    DrawerMenuItem(Icons.Default.SwapHoriz,      "Transações",        "transactions"),
    DrawerMenuItem(Icons.Default.Sell,           "Tags",              "tags"),
    DrawerMenuItem(Icons.Default.TrackChanges,   "Metas",             "goals"),
    DrawerMenuItem(Icons.Default.Person,         "Perfil",            "profile"),
    DrawerMenuItem(Icons.Default.Settings,       "Configurações",     "settings"),
)

@Composable
fun PoupaiDrawerContent(
    userName: String,
    userHandle: String,
    profileImageUrl: String?,
    selectedRoute: String?,
    onItemClick: (String) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(300.dp)
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = 32.dp),
    ) {
        // Header: avatar + nome
        Row(
            modifier = Modifier.padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(56.dp).clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = userName.firstOrNull()?.uppercase() ?: "U",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(userName, style = MaterialTheme.typography.titleMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("@$userHandle", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(6.dp))
                    Box(Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF4CAF50)))
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        drawerMenuItems.forEach { item ->
            val isSelected = item.route == selectedRoute
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 2.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                    .clickable { onItemClick(item.route) }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(item.icon, item.label,
                    tint = if (isSelected) MaterialTheme.colorScheme.primary
                           else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(16.dp))
                Text(item.label, style = MaterialTheme.typography.bodyLarge,
                    color = if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f))
                Icon(Icons.Default.ChevronRight, null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp))
            }
        }

        Spacer(Modifier.weight(1f))

        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier.padding(horizontal = 24.dp).fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text("Sair")
            Spacer(Modifier.width(8.dp))
            Icon(Icons.Default.ExitToApp, "Sair", modifier = Modifier.size(20.dp))
        }
    }
}
