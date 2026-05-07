package io.poupai.app.features.goals.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import io.poupai.app.core.theme.GreenPositive
import io.poupai.app.core.theme.Purple40
import io.poupai.app.core.theme.PurpleDark
import io.poupai.app.core.util.toBRL
import io.poupai.app.domain.model.Goal
import io.poupai.app.features.goals.state.GoalsUiState
import io.poupai.app.features.goals.viewmodel.GoalsViewModel
import java.text.SimpleDateFormat
import java.util.Locale

// ─── Opções de customização ───

private val iconOptions = listOf("🎯", "🏠", "✈️", "🚗", "📱", "💍", "🎓", "🏋️", "💻", "🎸", "🐾", "🌍")
private val colorOptions = listOf(
    "#503173", "#4CAF50", "#FF9800", "#E91E63",
    "#2196F3", "#009688", "#FF5722", "#795548",
)

private fun String.toComposeColor(): Color = try {
    Color(android.graphics.Color.parseColor(if (startsWith("#")) this else "#$this"))
} catch (e: Exception) { Color(0xFF503173) }

// ─── Screen ───

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun GoalsScreen(
    onNavigateBack: () -> Unit,
    viewModel: GoalsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val addSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val progressSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    val completed = uiState.goals.count { it.isCompleted }
    val totalTarget = uiState.goals.sumOf { it.targetValue }
    val totalCurrent = uiState.goals.sumOf { it.currentValue }

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
            ) { Icon(Icons.Default.Add, "Nova meta", tint = Color.White) }
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            // ─── Header gradiente ───
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(PurpleDark, Purple40)))
                    .padding(24.dp)
                    .padding(top = 16.dp),
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Default.ArrowBack, "Voltar", tint = Color.White)
                        }
                        Spacer(Modifier.weight(1f))
                        Text("Metas", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.weight(1f))
                        Spacer(Modifier.size(48.dp))
                    }
                    Spacer(Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        HeaderStat("Total", "${uiState.goals.size}")
                        Box(Modifier.width(1.dp).height(36.dp).background(Color.White.copy(alpha = 0.3f)))
                        HeaderStat("Concluídas", "$completed")
                        Box(Modifier.width(1.dp).height(36.dp).background(Color.White.copy(alpha = 0.3f)))
                        HeaderStat("Em andamento", "${uiState.goals.size - completed}")
                    }
                }
            }

            when {
                uiState.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Purple40)
                }
                uiState.goals.isEmpty() -> EmptyGoalsState()
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        item { OverallProgressCard(totalCurrent, totalTarget) }

                        val active = uiState.goals.filter { !it.isCompleted }
                        if (active.isNotEmpty()) {
                            item {
                                Text("Em andamento", style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold, color = Color(0xFF6B6B6B))
                            }
                            items(active, key = { it.id }) { goal ->
                                GoalCard(goal, dateFormat,
                                    onAddProgress = { viewModel.onShowProgressSheet(goal) },
                                    onDelete = { viewModel.onDeleteGoal(goal.id) },
                                )
                            }
                        }

                        val done = uiState.goals.filter { it.isCompleted }
                        if (done.isNotEmpty()) {
                            item {
                                Text("Concluídas ✓", style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold, color = GreenPositive)
                            }
                            items(done, key = { it.id }) { goal ->
                                GoalCard(goal, dateFormat,
                                    onAddProgress = {},
                                    onDelete = { viewModel.onDeleteGoal(goal.id) },
                                )
                            }
                        }

                        item { Spacer(Modifier.height(80.dp)) }
                    }
                }
            }
        }
    }

    // ─── Bottom Sheet: nova meta ───
    if (uiState.showAddSheet) {
        ModalBottomSheet(onDismissRequest = viewModel::onDismissAddSheet, sheetState = addSheetState) {
            AddGoalSheetContent(uiState, fieldColors,
                onTitleChanged = viewModel::onFormTitleChanged,
                onTargetChanged = viewModel::onFormTargetChanged,
                onCurrentChanged = viewModel::onFormCurrentChanged,
                onDeadlineChanged = viewModel::onFormDeadlineChanged,
                onIconChanged = viewModel::onFormIconChanged,
                onColorChanged = viewModel::onFormColorChanged,
                onSave = viewModel::onSaveGoal,
            )
        }
    }

    // ─── Bottom Sheet: progresso ───
    if (uiState.showProgressSheet) {
        ModalBottomSheet(onDismissRequest = viewModel::onDismissProgressSheet, sheetState = progressSheetState) {
            UpdateProgressSheetContent(uiState, fieldColors,
                onProgressChanged = viewModel::onProgressInputChanged,
                onConfirm = viewModel::onUpdateProgress,
            )
        }
    }
}

// ─── Header stat ───

@Composable
private fun HeaderStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
    }
}

// ─── Card de progresso geral ───

@Composable
private fun OverallProgressCard(totalCurrent: Double, totalTarget: Double) {
    val progress = if (totalTarget > 0) (totalCurrent / totalTarget).toFloat().coerceIn(0f, 1f) else 0f
    var animPlayed by remember { mutableStateOf(false) }
    val animProgress by animateFloatAsState(if (animPlayed) progress else 0f, tween(900), label = "overall")
    LaunchedEffect(Unit) { animPlayed = true }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Progresso Geral", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Text(totalCurrent.toBRL(), style = MaterialTheme.typography.bodyMedium, color = Purple40, fontWeight = FontWeight.SemiBold)
                Text(totalTarget.toBRL(), style = MaterialTheme.typography.bodyMedium, color = Color(0xFF9E9E9E))
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { animProgress },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = Purple40, trackColor = Purple40.copy(alpha = 0.12f),
            )
            Spacer(Modifier.height(6.dp))
            Text("${(progress * 100).toInt()}% do total acumulado",
                style = MaterialTheme.typography.bodySmall, color = Color(0xFF9E9E9E))
        }
    }
}

// ─── Card individual de meta ───

@Composable
private fun GoalCard(
    goal: Goal,
    dateFormat: SimpleDateFormat,
    onAddProgress: () -> Unit,
    onDelete: () -> Unit,
) {
    val goalColor = goal.color.toComposeColor()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var animPlayed by remember { mutableStateOf(false) }
    val animProgress by animateFloatAsState(
        if (animPlayed) goal.progress else 0f, tween(800), label = "goal_${goal.id}")
    LaunchedEffect(Unit) { animPlayed = true }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(48.dp).clip(RoundedCornerShape(14.dp))
                        .background(goalColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center,
                ) { Text(goal.icon, fontSize = 22.sp) }

                Spacer(Modifier.width(12.dp))

                Column(Modifier.weight(1f)) {
                    Text(goal.title, style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    goal.deadline?.let {
                        Text("Prazo: ${dateFormat.format(it)}",
                            style = MaterialTheme.typography.bodySmall, color = Color(0xFF9E9E9E))
                    }
                }

                if (goal.isCompleted) {
                    Icon(Icons.Default.CheckCircle, null, tint = GreenPositive, modifier = Modifier.size(24.dp))
                } else {
                    IconButton(onClick = onAddProgress, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Edit, "Atualizar", tint = goalColor, modifier = Modifier.size(20.dp))
                    }
                }
                IconButton(onClick = { showDeleteDialog = true }, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Delete, "Excluir", tint = Color(0xFFBDBDBD), modifier = Modifier.size(20.dp))
                }
            }

            Spacer(Modifier.height(16.dp))

            LinearProgressIndicator(
                progress = { animProgress },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = if (goal.isCompleted) GreenPositive else goalColor,
                trackColor = goalColor.copy(alpha = 0.10f),
            )
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Text(goal.currentValue.toBRL(), style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (goal.isCompleted) GreenPositive else goalColor)
                Text(
                    if (goal.isCompleted) "Concluída! 🎉" else "Faltam ${goal.remaining.toBRL()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (goal.isCompleted) GreenPositive else Color(0xFF9E9E9E),
                )
                Text(goal.targetValue.toBRL(), style = MaterialTheme.typography.bodySmall, color = Color(0xFF9E9E9E))
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Excluir meta") },
            text = { Text("Deseja excluir \"${goal.title}\"? Esta ação não pode ser desfeita.") },
            confirmButton = {
                TextButton(onClick = { showDeleteDialog = false; onDelete() }) {
                    Text("Excluir", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") }
            },
        )
    }
}

// ─── Sheet: nova meta ───

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddGoalSheetContent(
    uiState: GoalsUiState,
    fieldColors: TextFieldColors,
    onTitleChanged: (String) -> Unit,
    onTargetChanged: (String) -> Unit,
    onCurrentChanged: (String) -> Unit,
    onDeadlineChanged: (String) -> Unit,
    onIconChanged: (String) -> Unit,
    onColorChanged: (String) -> Unit,
    onSave: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Nova Meta", fontSize = 20.sp, fontWeight = FontWeight.Bold)

        // Seletor de ícone
        Text("Ícone", style = MaterialTheme.typography.labelMedium, color = Color(0xFF9E9E9E))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            iconOptions.forEach { icon ->
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (uiState.formIcon == icon) Purple40.copy(alpha = 0.15f) else Color(0xFFF5F5F5))
                        .clickable { onIconChanged(icon) },
                    contentAlignment = Alignment.Center,
                ) { Text(icon, fontSize = 20.sp) }
            }
        }

        // Seletor de cor
        Text("Cor", style = MaterialTheme.typography.labelMedium, color = Color(0xFF9E9E9E))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            colorOptions.forEach { hex ->
                val selected = uiState.formColor == hex
                Box(
                    modifier = Modifier
                        .size(if (selected) 36.dp else 32.dp)
                        .clip(CircleShape)
                        .background(hex.toComposeColor())
                        .clickable { onColorChanged(hex) },
                    contentAlignment = Alignment.Center,
                ) {
                    if (selected) Icon(Icons.Default.CheckCircle, null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
        }

        TextField(value = uiState.formTitle, onValueChange = onTitleChanged,
            label = { Text("Nome da meta") }, placeholder = { Text("Ex: Viagem para Europa...") },
            singleLine = true, modifier = Modifier.fillMaxWidth(), colors = fieldColors)

        TextField(value = uiState.formTargetValue, onValueChange = onTargetChanged,
            label = { Text("Valor alvo (R$)") }, placeholder = { Text("0,00") },
            singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(), colors = fieldColors)

        TextField(value = uiState.formCurrentValue, onValueChange = onCurrentChanged,
            label = { Text("Já tenho guardado (R$) — opcional") }, placeholder = { Text("0,00") },
            singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(), colors = fieldColors)

        TextField(value = uiState.formDeadline, onValueChange = onDeadlineChanged,
            label = { Text("Prazo (AAAA-MM-DD) — opcional") }, placeholder = { Text("2026-12-31") },
            singleLine = true, modifier = Modifier.fillMaxWidth(), colors = fieldColors)

        uiState.formError?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp) }

        Button(onClick = onSave, enabled = !uiState.isSaving,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Purple40)) {
            if (uiState.isSaving) CircularProgressIndicator(Modifier.size(24.dp), color = Color.White)
            else Text("Criar meta", fontSize = 16.sp, color = Color.White)
        }
    }
}

// ─── Sheet: atualizar progresso ───

@Composable
private fun UpdateProgressSheetContent(
    uiState: GoalsUiState,
    fieldColors: TextFieldColors,
    onProgressChanged: (String) -> Unit,
    onConfirm: () -> Unit,
) {
    val remaining = (uiState.progressGoalTarget - uiState.progressGoalCurrent).coerceAtLeast(0.0)
    val progress = if (uiState.progressGoalTarget > 0)
        (uiState.progressGoalCurrent / uiState.progressGoalTarget).toFloat().coerceIn(0f, 1f) else 0f

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Atualizar Progresso", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(uiState.progressGoalTitle, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF6B6B6B))

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
            color = Purple40, trackColor = Purple40.copy(alpha = 0.12f),
        )
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
            Text(uiState.progressGoalCurrent.toBRL(),
                style = MaterialTheme.typography.bodySmall, color = Purple40, fontWeight = FontWeight.SemiBold)
            Text("Faltam ${remaining.toBRL()}",
                style = MaterialTheme.typography.bodySmall, color = Color(0xFF9E9E9E))
        }

        TextField(value = uiState.progressInput, onValueChange = onProgressChanged,
            label = { Text("Quanto você guardou agora? (R$)") }, placeholder = { Text("0,00") },
            singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(), colors = fieldColors)

        Button(onClick = onConfirm, enabled = !uiState.isUpdatingProgress,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Purple40)) {
            if (uiState.isUpdatingProgress) CircularProgressIndicator(Modifier.size(24.dp), color = Color.White)
            else Text("Confirmar", fontSize = 16.sp, color = Color.White)
        }
    }
}

// ─── Estado vazio ───

@Composable
private fun EmptyGoalsState() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🎯", fontSize = 64.sp)
            Spacer(Modifier.height(16.dp))
            Text("Nenhuma meta ainda", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Text("Crie sua primeira meta e\nacompanhe seu progresso!",
                style = MaterialTheme.typography.bodyMedium, color = Color(0xFF9E9E9E),
                textAlign = TextAlign.Center)
        }
    }
}