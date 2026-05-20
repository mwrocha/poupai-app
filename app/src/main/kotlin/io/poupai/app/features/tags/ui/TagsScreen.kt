package io.poupai.app.features.tags.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import io.poupai.app.core.designsystem.components.EyeToggleIcon
import io.poupai.app.core.designsystem.components.PoupaiDrawerScaffold
import io.poupai.app.core.designsystem.components.PullToRefresh
import io.poupai.app.core.designsystem.components.TopLevelNavCallbacks
import io.poupai.app.core.theme.PoupaiTheme
import io.poupai.app.core.theme.Purple40
import io.poupai.app.core.theme.Purple60
import io.poupai.app.core.theme.PurpleDark
import io.poupai.app.core.theme.PurpleLight
import io.poupai.app.core.theme.RedNegative
import io.poupai.app.core.util.toBRL
import io.poupai.app.domain.model.Tag
import io.poupai.app.domain.model.Transaction
import io.poupai.app.features.tags.viewmodel.TagsViewModel
import java.text.SimpleDateFormat
import java.util.Locale

private const val HIDDEN = "••••"

// Paleta on-brand pra tags. O `tag.color` que vem do backend pode ser qualquer
// HEX (verde, laranja, etc, definidos por convenções antigas). Em vez de
// confiar nesse dado, distribuímos as tags entre 6 tons de roxo de forma
// estável (hash do nome) — assim cada categoria mantém sempre a mesma cor
// dentro da identidade roxa do app.
private val tagPalette = listOf(
    Purple40,                  // #503173
    Purple60,                  // #9B7FD4
    Color(0xFF7C5295),         // mid
    Color(0xFFB39DDB),         // lavanda clara
    Color(0xFFD1C4E9),         // lavanda muito clara
    Color(0xFF6A3F9E),         // roxo profundo
)

private fun tagColorFor(tag: Tag): Color {
    val seed = tag.name.ifBlank { tag.id }
    val idx = ((seed.hashCode() % tagPalette.size) + tagPalette.size) % tagPalette.size
    return tagPalette[idx]
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagsScreen(
    topLevelNav: TopLevelNavCallbacks,
    viewModel: TagsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val monthNames = listOf("Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
        "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro")

    if (uiState.selectedTag != null) {
        ModalBottomSheet(
            onDismissRequest = viewModel::onDismissDetail,
            sheetState = sheetState,
        ) {
            TagDetailSheet(
                tag = uiState.selectedTag!!,
                transactions = uiState.tagTransactions,
                isLoading = uiState.isLoadingDetail,
                totalSpent = uiState.totalSpent,
                hideValues = uiState.hideValues,
            )
        }
    }

    PoupaiDrawerScaffold(
        selectedRoute = "tags",
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
                Text("Tags", style = MaterialTheme.typography.titleLarge,
                    color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                EyeToggleIcon(hideValues = uiState.hideValues, onToggle = viewModel::toggleHideValues)
            }
        }

        uiState.errorMessage?.let { err ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
            ) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(err, Modifier.weight(1f), fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onErrorContainer)
                    TextButton(onClick = viewModel::clearError) { Text("Ok") }
                }
            }
        }

        PullToRefresh(
            isRefreshing = uiState.isRefreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier.weight(1f),
        ) {
            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Purple40)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {

                    item {
                        MonthSelector(
                            label = monthNames.getOrElse(uiState.selectedMonth - 1) { "" },
                            year = uiState.selectedYear,
                            onPrevious = viewModel::onPreviousMonth,
                            onNext = viewModel::onNextMonth,
                        )
                    }

                    item {
                        SpentHeroCard(
                            totalSpent = uiState.totalSpent,
                            tagCount = uiState.filteredTags.size,
                            biggest = uiState.filteredTags.maxByOrNull { it.totalSpent },
                            hideValues = uiState.hideValues,
                        )
                    }

                    item {
                        SearchField(
                            value = uiState.searchQuery,
                            onValueChange = viewModel::onSearchQueryChanged,
                        )
                    }

                    if (uiState.filteredTags.isEmpty()) {
                        item {
                            EmptyState(
                                searching = uiState.searchQuery.isNotBlank(),
                            )
                        }
                    } else {
                        item {
                            SectionTitle(
                                "${uiState.filteredTags.size} " +
                                    if (uiState.filteredTags.size == 1) "categoria" else "categorias",
                                Icons.Default.Category,
                            )
                        }

                        items(uiState.filteredTags, key = { it.id }) { tag ->
                            TagCard(
                                tag = tag,
                                totalSpent = uiState.totalSpent,
                                hideValues = uiState.hideValues,
                                onClick = { viewModel.onTagSelected(tag) },
                            )
                        }
                    }

                    item { Spacer(Modifier.height(40.dp)) }
                }
            }
        }
    }
    } // close PoupaiDrawerScaffold
}

@Composable
private fun SectionTitle(text: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 4.dp, start = 2.dp),
    ) {
        Icon(icon, null, tint = Purple40, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text(
            text,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = PoupaiTheme.tokens.textSecondary,
        )
    }
}

// ─── SELETOR DE MÊS ───

@Composable
private fun MonthSelector(
    label: String,
    year: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = PoupaiTheme.tokens.surface),
        elevation = CardDefaults.cardElevation(1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            IconButton(onClick = onPrevious) {
                Icon(Icons.Default.ArrowBackIosNew, "Mês anterior",
                    tint = Purple40, modifier = Modifier.size(16.dp))
            }
            Text(
                "$label $year",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = PoupaiTheme.tokens.textPrimary,
            )
            IconButton(onClick = onNext) {
                Icon(Icons.Default.ArrowForwardIos, "Próximo mês",
                    tint = Purple40, modifier = Modifier.size(16.dp))
            }
        }
    }
}

// ─── HERO ───

@Composable
private fun SpentHeroCard(
    totalSpent: Double,
    tagCount: Int,
    biggest: Tag?,
    hideValues: Boolean,
) {
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
                Text("Total gasto no mês", fontSize = 11.sp, color = Color.White.copy(alpha = 0.65f))
                Spacer(Modifier.height(6.dp))
                Text(
                    if (hideValues) HIDDEN else totalSpent.toBRL(),
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
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
                        label = "Categorias",
                        value = "$tagCount",
                        icon = Icons.Default.Category,
                    )
                    Box(
                        Modifier.width(1.dp).height(40.dp)
                            .background(Color.White.copy(alpha = 0.15f)),
                    )
                    HeroStat(
                        modifier = Modifier.weight(1.4f),
                        label = "Maior gasto",
                        value = biggest?.name ?: "—",
                        subtitle = if (biggest != null && !hideValues)
                            biggest.totalSpent.toBRL()
                        else if (biggest != null && hideValues) HIDDEN
                        else null,
                        icon = Icons.Default.TrendingDown,
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
    subtitle: String? = null,
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
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            value,
            fontSize = if (subtitle != null) 14.sp else 18.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (subtitle != null) {
            Text(
                subtitle,
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

// ─── SEARCH ───

@Composable
private fun SearchField(value: String, onValueChange: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(PoupaiTheme.tokens.surface)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.Default.Search, null,
            tint = PoupaiTheme.tokens.textMuted,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(10.dp))
        BasicTextFieldCompat(
            value = value,
            onValueChange = onValueChange,
            placeholder = "Pesquisar categoria...",
            modifier = Modifier.weight(1f),
        )
        if (value.isNotBlank()) {
            IconButton(
                onClick = { onValueChange("") },
                modifier = Modifier.size(28.dp),
            ) {
                Icon(
                    Icons.Default.Clear, "Limpar",
                    tint = PoupaiTheme.tokens.textMuted,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

@Composable
private fun BasicTextFieldCompat(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        if (value.isEmpty()) {
            Text(
                placeholder,
                fontSize = 13.sp,
                color = PoupaiTheme.tokens.textMuted,
            )
        }
        androidx.compose.foundation.text.BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = androidx.compose.ui.text.TextStyle(
                fontSize = 13.sp,
                color = PoupaiTheme.tokens.textPrimary,
            ),
            cursorBrush = androidx.compose.ui.graphics.SolidColor(Purple40),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

// ─── TAG CARD INDIVIDUAL ───

@Composable
private fun TagCard(tag: Tag, totalSpent: Double, hideValues: Boolean, onClick: () -> Unit) {
    val percent = if (totalSpent > 0) (tag.totalSpent / totalSpent).toFloat() else 0f
    val percentInt = (percent * 100).toInt()
    val tagColor = tagColorFor(tag)

    var animationPlayed by remember { mutableStateOf(false) }
    val animProgress by animateFloatAsState(
        targetValue = if (animationPlayed) percent else 0f,
        animationSpec = tween(800),
        label = "tag_progress",
    )
    LaunchedEffect(tag.id) { animationPlayed = true }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PoupaiTheme.tokens.surface),
        elevation = CardDefaults.cardElevation(1.dp),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(tagColor.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    tag.name.take(2).uppercase(),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = tagColor,
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        tag.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PoupaiTheme.tokens.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = tagColor.copy(alpha = 0.14f),
                    ) {
                        Text(
                            "$percentInt%",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = tagColor,
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { animProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = tagColor,
                    trackColor = tagColor.copy(alpha = 0.10f),
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    if (hideValues) HIDDEN else tag.totalSpent.toBRL(),
                    fontSize = 11.sp,
                    color = PoupaiTheme.tokens.textSecondary,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            Spacer(Modifier.width(8.dp))
            Icon(
                Icons.Default.ChevronRight,
                null,
                tint = PoupaiTheme.tokens.textMuted,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

// ─── EMPTY STATE ───

@Composable
private fun EmptyState(searching: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = PoupaiTheme.tokens.surface),
        elevation = CardDefaults.cardElevation(1.dp),
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .padding(40.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(PurpleLight.copy(alpha = 0.55f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Default.Inbox, null,
                        tint = Purple40, modifier = Modifier.size(28.dp),
                    )
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    if (searching) "Nenhuma categoria encontrada"
                    else "Nenhuma despesa neste mês",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = PoupaiTheme.tokens.textSecondary,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    if (searching) "Tente outra palavra-chave"
                    else "Registre transações para vê-las aqui",
                    fontSize = 12.sp,
                    color = PoupaiTheme.tokens.textMuted,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

// ─── BOTTOM SHEET DE DETALHE ───

@Composable
private fun TagDetailSheet(
    tag: Tag,
    transactions: List<Transaction>,
    isLoading: Boolean,
    totalSpent: Double,
    hideValues: Boolean,
) {
    val tagColor = tagColorFor(tag)
    val percent = if (totalSpent > 0) (tag.totalSpent / totalSpent * 100).toInt() else 0

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 40.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(14.dp))
                    .background(tagColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    tag.name.take(2).uppercase(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = tagColor,
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(tag.name, fontSize = 18.sp, fontWeight = FontWeight.Bold,
                    color = PoupaiTheme.tokens.textPrimary)
                Text("$percent% do total gasto", fontSize = 12.sp,
                    color = PoupaiTheme.tokens.textSecondary)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    if (hideValues) HIDDEN else tag.totalSpent.toBRL(),
                    fontSize = 18.sp, fontWeight = FontWeight.Bold, color = tagColor,
                )
                Text(
                    "${transactions.size} transaç${if (transactions.size == 1) "ão" else "ões"}",
                    fontSize = 11.sp,
                    color = PoupaiTheme.tokens.textMuted,
                )
            }
        }

        Spacer(Modifier.height(18.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(PoupaiTheme.tokens.divider),
        )
        Spacer(Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.Receipt, null,
                tint = Purple40, modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(6.dp))
            Text(
                "Transações",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = PoupaiTheme.tokens.textSecondary,
            )
        }

        Spacer(Modifier.height(8.dp))

        when {
            isLoading -> Box(
                Modifier.fillMaxWidth().height(120.dp),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = tagColor, modifier = Modifier.size(32.dp))
            }

            transactions.isEmpty() -> Box(
                Modifier.fillMaxWidth().height(120.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "Nenhuma transação encontrada",
                    style = MaterialTheme.typography.bodyMedium,
                    color = PoupaiTheme.tokens.textMuted,
                )
            }

            else -> Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                transactions.forEachIndexed { index, transaction ->
                    TagTransactionRow(
                        transaction = transaction,
                        hideValues = hideValues,
                    )
                    if (index < transactions.lastIndex) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(PoupaiTheme.tokens.surfaceAlt),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TagTransactionRow(transaction: Transaction, hideValues: Boolean) {
    val dateFormatter = SimpleDateFormat("dd 'de' MMM", Locale("pt", "BR"))

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(38.dp).clip(RoundedCornerShape(10.dp))
                .background(RedNegative.copy(alpha = 0.10f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.TrendingDown, null, tint = RedNegative, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                transaction.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = PoupaiTheme.tokens.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                dateFormatter.format(transaction.date).replaceFirstChar { it.uppercase() },
                fontSize = 11.sp,
                color = PoupaiTheme.tokens.textMuted,
            )
        }
        Text(
            if (hideValues) HIDDEN else "- ${transaction.amount.toBRL()}",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = RedNegative,
        )
    }
}
