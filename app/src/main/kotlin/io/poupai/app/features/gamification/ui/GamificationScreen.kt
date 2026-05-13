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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F7)),
    ) {
        // ─── Header ───
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
                    .padding(horizontal = 20.dp)
                    .padding(top = 16.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // ─── Card de nível/pontos ───
                LevelSummaryCard(points = uiState.totalPoints)

                // ─── Cards de streak ───
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    StatCard(Modifier.weight(1f), "🔥", "${uiState.currentStreak}", "Dias seguidos", Color(0xFFFF5722))
                    StatCard(Modifier.weight(1f), "🏆", "${uiState.longestStreak}", "Recorde", Color(0xFF9C27B0))
                    StatCard(Modifier.weight(1f), "⭐", "${uiState.totalPoints}", "Pontos", Color(0xFFFFB300))
                }

                Spacer(Modifier.height(4.dp))

                // ─── Título conquistas ───
                val unlockedCount = uiState.badges.count { it.unlocked }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Conquistas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = Color(0xFF1C1B1F))
                    Text("$unlockedCount/${uiState.badges.size}", fontSize = 12.sp, color = Color(0xFF9E9E9E))
                }

                // ─── Grid de badges ───
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.heightIn(max = 600.dp),
                    userScrollEnabled = false,
                ) {
                    items(uiState.badges) { badge -> BadgeCard(badge) }
                }
            }
        }
    }
}

// ─── CARD DE NÍVEL ───

@Composable
private fun LevelSummaryCard(points: Int) {
    val levels = listOf(0, 100, 300, 600, 1000)
    val levelNames = listOf("Iniciante", "Economizador", "Poupador", "Investidor", "Expert")
    val currentLevel = levels.indexOfLast { points >= it }.coerceAtLeast(0)
    val nextLevel = (currentLevel + 1).coerceAtMost(levels.lastIndex)
    val progress = if (currentLevel == levels.lastIndex) 1f
    else ((points - levels[currentLevel]).toFloat() / (levels[nextLevel] - levels[currentLevel])).coerceIn(0f, 1f)

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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text("Nível atual", fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
                        Text(levelNames[currentLevel], style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Box(
                        modifier = Modifier.size(52.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Default.EmojiEvents, null, tint = Color.White, modifier = Modifier.size(30.dp))
                    }
                }

                if (currentLevel < levels.lastIndex) {
                    Spacer(Modifier.height(16.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = Color.White,
                        trackColor = Color.White.copy(alpha = 0.25f),
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("$points pts", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                        Text("Próximo: ${levelNames[nextLevel]} (${levels[nextLevel]} pts)", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                    }
                } else {
                    Spacer(Modifier.height(8.dp))
                    Text("Nível máximo atingido! 🎉", fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

// ─── CARD DE STAT ───

@Composable
private fun StatCard(modifier: Modifier, emoji: String, value: String, label: String, color: Color) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(emoji, fontSize = 22.sp)
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color)
            Text(label, fontSize = 10.sp, color = Color(0xFF9E9E9E), textAlign = TextAlign.Center, lineHeight = 13.sp)
        }
    }
}

// ─── BADGE CARD ───

@Composable
private fun BadgeCard(badge: Badge) {
    Card(
        modifier = Modifier.fillMaxWidth().alpha(if (badge.unlocked) 1f else 0.45f),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(if (badge.unlocked) 1.dp else 0.dp),
        colors = CardDefaults.cardColors(containerColor = if (badge.unlocked) Color.White else Color(0xFFEEEEEE)),
    ) {
        Column(
            modifier = Modifier.padding(10.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(if (badge.unlocked) badge.emoji else "🔒", fontSize = 26.sp)
            Spacer(Modifier.height(6.dp))
            Text(
                badge.title,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                color = if (badge.unlocked) Color(0xFF1C1B1F) else Color(0xFF9E9E9E),
            )
            Spacer(Modifier.height(3.dp))
            Text(
                badge.description,
                fontSize = 9.sp,
                textAlign = TextAlign.Center,
                color = Color(0xFF9E9E9E),
                lineHeight = 12.sp,
            )
        }
    }
}