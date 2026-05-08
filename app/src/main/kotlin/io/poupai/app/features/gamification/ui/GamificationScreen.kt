package io.poupai.app.features.gamification.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import io.poupai.app.core.theme.Purple40
import io.poupai.app.core.theme.PurpleDark
import io.poupai.app.domain.model.Badge
import io.poupai.app.features.gamification.viewmodel.GamificationViewModel

@Composable
fun GamificationScreen(
    onNavigateBack: () -> Unit,
    viewModel: GamificationViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {

        // ─── Header ───
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(brush = Brush.verticalGradient(colors = listOf(PurpleDark, Purple40)))
                .padding(24.dp)
                .padding(top = 8.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, "Voltar", tint = Color.White)
                }
                Spacer(Modifier.weight(1f))
                Text("Conquistas", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                Spacer(Modifier.size(48.dp))
            }
        }

        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Purple40)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                Spacer(Modifier.height(4.dp))

                // ─── Cards de pontos e streak ───
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        emoji = "⭐",
                        value = "${uiState.totalPoints}",
                        label = "Pontos",
                        color = Color(0xFFFFB300),
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        emoji = "🔥",
                        value = "${uiState.currentStreak}",
                        label = "Dias seguidos",
                        color = Color(0xFFFF5722),
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        emoji = "🏆",
                        value = "${uiState.longestStreak}",
                        label = "Recorde",
                        color = Color(0xFF9C27B0),
                    )
                }

                // ─── Progresso de pontos ───
                PointsProgressCard(points = uiState.totalPoints)

                // ─── Badges ───
                Text(
                    "Conquistas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )

                val unlockedCount = uiState.badges.count { it.unlocked }
                Text(
                    "$unlockedCount de ${uiState.badges.size} desbloqueadas",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.heightIn(max = 600.dp),
                    userScrollEnabled = false,
                ) {
                    items(uiState.badges) { badge ->
                        BadgeCard(badge = badge)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    emoji: String,
    value: String,
    label: String,
    color: Color,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f)),
    ) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(emoji, fontSize = 24.sp)
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun PointsProgressCard(points: Int) {
    // Níveis: 0, 100, 300, 600, 1000
    val levels = listOf(0, 100, 300, 600, 1000)
    val levelNames = listOf("Iniciante", "Economizador", "Poupador", "Investidor", "Expert")
    val currentLevel = levels.indexOfLast { points >= it }.coerceAtLeast(0)
    val nextLevel = (currentLevel + 1).coerceAtMost(levels.lastIndex)
    val currentLevelPoints = levels[currentLevel]
    val nextLevelPoints = levels[nextLevel]
    val progress = if (currentLevel == levels.lastIndex) 1f
    else ((points - currentLevelPoints).toFloat() / (nextLevelPoints - currentLevelPoints)).coerceIn(0f, 1f)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text("Nível atual", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(levelNames[currentLevel], style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Purple40)
                }
                Box(
                    modifier = Modifier.size(48.dp).clip(CircleShape).background(Purple40.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.EmojiEvents, null, tint = Purple40, modifier = Modifier.size(28.dp))
                }
            }

            if (currentLevel < levels.lastIndex) {
                Spacer(Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("$points pts", style = MaterialTheme.typography.bodySmall, color = Purple40, fontWeight = FontWeight.SemiBold)
                    Text("$nextLevelPoints pts", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = Purple40,
                    trackColor = Purple40.copy(alpha = 0.12f),
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "Próximo nível: ${levelNames[nextLevel]}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Spacer(Modifier.height(8.dp))
                Text("Nível máximo atingido! 🎉", style = MaterialTheme.typography.bodySmall, color = Purple40, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun BadgeCard(badge: Badge) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (badge.unlocked) 1f else 0.4f),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(if (badge.unlocked) 2.dp else 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (badge.unlocked) Color.White else Color(0xFFF5F5F5),
        ),
    ) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(if (badge.unlocked) badge.emoji else "🔒", fontSize = 28.sp)
            Spacer(Modifier.height(6.dp))
            Text(
                badge.title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                color = if (badge.unlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                badge.description,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 9.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 12.sp,
            )
        }
    }
}