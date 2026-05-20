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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import io.poupai.app.core.designsystem.components.PoupaiDrawerScaffold
import io.poupai.app.core.designsystem.components.PullToRefresh
import io.poupai.app.core.designsystem.components.TopLevelNavCallbacks
import io.poupai.app.core.theme.GreenPositive
import io.poupai.app.core.theme.PoupaiTheme
import io.poupai.app.core.theme.Purple40
import io.poupai.app.core.theme.PurpleDark
import io.poupai.app.core.util.toBRL
import io.poupai.app.domain.model.Goal
import io.poupai.app.features.goals.state.GoalsUiState
import io.poupai.app.features.goals.viewmodel.GoalsViewModel
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.Locale

private val iconOptions = listOf("🎯", "🏠", "✈️", "🚗", "📱", "💍", "🎓", "🏋️", "💻", "🎸", "🐾", "🌍")
private val colorOptions = listOf(
    "#503173", "#4CAF50", "#FF9800", "#E91E63",
    "#2196F3", "#009688", "#FF5722", "#795548",
)

private fun String.toComposeColor(): Color = try {
    Color(android.graphics.Color.parseColor(if (startsWith("#")) this else "#$this"))
} catch (e: Exception) { Color(0xFF503173) }

private fun Date.toLocalDate(): LocalDate =
    toInstant().atZone(ZoneId.systemDefault()).toLocalDate()

private fun daysUntilDeadline(deadline: Date?): Long? =
    deadline?.let { ChronoUnit.DAYS.between(LocalDate.now(), it.toLocalDate()) }

private fun monthsUntilDeadline(deadline: Date?): Long? =
    deadline?.let { ChronoUnit.MONTHS.between(LocalDate.now(), it.toLocalDate()) }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun GoalsScreen(
    topLevelNav: TopLevelNavCallbacks,
    viewModel: GoalsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val addSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val progressSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val dateFormat = remember { SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()) }

    val completed = uiState.goals.count { it.isCompleted }
    val totalTarget = uiState.goals.sumOf { it.targetValue }
    val totalCurrent = uiState.goals.sumOf { it.currentValue }
    val active = uiState.goals.filter { !it.isCompleted }
    val done = uiState.goals.filter { it.isCompleted }

    val fieldColors = TextFieldDefaults.colors(
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
        focusedIndicatorColor = Purple40,
        unfocusedIndicatorColor = PoupaiTheme.tokens.textMuted,
        focusedLabelColor = Purple40,
        unfocusedLabelColor = PoupaiTheme.tokens.textMuted,
        focusedTextColor = PoupaiTheme.tokens.textPrimary,
        unfocusedTextColor = PoupaiTheme.tokens.textPrimary,
        cursorColor = Purple40,
    )

    PoupaiDrawerScaffold(
        selectedRoute = "goals",
        nav = topLevelNav,
    ) { onMenuClick ->
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PoupaiTheme.tokens.bg),
    ) {
        // ─── Header ───
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(PurpleDark, Purple40)))
                .padding(horizontal = 20.dp)
                .padding(top = 16.dp, bottom = 16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onMenuClick) {
                    Icon(Icons.Default.Menu, "Menu", tint = Color.White)
                }
                Spacer(Modifier.weight(1f))
                Text("Metas", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                Spacer(Modifier.size(48.dp))
            }
        }

        PullToRefresh(
            isRefreshing = uiState.isRefreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier.weight(1f),
        ) {
            when {
                uiState.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Purple40)
                }

                uiState.goals.isEmpty() -> EmptyGoalsState(onAdd = viewModel::onShowAddSheet)

                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        // ─── Hero ───
                        item {
                            GoalsHeroCard(
                                totalCurrent = totalCurrent,
                                totalTarget = totalTarget,
                                total = uiState.goals.size,
                                completed = completed,
                                active = active.size,
                            )
                        }

                        // ─── Em andamento ───
                        if (active.isNotEmpty()) {
                            item { SectionTitle("Em andamento", Icons.Default.TrackChanges) }
                            items(active, key = { it.id }) { goal ->
                                GoalCard(
                                    goal = goal,
                                    dateFormat = dateFormat,
                                    onAddProgress = { viewModel.onShowProgressSheet(goal) },
                                    onDelete = { viewModel.onDeleteGoal(goal.id) },
                                )
                            }
                        }

                        // ─── Concluídas ───
                        if (done.isNotEmpty()) {
                            item { SectionTitle("Concluídas", Icons.Default.EmojiEvents, GreenPositive) }
                            items(done, key = { it.id }) { goal ->
                                GoalCard(
                                    goal = goal,
                                    dateFormat = dateFormat,
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
    } // close PoupaiDrawerScaffold

    // ─── FAB ───
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomEnd) {
        FloatingActionButton(
            onClick = viewModel::onShowAddSheet,
            containerColor = Purple40,
            shape = CircleShape,
            modifier = Modifier.padding(24.dp),
        ) { Icon(Icons.Default.Add, "Nova meta", tint = Color.White) }
    }

    // ─── Sheet: nova meta ───
    if (uiState.showAddSheet) {
        ModalBottomSheet(onDismissRequest = viewModel::onDismissAddSheet, sheetState = addSheetState) {
            AddGoalSheetContent(
                uiState = uiState,
                fieldColors = fieldColors,
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

    // ─── Sheet: progresso ───
    if (uiState.showProgressSheet) {
        ModalBottomSheet(onDismissRequest = viewModel::onDismissProgressSheet, sheetState = progressSheetState) {
            UpdateProgressSheetContent(
                uiState = uiState,
                fieldColors = fieldColors,
                onProgressChanged = viewModel::onProgressInputChanged,
                onConfirm = viewModel::onUpdateProgress,
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String, icon: ImageVector, tint: Color = Purple40) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 4.dp, start = 2.dp),
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text(
            text,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = PoupaiTheme.tokens.textSecondary,
        )
    }
}

// ─── HERO ───

@Composable
private fun GoalsHeroCard(
    totalCurrent: Double,
    totalTarget: Double,
    total: Int,
    completed: Int,
    active: Int,
) {
    val progress = if (totalTarget > 0) (totalCurrent / totalTarget).toFloat().coerceIn(0f, 1f) else 0f
    var animPlayed by remember { mutableStateOf(false) }
    val animProgress by animateFloatAsState(if (animPlayed) progress else 0f, tween(900), label = "overall")
    LaunchedEffect(Unit) { animPlayed = true }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(6.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(listOf(PurpleDark, Purple40, Color(0xFF6B4396))),
                    RoundedCornerShape(22.dp),
                )
                .padding(22.dp),
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Progresso geral", fontSize = 11.sp, color = Color.White.copy(alpha = 0.65f))
                    Spacer(Modifier.weight(1f))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White.copy(alpha = 0.18f),
                    ) {
                        Text(
                            "${(progress * 100).toInt()}%",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontSize = 11.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }

                Spacer(Modifier.height(6.dp))
                Text(
                    if (totalTarget > 0) totalCurrent.toBRL() else "R$ 0,00",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    if (totalTarget > 0) "de ${totalTarget.toBRL()} guardado" else "Defina suas metas",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.65f),
                )

                Spacer(Modifier.height(14.dp))
                LinearProgressIndicator(
                    progress = { animProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.22f),
                )

                Spacer(Modifier.height(18.dp))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color.White.copy(alpha = 0.15f)),
                )
                Spacer(Modifier.height(14.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    HeroStat(
                        modifier = Modifier.weight(1f),
                        label = "Em andamento",
                        value = "$active",
                        icon = Icons.Default.TrackChanges,
                    )
                    Box(
                        Modifier.width(1.dp).height(40.dp)
                            .background(Color.White.copy(alpha = 0.15f)),
                    )
                    HeroStat(
                        modifier = Modifier.weight(1f),
                        label = "Concluídas",
                        value = "$completed",
                        icon = Icons.Default.EmojiEvents,
                    )
                    Box(
                        Modifier.width(1.dp).height(40.dp)
                            .background(Color.White.copy(alpha = 0.15f)),
                    )
                    HeroStat(
                        modifier = Modifier.weight(1f),
                        label = "Total",
                        value = "$total",
                        icon = Icons.Default.Flag,
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroStat(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    icon: ImageVector,
) {
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(20.dp).clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, null, tint = Color.White, modifier = Modifier.size(12.dp))
            }
            Spacer(Modifier.width(6.dp))
            Text(
                label,
                fontSize = 10.sp,
                color = Color.White.copy(alpha = 0.7f),
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            value,
            fontSize = 18.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold,
        )
    }
}

// ─── CARD INDIVIDUAL ───

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
        if (animPlayed) goal.progress else 0f,
        tween(800),
        label = "goal_${goal.id}",
    )
    LaunchedEffect(Unit) { animPlayed = true }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = PoupaiTheme.tokens.surface),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(goalColor.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center,
                ) { Text(goal.icon, fontSize = 20.sp) }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        goal.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PoupaiTheme.tokens.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "${(goal.progress * 100).toInt()}%",
                            fontSize = 11.sp,
                            color = goalColor,
                            fontWeight = FontWeight.Bold,
                        )
                        goal.deadline?.let {
                            Text(
                                " · ${dateFormat.format(it)}",
                                fontSize = 11.sp,
                                color = PoupaiTheme.tokens.textMuted,
                            )
                        }
                    }
                }

                if (goal.isCompleted) {
                    Box(
                        Modifier.size(28.dp).clip(CircleShape)
                            .background(GreenPositive.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Default.CheckCircle, null, tint = GreenPositive,
                            modifier = Modifier.size(18.dp))
                    }
                } else {
                    IconButton(onClick = onAddProgress, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Edit, "Atualizar", tint = goalColor, modifier = Modifier.size(18.dp))
                    }
                }
                IconButton(onClick = { showDeleteDialog = true }, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Delete, "Excluir", tint = PoupaiTheme.tokens.textMuted, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(Modifier.height(14.dp))

            LinearProgressIndicator(
                progress = { animProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (goal.isCompleted) GreenPositive else goalColor,
                trackColor = goalColor.copy(alpha = 0.10f),
            )

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text("Guardado", fontSize = 10.sp, color = PoupaiTheme.tokens.textMuted)
                    Text(
                        goal.currentValue.toBRL(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (goal.isCompleted) GreenPositive else goalColor,
                    )
                }
                if (!goal.isCompleted) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Falta", fontSize = 10.sp, color = PoupaiTheme.tokens.textMuted)
                        Text(
                            goal.remaining.toBRL(),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PoupaiTheme.tokens.textPrimary,
                        )
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = GreenPositive.copy(alpha = 0.14f),
                    ) {
                        Text(
                            "Objetivo alcançado",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = GreenPositive,
                        )
                    }
                }
            }

            // ─── Insights de prazo / aporte sugerido ───
            if (!goal.isCompleted && goal.deadline != null) {
                DeadlineInsights(goal = goal, goalColor = goalColor)
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
            shape = RoundedCornerShape(16.dp),
        )
    }
}

// ─── SHEET: NOVA META ───

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
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text("Nova Meta", fontSize = 20.sp, fontWeight = FontWeight.Bold,
            color = PoupaiTheme.tokens.textPrimary)

        Text("Ícone", style = MaterialTheme.typography.labelMedium,
            color = PoupaiTheme.tokens.textMuted)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            iconOptions.forEach { icon ->
                val selected = uiState.formIcon == icon
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (selected) Purple40.copy(alpha = 0.18f)
                            else PoupaiTheme.tokens.surfaceAlt
                        )
                        .clickable { onIconChanged(icon) },
                    contentAlignment = Alignment.Center,
                ) { Text(icon, fontSize = 20.sp) }
            }
        }

        Text("Cor", style = MaterialTheme.typography.labelMedium,
            color = PoupaiTheme.tokens.textMuted)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
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
            label = { Text("Prazo (dd-mm-aaaa) — opcional") }, placeholder = { Text("31-12-2026") },
            singleLine = true, modifier = Modifier.fillMaxWidth(), colors = fieldColors)

        uiState.formError?.let {
            Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
        }

        Button(
            onClick = onSave, enabled = !uiState.isSaving,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Purple40),
        ) {
            if (uiState.isSaving) CircularProgressIndicator(Modifier.size(24.dp), color = Color.White)
            else Text("Criar meta", fontSize = 16.sp, color = Color.White)
        }
    }
}

// ─── SHEET: ATUALIZAR PROGRESSO ───

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
        Text("Atualizar Progresso", fontSize = 20.sp, fontWeight = FontWeight.Bold,
            color = PoupaiTheme.tokens.textPrimary)
        Text(uiState.progressGoalTitle, style = MaterialTheme.typography.bodyMedium,
            color = PoupaiTheme.tokens.textSecondary)

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
            color = Purple40, trackColor = Purple40.copy(alpha = 0.12f),
        )
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
            Text(uiState.progressGoalCurrent.toBRL(), style = MaterialTheme.typography.bodySmall,
                color = Purple40, fontWeight = FontWeight.SemiBold)
            Text("Faltam ${remaining.toBRL()}", style = MaterialTheme.typography.bodySmall,
                color = PoupaiTheme.tokens.textMuted)
        }

        TextField(
            value = uiState.progressInput, onValueChange = onProgressChanged,
            label = { Text("Quanto você guardou agora? (R$)") }, placeholder = { Text("0,00") },
            singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(), colors = fieldColors,
        )

        Button(
            onClick = onConfirm, enabled = !uiState.isUpdatingProgress,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Purple40),
        ) {
            if (uiState.isUpdatingProgress) CircularProgressIndicator(Modifier.size(24.dp), color = Color.White)
            else Text("Confirmar", fontSize = 16.sp, color = Color.White)
        }
    }
}

// ─── INSIGHTS DE PRAZO ───

@Composable
private fun DeadlineInsights(goal: Goal, goalColor: Color) {
    val days = daysUntilDeadline(goal.deadline) ?: return
    val months = monthsUntilDeadline(goal.deadline) ?: return

    Spacer(Modifier.height(14.dp))
    Box(
        Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(PoupaiTheme.tokens.divider),
    )
    Spacer(Modifier.height(12.dp))

    when {
        days < 0 -> {
            // Prazo expirado
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFFFEBEE))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier.size(22.dp).clip(CircleShape)
                        .background(Color(0xFFC62828).copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.WarningAmber, null,
                        tint = Color(0xFFC62828), modifier = Modifier.size(13.dp))
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    "Prazo expirado · considere revisar o prazo",
                    fontSize = 11.sp,
                    color = Color(0xFFC62828),
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        months >= 1 -> {
            // Sugestão mensal
            val required = goal.remaining / months
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier.size(28.dp).clip(CircleShape)
                        .background(goalColor.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Insights, null,
                        tint = goalColor, modifier = Modifier.size(15.dp))
                }
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Aporte sugerido", fontSize = 10.sp, color = PoupaiTheme.tokens.textMuted)
                    Text(
                        "${required.toBRL()} / mês",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = goalColor,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Tempo restante", fontSize = 10.sp, color = PoupaiTheme.tokens.textMuted)
                    val years = months / 12
                    val label = when {
                        years >= 2 -> "$months meses · ~$years anos"
                        years == 1L -> "$months meses · 1 ano"
                        else -> "$months ${if (months == 1L) "mês" else "meses"}"
                    }
                    Text(
                        label,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PoupaiTheme.tokens.textPrimary,
                    )
                }
            }
        }

        else -> {
            // Faltam menos de 1 mês
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier.size(28.dp).clip(CircleShape)
                        .background(Color(0xFFE65100).copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Schedule, null,
                        tint = Color(0xFFE65100), modifier = Modifier.size(15.dp))
                }
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Falta guardar", fontSize = 10.sp, color = PoupaiTheme.tokens.textMuted)
                    Text(
                        goal.remaining.toBRL(),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = goalColor,
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFE65100).copy(alpha = 0.14f),
                ) {
                    Text(
                        when (days) {
                            0L -> "Vence hoje"
                            1L -> "Falta 1 dia"
                            else -> "$days dias"
                        },
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE65100),
                    )
                }
            }
        }
    }
}

// ─── ESTADO VAZIO ───

@Composable
private fun EmptyGoalsState(onAdd: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 32.dp),
        ) {
            Box(
                Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(Purple40.copy(alpha = 0.18f), Purple40.copy(alpha = 0.08f))
                        )
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Default.TrackChanges,
                    null,
                    tint = Purple40,
                    modifier = Modifier.size(36.dp),
                )
            }
            Spacer(Modifier.height(20.dp))
            Text(
                "Nenhuma meta ainda",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = PoupaiTheme.tokens.textPrimary,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Crie sua primeira meta e\nacompanhe seu progresso!",
                style = MaterialTheme.typography.bodyMedium,
                color = PoupaiTheme.tokens.textMuted,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onAdd,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Purple40),
                modifier = Modifier.height(48.dp),
            ) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp), tint = Color.White)
                Spacer(Modifier.width(8.dp))
                Text("Criar primeira meta", color = Color.White)
            }
        }
    }
}
